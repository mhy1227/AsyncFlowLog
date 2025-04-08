package com.asyncflow.log.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FileUtils {
    
    public static boolean createDirectories(String directoryPath) {
        try {
            Path path = Paths.get(directoryPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("创建目录成功: {}", directoryPath);
            }
            return true;
        } catch (IOException e) {
            log.error("创建目录失败: {}", directoryPath, e);
            return false;
        }
    }
    
    public static boolean writeToFile(String filePath, String content, boolean append) {
        try {
            Path path = Paths.get(filePath);
            // 确保目录存在
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
                writer.write(content);
                writer.newLine();
                return true;
            }
        } catch (IOException e) {
            log.error("写入文件失败: {}", filePath, e);
            return false;
        }
    }
    
    public static List<String> readLines(String filePath, int lineNumber, int count) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int currentLine = 0;
            
            // 跳过前面的行
            while (currentLine < lineNumber - 1 && (line = reader.readLine()) != null) {
                currentLine++;
            }
            
            // 读取指定数量的行
            while (currentLine < lineNumber + count - 1 && (line = reader.readLine()) != null) {
                if (currentLine >= lineNumber - 1) {
                    lines.add(line);
                }
                currentLine++;
            }
            
            return lines;
        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            return lines;
        }
    }
    
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("删除文件成功: {}", filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }
    
    public static long getFileSize(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return Files.size(path);
            }
            return 0;
        } catch (IOException e) {
            log.error("获取文件大小失败: {}", filePath, e);
            return 0;
        }
    }
}