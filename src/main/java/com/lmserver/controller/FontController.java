package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字体管理控制器 — /api/fonts/*，上传/预览/删除字体文件。
 * Phase 5: 字体目录暂用项目 fonts/ 目录。
 */
@RestController
@RequestMapping("/api/fonts")
public class FontController {

    private static final String FONT_DIR = "fonts";
    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public ApiResponse<List<String>> list() {
        File dir = new File(FONT_DIR);
        String[] files = dir.list((d, name) -> name.endsWith(".ttf") || name.endsWith(".otf") || name.endsWith(".woff2"));
        return ApiResponse.ok(files != null ? Arrays.asList(files) : List.of());
    }
    @PostMapping("/upload")
    public ApiResponse<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            File dir = new File(FONT_DIR);
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(dir, file.getOriginalFilename());
            file.transferTo(dest);
            return ApiResponse.ok("上传成功: " + file.getOriginalFilename());
        } catch (Exception e) {
            return ApiResponse.fail("上传失败: " + e.getMessage());
        }
    }
    @DeleteMapping("/{name}")
    /** 删除记录 */
    public ApiResponse<Void> delete(@PathVariable String name) {
        File f = new File(FONT_DIR, name);
        if (f.exists()) f.delete();
        return ApiResponse.ok();
    }

    @GetMapping("/preview/{name}")
    public void preview(@PathVariable String name, HttpServletResponse resp) throws IOException {
        File f = new File(FONT_DIR, name);
        if (!f.exists()) { resp.sendError(404); return; }
        resp.setContentType("font/ttf");
        try (FileInputStream in = new FileInputStream(f); OutputStream out = resp.getOutputStream()) {
            in.transferTo(out);
        }
    }

    @GetMapping("/download/{name}")
    public void download(@PathVariable String name, HttpServletResponse resp) throws IOException {
        File f = new File(FONT_DIR, name);
        if (!f.exists()) { resp.sendError(404); return; }
        resp.setContentType("application/octet-stream");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + name + "\"");
        try (FileInputStream in = new FileInputStream(f); OutputStream out = resp.getOutputStream()) {
            in.transferTo(out);
        }
    }

    @PostMapping("/batch-upload")
    public ApiResponse<Integer> batchUpload(@RequestParam("files") List<MultipartFile> files) {
        int c = 0; for (MultipartFile f : files) {
            try { File d = new File(FONT_DIR); if(!d.exists()) d.mkdirs();
                f.transferTo(new File(d, f.getOriginalFilename())); c++; } catch(Exception ignored){}
        }
        return ApiResponse.ok(c);
    }
}
