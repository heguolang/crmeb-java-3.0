package com.zbkj.common.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 隐藏运维面板审计工具
 * 操作记录写入用户主目录下隐藏目录文件（.qxs/audit.log），不落业务库。
 */
public class HiddenAuditUtil {

    private static final String DIR_NAME = ".qxs";
    private static final String FILE_NAME = "audit.log";

    private HiddenAuditUtil() {}

    /**
     * 追加一条审计记录
     * @param operator 操作人账号
     * @param action 动作标识
     * @param affected 影响行数
     * @param detail 明细
     */
    public static void log(String operator, String action, long affected, String detail) {
        BufferedWriter writer = null;
        try {
            File dir = new File(System.getProperty("user.home"), DIR_NAME);
            if (!dir.exists() && !dir.mkdirs()) {
                return;
            }
            File file = new File(dir, FILE_NAME);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            StringBuilder sb = new StringBuilder();
            sb.append(sdf.format(new Date())).append(" | ")
              .append(operator == null ? "-" : operator).append(" | ")
              .append(action).append(" | affected=").append(affected);
            if (detail != null && !detail.isEmpty()) {
                sb.append(" | ").append(detail);
            }
            sb.append(System.lineSeparator());
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8));
            writer.write(sb.toString());
            writer.flush();
        } catch (Exception e) {
            // 审计失败不影响主流程
        } finally {
            if (writer != null) {
                try { writer.close(); } catch (Exception ignore) {}
            }
        }
    }

    /** 审计文件位置（仅供排查用） */
    public static String auditFilePath() {
        return new File(System.getProperty("user.home"), DIR_NAME + File.separator + FILE_NAME).getAbsolutePath();
    }
}
