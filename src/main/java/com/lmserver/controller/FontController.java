package com.lmserver.controller;

import com.lmserver.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;
import java.util.List;

/**
 * 字体管理控制器 — 对齐 Python main.py fonts_* 系列接口。
 *
 * <h3>字体存储路径</h3>
 * 部署时自动解析为 jar 包同级目录下的 fonts/ 子目录。
 * 开发时回退到当前工作目录下的 fonts/。
 *
 * <ul>
 * <li>GET /api/fonts/list       — 扫描字体目录 + Windows 系统字体</li>
 * <li>POST /api/fonts/import    — 从本地磁盘路径导入字体文件</li>
 * <li>POST /api/fonts/upload    — HTTP multipart 上传字体文件</li>
 * <li>GET /api/fonts/preview    — 生成字体标本卡 PNG 预览图</li>
 * <li>GET /api/fonts/file/{id}  — 提供 Web 字体文件（@font-face）</li>
 * <li>GET /api/fonts/file?font= — 同上（兼容旧版参数名）</li>
 * <li>POST /api/fonts/mark-used — 标记最近使用，影响列表排序</li>
 * </ul>
 *
 * 支持格式：.ttf / .otf / .ttc / .woff / .woff2
 */
@Slf4j
@RestController
@RequestMapping("/api/fonts")
public class FontController {

    /** 支持的字体文件扩展名 */
    static final Set<String> EXTS = Set.of(".ttf", ".otf", ".ttc", ".woff", ".woff2");

    /**
     * 解析字体目录路径。
     * 部署环境（jar 包运行时）: jar 同级目录下的 fonts/
     * 开发环境（IDE/mvn spring-boot:run）: 当前工作目录下的 fonts/
     */
    private Path fontsDir() {
        try {
            ApplicationHome home = new ApplicationHome(FontController.class);
            Path jarOrClasses = home.getSource() != null ? home.getSource().toPath() : null;
            if (jarOrClasses != null) {
                // jar:file:///.../lm-server-0.1.0-SNAPSHOT.jar → 取 jar 所在目录
                Path base = Files.isRegularFile(jarOrClasses) ? jarOrClasses.getParent() : jarOrClasses;
                if (base != null) return base.resolve("fonts");
            }
        } catch (Exception e) {
            log.warn("解析 JAR 路径失败，回退到工作目录: {}", e.getMessage());
        }
        return Paths.get("fonts");
    }

    // ═══════════ 字体列表 ═══════════

