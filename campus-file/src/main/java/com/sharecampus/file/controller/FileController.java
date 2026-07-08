package com.sharecampus.file.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.file.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.io.OutputStream;

@RestController @RequestMapping("/api/v1/file") @RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws Exception {
        return Result.success(fileService.upload(file));
    }

    @GetMapping("/{objectName}")
    public void download(@PathVariable String objectName, HttpServletResponse response) throws Exception {
        try (InputStream is = fileService.download(objectName);
             OutputStream os = response.getOutputStream()) {
            response.setContentType("application/octet-stream");
            is.transferTo(os);
        }
    }

    /** 获取前端直传预签名URL */
    @PostMapping("/sts-token")
    public Result<String> stsToken(@RequestParam String objectName) throws Exception {
        return Result.success(fileService.presignedUploadUrl(objectName));
    }
}
