package com.asyncflow.log.maintenance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 日志留存清理定时任务。
 * - 每日在配置的 cron 时间触发；
 * - 仅清理严格早于“今天”的历史日志文件；
 * - 默认关闭，通过 async.log.maintenance.enabled 开启。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "async.log.maintenance", name = "enabled", havingValue = "true")
public class LogRetentionScheduler {

    /**
     * 日志目录（与 FileAppender 默认一致）。
     */
    @Value("${async.log.file.path:logs}")
    private String logDir;

    /**
     * 保留天数（含今天）；早于该阈值的历史文件将被清理（删除）。
     */
    @Value("${async.log.retention.days:7}")
    private int retentionDays;

    /**
     * 是否启用归档（在删除前，对较旧但未到删除阈值的文件执行归档）。
     */
    @Value("${async.log.archive.enabled:false}")
    private boolean archiveEnabled;

    /**
     * 归档目录（默认 logs/archive）。
     */
    @Value("${async.log.archive.dir:logs/archive}")
    private String archiveDir;

    /**
     * 归档阈值天数（含今天）；早于该阈值的历史文件将被归档。
     * 注意：应小于保留天数（例如 archive.days=3, retention.days=7）。
     */
    @Value("${async.log.archive.days:3}")
    private int archiveDays;

    /**
     * 归档压缩格式：当前仅支持 zip。
     */
    @Value("${async.log.archive.compress:zip}")
    private String archiveCompress;

    private static final Pattern FILE_PATTERN = Pattern.compile("^async-log-(\\d{4}-\\d{2}-\\d{2})\\.log$");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 每日定时触发（默认 02:05, Asia/Shanghai）。
     * 可通过配置覆盖：async.log.maintenance.cron / async.log.maintenance.timezone
     */
    @Scheduled(
            cron = "${async.log.maintenance.cron:0 5 2 * * ?}",
            zone = "${async.log.maintenance.timezone:Asia/Shanghai}"
    )
    public void cleanOldLogs() {
        Path base = Paths.get(logDir);
        if (!Files.exists(base)) {
            log.debug("日志目录不存在，跳过清理: {}", logDir);
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate deleteCutoff = today.minusDays(Math.max(retentionDays, 0));
        LocalDate archiveCutoff = today.minusDays(Math.max(archiveDays, 0));
        AtomicInteger archived = new AtomicInteger(0);
        AtomicInteger deleted = new AtomicInteger(0);
        AtomicInteger skipped = new AtomicInteger(0);
        AtomicInteger errors = new AtomicInteger(0);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(base, path -> Files.isRegularFile(path))) {
            for (Path p : stream) {
                String fileName = p.getFileName().toString();
                Matcher m = FILE_PATTERN.matcher(fileName);
                if (!m.matches()) {
                    skipped.incrementAndGet();
                    continue; // 非默认命名的文件不处理
                }

                LocalDate fileDate;
                try {
                    fileDate = LocalDate.parse(m.group(1), DATE_FMT);
                } catch (DateTimeParseException ex) {
                    skipped.incrementAndGet();
                    continue;
                }

                // 仅处理严格早于今天的历史文件
                if (!fileDate.isBefore(today)) {
                    skipped.incrementAndGet();
                    continue;
                }

                // 优先处理删除：早于或等于删除阈值（达到留存策略上限）
                if (!fileDate.isAfter(deleteCutoff)) {
                    try {
                        Files.deleteIfExists(p);
                        deleted.incrementAndGet();
                        log.info("已清理历史日志文件: {}", p.toAbsolutePath());
                    } catch (IOException ioe) {
                        errors.incrementAndGet();
                        log.warn("删除历史日志文件失败: {} - {}", p.toAbsolutePath(), ioe.getMessage());
                    }
                    continue;
                }

                // 归档：介于归档阈值与删除阈值之间的文件（更早的已删除，更新的暂不处理）
                if (archiveEnabled && !fileDate.isAfter(archiveCutoff)) {
                    try {
                        Path archivedPath = archiveFile(p);
                        if (archivedPath != null) {
                            archived.incrementAndGet();
                            // 归档成功后删除源文件
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException del) {
                                log.warn("归档后删除源文件失败: {} - {}", p.toAbsolutePath(), del.getMessage());
                            }
                            log.info("已归档历史日志文件: {} -> {}", p.toAbsolutePath(), archivedPath.toAbsolutePath());
                        } else {
                            skipped.incrementAndGet();
                        }
                    } catch (Exception ex) {
                        errors.incrementAndGet();
                        log.warn("归档历史日志文件失败: {} - {}", p.toAbsolutePath(), ex.getMessage());
                    }
                    continue;
                }

                // 未到归档阈值或未启用归档：跳过
                skipped.incrementAndGet();
            }
        } catch (IOException e) {
            log.warn("扫描日志目录失败: {} - {}", base.toAbsolutePath(), e.getMessage());
            return;
        }

        log.info("日志清理完成，归档: {}，删除: {}，跳过: {}，错误: {}，目录: {}，归档天数: {}，保留天数: {}",
                archived.get(), deleted.get(), skipped.get(), errors.get(), base.toAbsolutePath(), archiveDays, retentionDays);
    }

    /**
     * 将指定日志文件归档到归档目录（当前以 zip 压缩）。
     * @param source 待归档的源文件
     * @return 归档后的目标文件路径；若失败返回 null
     * @throws IOException IO 异常
     */
    private Path archiveFile(Path source) throws IOException {
        if (!"zip".equalsIgnoreCase(archiveCompress)) {
            // 仅支持 zip，其它值直接跳过
            return null;
        }
        Path archiveBase = Paths.get(archiveDir);
        if (!Files.exists(archiveBase)) {
            Files.createDirectories(archiveBase);
        }
        String zipName = source.getFileName().toString() + ".zip";
        Path target = archiveBase.resolve(zipName);
        if (Files.exists(target)) {
            // 已存在同名归档，避免重复
            return target;
        }

        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(target));
             java.io.InputStream in = Files.newInputStream(source)) {
            java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(source.getFileName().toString());
            zos.putNextEntry(entry);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
        }
        return target;
    }
}