    /**
     * 返回所有可用字体 — 用户导入字体 + Windows 系统字体。
     * 排序规则：最近使用的字体排最前面，其余按文件名排序。
     */
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(scan());
    }

    /** 扫描 fonts/ 目录及系统字体目录，构建字体列表 */
    private List<Map<String, Object>> scan() {
        List<Map<String, Object>> fonts = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        List<String> recent = recent();

        // 1. 用户导入字体（fonts/ 目录）
        Path dir = fontsDir();
        try {
            if (Files.isDirectory(dir)) {
                Files.list(fontsDir())
                    .filter(p -> EXTS.stream().anyMatch(p.getFileName().toString().toLowerCase()::endsWith))
                    .sorted((a, b) -> {
                        boolean ra = recent.contains(strip(a)), rb = recent.contains(strip(b));
                        return ra == rb
                            ? a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString())
                            : ra ? -1 : 1;  // 最近使用的排前面
                    })
                    .forEach(p -> {
                        String id = strip(p);
                        if (seen.add(id))
                            fonts.add(Map.of("id", id, "name", id, "source", "user"));
                    });
            }
        } catch (Exception e) {
            log.warn("扫描字体目录失败: {}", e.getMessage());
        }

        // 2. Windows 系统字体
        Path sysDir = Paths.get(System.getenv("SystemRoot"), "Fonts");
        for (String name : new String[]{"simhei", "msyh", "simsun", "arial", "kaiu", "fangsong"}) {
            if (seen.add(name)
                    && (Files.exists(sysDir.resolve(name + ".ttf")) || Files.exists(sysDir.resolve(name + ".ttc"))))
                fonts.add(Map.of("id", name, "name", name, "source", "system"));
        }
        return fonts;
    }

    // ═══════════ 导入 ═══════════

    /**
     * 从本地磁盘路径导入字体文件。
     * 请求体: {"sources": ["C:/path/to/font.ttf", "D:/fonts/"]}
     * 支持文件路径和目录路径，目录将递归扫描。
     */
    @PostMapping("/import")
    public ApiResponse<Map<String, Object>> importFonts(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> sources = (List<String>) body.getOrDefault("sources", List.of());
        int imported = 0;
        try {
            Files.createDirectories(fontsDir());
            for (String sp : sources) {
                Path src = Paths.get(sp.replace("\\", "/"));
                if (Files.isDirectory(src)) {
                    // 递归扫描目录
                    for (Path p : Files.walk(src).filter(Files::isRegularFile).toList())
                        if (isFont(p)) imported += copyIfNew(p);
                } else if (Files.isRegularFile(src) && isFont(src)) {
                    imported += copyIfNew(src);
                }
            }
        } catch (Exception e) {
            return ApiResponse.fail("导入失败: " + e.getMessage());
        }
        return ApiResponse.ok(Map.of("imported", imported, "fonts", scan()));
    }

    /**
     * HTTP 上传字体文件（multipart/form-data）。
     * 字段名: files — 支持多文件同时上传。
     */
    @PostMapping("/upload")
    public ApiResponse<Map<String, Object>> upload(@RequestParam("files") List<MultipartFile> files) {
        int imported = 0;
        try {
            Files.createDirectories(fontsDir());
            for (MultipartFile f : files) {
                if (f.getOriginalFilename() == null) continue;
                if (EXTS.stream().noneMatch(f.getOriginalFilename().toLowerCase()::endsWith)) continue;
                Path dst = fontsDir().resolve(f.getOriginalFilename());
                if (!Files.exists(dst)) Files.copy(f.getInputStream(), dst);
                imported++;
            }
        } catch (Exception e) {
            return ApiResponse.fail("上传失败: " + e.getMessage());
        }
        return ApiResponse.ok(Map.of("imported", imported, "fonts", scan()));
    }

    // ═══════════ 预览 ═══════════

    /**
     * 生成字体标本卡预览图（PNG 560x210）。
     * 版面布局：顶部橙色强调条 + 大字中文 + 英文/数字/符号 + 分隔线 + 兰亭序 + 底部标签。
     * 与 Python Pillow 生成的预览完全对齐。
     */
    @GetMapping("/preview")
    public ResponseEntity<byte[]> preview(@RequestParam(defaultValue = "simhei") String font) {
        try {
            Path fp = find(font);
            if (fp == null) return ResponseEntity.notFound().build();

            Font display = Font.createFont(Font.TRUETYPE_FONT, fp.toFile()).deriveFont(64f);
            Font body = display.deriveFont(26f);
            Font caption = display.deriveFont(16f);

            BufferedImage img = new BufferedImage(560, 210, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // 暖白底色
            g.setColor(new Color(254, 252, 249));
            g.fillRect(0, 0, 560, 210);
            // 顶部强调色条
            g.setColor(new Color(212, 133, 10));
            g.fillRect(0, 0, 560, 3);
            // 大字中文标题
            g.setFont(display);
            g.setColor(new Color(30, 27, 24));
            g.drawString("字体样张", 24, 60);
            // 英文 + 数字校验行
            g.setFont(body);
            g.setColor(new Color(92, 86, 79));
            g.drawString("ABCDEFGHIJKLM  abcdefghijklm  0123456789", 24, 110);
            // 分隔线
            g.setColor(new Color(218, 212, 202));
            g.drawLine(24, 128, 536, 128);
            // 中文脚本展示行
            g.setColor(new Color(60, 55, 48));
            g.drawString("永和九年岁在癸丑暮春之初会于会稽山阴之兰亭", 24, 158);
            // 底部标签栏
            g.setFont(caption);
            g.setColor(new Color(160, 155, 148));
            g.drawString(font + "  64px/26px/16px", 24, 195);

            g.dispose();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(img, "PNG", bos);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bos.toByteArray());

        } catch (Exception e) {
            log.error("字体预览失败: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ═══════════ 文件服务 ═══════════

    /**
     * 提供 Web 字体文件 — 供前端 CSS @font-face 使用。
     * MIME type: font/ttf, font/otf, font/woff, font/collection
     * Cache-Control: max-age=3600
     */
    @GetMapping("/file/{id}")
    public ResponseEntity<FileSystemResource> fontFile(@PathVariable String id) {
        Path p = find(id);
        if (p == null) return ResponseEntity.notFound().build();

        String ext = p.getFileName().toString().toLowerCase();
        String mime = ext.endsWith(".ttf") || ext.endsWith(".otf") ? "font/" + ext.substring(1)
                : ext.endsWith(".woff") || ext.endsWith(".woff2") ? "font/" + ext.substring(1)
                : ext.endsWith(".ttc") ? "font/collection"
                : "application/octet-stream";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, mime)
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(new FileSystemResource(p));
    }

    /** 兼容旧版: /api/fonts/file?font=xxx */
    @GetMapping("/file")
    public ResponseEntity<FileSystemResource> fontFileLegacy(@RequestParam String font) {
        return fontFile(font);
    }

    // ═══════════ 标记使用 ═══════════

    /**
     * 标记字体为最近使用 — 影响 fonts/list 排序（最近使用的排最前面）。
     * 请求体: {"font": "simhei"}
     * 最多保留 20 条最近记录。
     */
    @PostMapping("/mark-used")
    public ApiResponse<Void> markUsed(@RequestBody Map<String, String> body) {
        String font = body.getOrDefault("font", "").trim();
        if (font.isEmpty()) return ApiResponse.ok();
        try {
            Files.createDirectories(fontsDir());
            List<String> recent = recent();
            recent.remove(font);
            recent.add(0, font);
            if (recent.size() > 20) recent = recent.subList(0, 20);
            Files.write(fontsDir().resolve(".recent"), recent);
        } catch (Exception e) {
            log.warn("标记字体失败: {}", e.getMessage());
        }
        return ApiResponse.ok();
    }

    // ═══════════ 辅助方法 ═══════════

    /** 根据字体 ID 查找完整路径，先查用户字体再查系统字体 */
    Path find(String id) {
        // 用户字体
        Path dir = fontsDir();
        try {
            if (Files.isDirectory(dir))
                for (String ext : EXTS) {
                    Path p = fontsDir().resolve(id + ext);
                    if (Files.isRegularFile(p)) return p;
                }
        } catch (Exception ignored) {}
        // 系统字体
        Path sys = Paths.get(System.getenv("SystemRoot"), "Fonts");
        for (String ext : EXTS) {
            Path p = sys.resolve(id + ext);
            if (Files.isRegularFile(p)) return p;
        }
        return null;
    }

    /** 去掉文件扩展名，返回字体 ID */
    String strip(Path p) {
        String n = p.getFileName().toString();
        int d = n.lastIndexOf('.');
        return d > 0 ? n.substring(0, d) : n;
    }

    /** 判断文件是否为支持的字体格式 */
    boolean isFont(Path p) {
        return EXTS.stream().anyMatch(p.getFileName().toString().toLowerCase()::endsWith);
    }

    /** 复制文件到 fonts/ 目录（已存在则跳过），返回 1=新增 0=跳过 */
    int copyIfNew(Path src) throws IOException {
        Path dst = fontsDir().resolve(src.getFileName());
        if (Files.exists(dst)) return 0;
        Files.copy(src, dst);
        return 1;
    }

    /** 读取最近使用列表 */
    List<String> recent() {
        try {
            return Files.isRegularFile(fontsDir().resolve(".recent"))
                    ? new ArrayList<>(Files.readAllLines(fontsDir().resolve(".recent")))
                    : new ArrayList<>();
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }
}
