-- =============================================================
-- 清空商品分类(type=1) + 重建「默认分类」+ 商品改挂
-- 目标库: crmeb   幂等可重复执行
-- 影响范围: 仅 eb_category.type = 1（产品分类），共 43 条
--          type=2/3/5/6 的附件/文章/菜单/配置分类【一律不动】
-- =============================================================

-- 0. 备份原数据（仅第一次执行时生成，避免重复执行覆盖）
CREATE TABLE IF NOT EXISTS bak_category_type1_20260916 AS
SELECT * FROM eb_category WHERE type = 1;

CREATE TABLE IF NOT EXISTS bak_store_product_cate_20260916 AS
SELECT id, cate_id, store_name FROM eb_store_product;

SELECT '=== 备份完成，条数 ===' AS t;
SELECT (SELECT COUNT(*) FROM bak_category_type1_20260916) AS 备份分类数,
       (SELECT COUNT(*) FROM bak_store_product_cate_20260916) AS 备份商品数;

-- 1. 建立「默认分类」一级（type=1, pid=0, path=/0/）
INSERT INTO eb_category (pid, path, name, type, url, extra, status, sort, create_time, update_time)
SELECT 0, '/0/', '默认分类', 1, '', NULL, 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT id FROM eb_category WHERE name='默认分类' AND type=1 AND pid=0) t
);

-- 2. 取得一级 ID，并建立同名子类（pid=一级ID, path=/0/<pid>/）
SET @root_id := (SELECT id FROM eb_category WHERE name='默认分类' AND type=1 AND pid=0 ORDER BY id LIMIT 1);

INSERT INTO eb_category (pid, path, name, type, url, extra, status, sort, create_time, update_time)
SELECT @root_id, CONCAT('/0/', @root_id, '/'), '默认分类', 1, '', NULL, 1, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM (SELECT id FROM eb_category WHERE name='默认分类' AND type=1 AND pid=@root_id) t
);

SET @child_id := (SELECT id FROM eb_category WHERE name='默认分类' AND type=1 AND pid=@root_id ORDER BY id LIMIT 1);

SELECT '=== 新建的分类 ===' AS t;
SELECT id, pid, name, type, path, status, sort FROM eb_category
WHERE name='默认分类' AND type=1;

-- 3. 把 7 个商品的 cate_id 改挂到「默认分类」子类
UPDATE eb_store_product
SET cate_id = CAST(@child_id AS CHAR)
WHERE is_del = 0;

SELECT '=== 商品改挂结果 ===' AS t;
SELECT id, cate_id, LEFT(store_name, 30) AS 商品名 FROM eb_store_product WHERE is_del = 0;

-- 4. 删除旧的 43 条 type=1 分类（保留刚新建的 2 条，按 id 排除，最稳妥）
DELETE FROM eb_category
WHERE type = 1
  AND id NOT IN (@root_id, @child_id);

SELECT '=== 清理后 type=1 剩余 ===' AS t;
SELECT id, pid, name, type, path, status FROM eb_category WHERE type = 1 ORDER BY pid, id;

-- 5. 清理关联引用（避免前台/装修数据指向已删分类）
DELETE FROM eb_store_product_cate;

SELECT '=== 校验：分类表总量（应仍保留菜单等非 type=1 数据）===' AS t;
SELECT type, COUNT(*) AS cnt FROM eb_category GROUP BY type ORDER BY type;

SELECT '=== 校验：商品是否都指向默认分类子类 ===' AS t;
SELECT p.id, p.cate_id, c.name AS 对应分类名
FROM eb_store_product p LEFT JOIN eb_category c ON c.id = p.cate_id
WHERE p.is_del = 0;

SELECT '=== 校验：type=1 是否只有默认分类两条 ===' AS t;
SELECT COUNT(*) AS 剩余产品分类数 FROM eb_category WHERE type = 1;
