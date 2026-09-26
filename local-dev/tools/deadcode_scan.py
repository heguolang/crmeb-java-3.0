#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
QIANXU 死代码静态扫描（保守版：只报「高置信度」，宁可漏报不误报）

判定原则
  1) 只在**全仓库文本**里找引用（含注释/字符串），出现即视为「被引用」→ 避免误删；
     即：报出来的都是「连注释里都没提过」的东西。
  2) 框架会扫描/注册的类（@RestController/@Service/@Component/@Mapper/@TableName 等）
     不判为死代码，单独列出供人工判断。
  3) private 方法/字段只在**本文件内**可能被调用 → 本文件内仅出现 1 次即从未被使用。
  4) Lombok @Data/@Getter 类不做字段判定（getter 由编译期生成，外部可用）。

用法： python local-dev/tools/deadcode_scan.py [--json out.json]
"""
import os
import re
import sys
import json
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

TEXT_EXT = {'.java', '.xml', '.yml', '.yaml', '.properties', '.json',
            '.js', '.vue', '.ts', '.sql', '.sh', '.bat', '.scss', '.css'}
SKIP_DIR = {'target', 'node_modules', '.git', 'dist', 'unpackage', 'build'}

# 框架会自己扫描/注入的注解 —— 即使源码里零引用也不能删
FRAMEWORK_ANN = {
    'RestController', 'Controller', 'Service', 'Component', 'Repository',
    'Configuration', 'Mapper', 'TableName', 'Aspect', 'ControllerAdvice',
    'RestControllerAdvice', 'SpringBootApplication', 'MapperScan',
    'ServletComponentScan', 'ConfigurationProperties', 'Entity', 'Embeddable',
    'Converter', 'WebFilter', 'WebListener', 'Scheduled', 'FeignClient',
    'MappedSuperclass', 'JsonComponent', 'SessionAttributes',
    'EnableWebSecurity', 'EnableScheduling', 'EnableAsync', 'EnableCaching',
}

# 实现这些接口/继承这些基类，容器会自己装配 —— 同样不能删
FRAMEWORK_IMPL_MARKERS = (
    'ResponseBodyAdvice', 'HandlerInterceptor', 'WebMvcConfigurer',
    'OncePerRequestFilter', 'ApplicationRunner', 'CommandLineRunner',
    'HandlerExceptionResolver', 'InitializingBean', 'DisposableBean',
    'ApplicationListener', 'ErrorController', 'ArgumentResolver',
    'BaseMapper', 'ServiceImpl<', 'Interceptor', 'MappingJackson2HttpMessageConverter',
)

TYPE_RE = re.compile(
    r'^\s*(?:@\w+(?:\([^)]*\))?\s*)*'
    r'(?:public\s+|protected\s+|private\s+)?'
    r'(?:static\s+|final\s+|abstract\s+)*'
    r'(class|interface|enum)\s+([A-Za-z_$][\w$]*)',
    re.M)


def walk(exts):
    for base, dirs, files in os.walk(ROOT):
        dirs[:] = [d for d in dirs if d not in SKIP_DIR]
        for f in files:
            if os.path.splitext(f)[1].lower() in exts:
                yield os.path.join(base, f)


def load_corpus():
    """text[path] = 文件内容（读不了就跳过，如编码异常）"""
    text = {}
    for p in walk(TEXT_EXT):
        try:
            with open(p, 'r', encoding='utf-8', errors='replace') as fh:
                text[p] = fh.read()
        except OSError:
            pass
    return text


def strip_comments_and_strings(src):
    out, i, n = [], 0, len(src)
    while i < n:
        c = src[i]
        if c == '/' and i + 1 < n and src[i + 1] == '/':
            while i < n and src[i] != '\n':
                i += 1
        elif c == '/' and i + 1 < n and src[i + 1] == '*':
            i += 2
            while i + 1 < n and not (src[i] == '*' and src[i + 1] == '/'):
                i += 1
            i += 2
        elif c in '"\'':
            q = c
            i += 1
            while i < n and src[i] != q:
                i += 2 if src[i] == '\\' else 1
            i += 1
        else:
            out.append(c)
            i += 1
    return ''.join(out)


def annotations_before(code, pos):
    """取紧贴在 pos 之前的那一段注解（用最近的 ; 或 } 截断，避免把上一个类/方法的注解带进来）"""
    seg = code[max(0, pos - 2000):pos]
    cut = max(seg.rfind(';'), seg.rfind('}'))
    if cut >= 0:
        seg = seg[cut + 1:]
    return re.findall(r'@([A-Z]\w+)', seg)


def top_level_types(src):
    """返回 [(kind, name, annotations, pos)]，只看大括号深度 0 的位置"""
    res = []
    depth = 0
    code = strip_comments_and_strings(src)
    for m in re.finditer(r'[{}]|^\s*(?:@\w+(?:\([^)]*\))?\s*|'
                         r'(?:public|protected|private|static|final|abstract)\s+)*'
                         r'(class|interface|enum)\s+([A-Za-z_$][\w$]*)', code, re.M):
        tok = m.group(0)
        if tok.startswith('{'):
            depth += 1
        elif tok.startswith('}'):
            depth -= 1
        elif m.group(1) and depth == 0:
            kwm = re.search(r'\b(?:class|interface|enum)\s+', m.group(0))
            kw = m.start() + (kwm.start() if kwm else 0)
            res.append((m.group(1), m.group(2), annotations_before(code, kw), kw))
    return res


def main():
    corpus = load_corpus()
    java_files = [p for p in corpus if p.endswith('.java')]
    print('# 扫描文件数: 全部 %d，其中 java %d' % (len(corpus), len(java_files)))

    # ---- 每个 java 文件里声明的顶层类型 ----
    decls = defaultdict(list)          # name -> [ {file, kind, anns} ]
    stripped = {}
    for p in java_files:
        stripped[p] = strip_comments_and_strings(corpus[p])
        for kind, name, anns, pos in top_level_types(corpus[p]):
            decls[name].append({'file': p, 'kind': kind, 'anns': anns, 'pos': pos})

    # ---- 全仓库引用计数（单遍分词，避免逐类全库正则）----
    from collections import Counter
    per_file = {}
    total_counter = Counter()
    WORD = re.compile(r'[A-Za-z_$][\w$]*')
    for p, t in corpus.items():
        c = Counter(WORD.findall(t))
        per_file[p] = c
        total_counter.update(c)
    ref_count = total_counter

    dead, framework_only = [], []
    for name, items in sorted(decls.items()):
        total = ref_count.get(name, 0)
        for it in items:
            own = per_file[it['file']].get(name, 0)
            external = total - own
            if external > 0:
                continue
            # 本文件内除声明外还有别的用法？仍算「仅文件内自用」→ 死
            rec = {'name': name, 'kind': it['kind'],
                   'file': os.path.relpath(it['file'], ROOT).replace('\\', '/'),
                   'own_refs': own, 'external_refs': external,
                   'anns': sorted(set(it['anns']))}
            sig = stripped[it['file']][it['pos']:it['pos'] + 400]
            registered = bool(FRAMEWORK_ANN & set(it['anns'])) or \
                any(mk in sig for mk in FRAMEWORK_IMPL_MARKERS)
            if registered:
                framework_only.append(rec)
            else:
                dead.append(rec)

    # ---- private 方法 / 字段（仅本文件内可见）----
    dead_members = []
    LOMBOK = ('@Data', '@Getter', '@Setter', '@Accessors', '@Builder', '@Value',
              '@EqualsAndHashCode', '@ToString')
    for p in java_files:
        raw = corpus[p]
        code = stripped[p]
        rel = os.path.relpath(p, ROOT).replace('\\', '/')
        # Lombok 会在编译期生成 getter/setter → 其私有字段并非无用，整文件跳过字段判定
        lombok_file = any(k in raw for k in LOMBOK)

        if not lombok_file:
            for m in re.finditer(
                    r'(?m)^[ \t]*private\s+(?:static\s+|final\s+|transient\s+|volatile\s+)*'
                    r'([\w<>\[\],.?]+)\s+(\w+)\s*(?:=|;)', code):
                nm = m.group(2)
                if nm == 'serialVersionUID':
                    continue
                if len(re.findall(r'\b' + re.escape(nm) + r'\b', code)) == 1:
                    dead_members.append({'file': rel, 'member': nm,
                                         'type': m.group(1).strip(),
                                         'sort': 'private field'})

        for m in re.finditer(
                r'(?m)^[ \t]*private\s+(?:static\s+|final\s+|synchronized\s+)*'
                r'(?!class|interface|enum)'
                r'([\w<>\[\],.?\s]+?)\s+(\w+)\s*\(', code):
            nm = m.group(2)
            if nm == 'serialVersionUID':
                continue
            # 容器/框架会回调的私有方法（@PostConstruct/@EventListener/@Scheduled 等）不算死代码
            if annotations_before(code, m.start() + m.group(0).find(nm)):
                continue
            if len(re.findall(r'\b' + re.escape(nm) + r'\b', code)) == 1:
                dead_members.append({'file': rel, 'member': nm,
                                     'type': m.group(1).strip(),
                                     'sort': 'private method'})

    rep = {'dead_types': dead, 'framework_registered_unreferenced': framework_only,
           'dead_private_members': dead_members}
    print('\n== 零外部引用的类（可删候选）: %d ==' % len(dead))
    for r in dead:
        print('  %-14s %-10s %s' % (r['kind'], r['name'], r['file']))
    print('\n== 零引用但由框架注册（不动，仅报告）: %d ==' % len(framework_only))
    for r in framework_only:
        print('  %-14s %-34s %s  %s' % (r['kind'], r['name'], r['file'], ','.join(r['anns'])))
    print('\n== 从未被使用的 private 成员: %d ==' % len(dead_members))
    for r in dead_members:
        print('  %-18s %-26s %s' % (r['sort'], r['member'], r['file']))

    if '--json' in sys.argv:
        out = sys.argv[sys.argv.index('--json') + 1]
        with open(out, 'w', encoding='utf-8') as fh:
            json.dump(rep, fh, ensure_ascii=False, indent=2)
        print('\n已写出 JSON -> %s' % out)


if __name__ == '__main__':
    main()
