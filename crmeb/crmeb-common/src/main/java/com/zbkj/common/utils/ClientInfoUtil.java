package com.zbkj.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 客户端信息解析工具（浏览器 / 操作系统 / IP 归属地）
 *  +----------------------------------------------------------------------
 *  | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
 *  +----------------------------------------------------------------------
 *  | Copyright (c) 2016~2024 https://www.crmeb.com All rights reserved.
 *  +----------------------------------------------------------------------
 *  | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
 *  +----------------------------------------------------------------------
 *  | Author: CRMEB Team <admin@crmeb.com>
 *  +----------------------------------------------------------------------
 */
public class ClientInfoUtil {

    /**
     * 从 User-Agent 中解析浏览器名称及版本
     * @param userAgent UA 字符串
     * @return 如 Chrome 120 / Edge 120 / Firefox 121 / Safari 17 / 未知
     */
    public static String getBrowser(String userAgent) {
        if (StringUtils.isBlank(userAgent)) {
            return "未知";
        }
        String ua = userAgent;
        // 顺序敏感：Edge / Chrome 的 UA 里都带有 Safari 字样，必须先判断
        if (ua.contains("Edg/") || ua.contains("Edge/")) {
            return "Edge " + getVersion(ua, "Edg/", "Edge/");
        }
        if (ua.contains("OPR/") || ua.contains("Opera/")) {
            return "Opera " + getVersion(ua, "OPR/", "Opera/");
        }
        // 国产浏览器内核（均基于 Chrome）
        if (ua.contains("QQBrowser/")) {
            return "QQ浏览器 " + getVersion(ua, "QQBrowser/");
        }
        if (ua.contains("MicroMessenger/")) {
            return "微信内置浏览器 " + getVersion(ua, "MicroMessenger/");
        }
        if (ua.contains("UCBrowser/") || ua.contains("UBrowser/")) {
            return "UC浏览器 " + getVersion(ua, "UCBrowser/", "UBrowser/");
        }
        if (ua.contains("Firefox/")) {
            return "Firefox " + getVersion(ua, "Firefox/");
        }
        if (ua.contains("Chrome/")) {
            return "Chrome " + getVersion(ua, "Chrome/");
        }
        if (ua.contains("Safari/") && ua.contains("Version/")) {
            return "Safari " + getVersion(ua, "Version/");
        }
        if (ua.contains("MSIE") || ua.contains("Trident/")) {
            return "IE";
        }
        return "未知";
    }

    /**
     * 从 User-Agent 中解析操作系统
     * @param userAgent UA 字符串
     * @return 如 Windows 10/11 / macOS / Android / iOS / Linux
     */
    public static String getOs(String userAgent) {
        if (StringUtils.isBlank(userAgent)) {
            return "未知";
        }
        String ua = userAgent;
        if (ua.contains("Windows NT 10.0")) {
            return "Windows 10/11";
        }
        if (ua.contains("Windows NT 6.3")) {
            return "Windows 8.1";
        }
        if (ua.contains("Windows NT 6.2")) {
            return "Windows 8";
        }
        if (ua.contains("Windows NT 6.1")) {
            return "Windows 7";
        }
        if (ua.contains("Windows NT 6.0")) {
            return "Windows Vista";
        }
        if (ua.contains("Windows NT 5.")) {
            return "Windows XP";
        }
        if (ua.contains("Windows")) {
            return "Windows";
        }
        if (ua.contains("Android")) {
            return "Android " + getVersion(ua, "Android ");
        }
        if (ua.contains("iPhone") || ua.contains("iPad") || ua.contains("iPod")) {
            // iOS 的 UA 用下划线表示版本（如 CPU iPhone OS 17_1），展示时转成点号
            String iosVersion = getVersion(ua, "OS ").replace('_', '.').trim();
            return iosVersion.isEmpty() ? "iOS" : "iOS " + iosVersion;
        }
        if (ua.contains("Mac OS X")) {
            return "macOS " + getVersion(ua, "Mac OS X ");
        }
        if (ua.contains("Linux")) {
            return "Linux";
        }
        return "未知";
    }

    /**
     * 根据 IP 返回归属地描述
     * <p>本地/内网 IP 直接返回「本机/局域网」，公网 IP 项目未内置离线 IP 库，
     * 返回 IP 本身占位，避免引入外部付费接口依赖。</p>
     * @param ip IP 地址
     * @return 归属地
     */
    public static String getLocation(String ip) {
        if (StringUtils.isBlank(ip)) {
            return "未知";
        }
        if ("127.0.0.1".equals(ip) || "localhost".equalsIgnoreCase(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "本机";
        }
        if (isInnerIp(ip)) {
            return "局域网";
        }
        return ip;
    }

    /**
     * 判断是否为内网 IP
     */
    public static boolean isInnerIp(String ip) {
        if (StringUtils.isBlank(ip)) {
            return false;
        }
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) {
            return true;
        }
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length > 1) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    return second >= 16 && second <= 31;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 从 UA 里提取指定标识后面的版本号（取到第一个空格或分号为止）
     */
    private static String getVersion(String ua, String... keys) {
        for (String key : keys) {
            int idx = ua.indexOf(key);
            if (idx < 0) {
                continue;
            }
            int start = idx + key.length();
            int end = start;
            while (end < ua.length()) {
                char c = ua.charAt(end);
                if (c == ' ' || c == ';' || c == ')' || c == '/') {
                    break;
                }
                end++;
            }
            if (end > start) {
                return ua.substring(start, end);
            }
        }
        return "";
    }
}
