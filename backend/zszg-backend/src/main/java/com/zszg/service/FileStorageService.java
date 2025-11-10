package com.zszg.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 存储文件并返回访问URL
     */
    public String storeFile(MultipartFile file, String category) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("无法存储空文件");
        }

        // 生成文件名：日期/UUID_原始文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = UUID.randomUUID().toString() + extension;
        
        // 创建目录
        Path categoryPath = Paths.get(uploadPath, category, dateDir);
        Files.createDirectories(categoryPath);

        // 存储文件
        Path targetPath = categoryPath.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 返回完整的访问URL（包含后端服务器地址）
        return String.format("http://localhost:%s/uploads/%s/%s/%s", serverPort, category, dateDir, filename);
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String fileUrl) {
        try {
            // fileUrl 格式可能是: http://localhost:8080/uploads/category/date/filename 或 /uploads/category/date/filename
            String uploadsPath;
            if (fileUrl.contains("/uploads/")) {
                uploadsPath = fileUrl.substring(fileUrl.indexOf("/uploads/") + "/uploads/".length());
            } else {
                return false;
            }
            
            Path filePath = Paths.get(uploadPath, uploadsPath);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            return false;
        }
    }
}

