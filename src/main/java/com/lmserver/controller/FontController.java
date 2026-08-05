package com.lmserver.controller;
import com.lmserver.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

@Slf4j @RestController @RequestMapping("/api/fonts")
public class FontController {
    static final Path DIR = Paths.get("fonts"), RECENT = DIR.resolve(".recent");
    static final Set<String> EXTS = Set.of(".ttf",".otf",".ttc",".woff",".woff2");

    @GetMapping("/list") public ApiResponse<List<Map<String,Object>>> list() { return ApiResponse.ok(scan()); }

    List<Map<String,Object>> scan() {
        List<Map<String,Object>> f=new ArrayList<>(); Set<String> s=new HashSet<>(); List<String> r=recent();
        try { if(Files.isDirectory(DIR)) Files.list(DIR).filter(p->EXTS.stream().anyMatch(p.getFileName().toString().toLowerCase()::endsWith))
            .sorted((a,b)->{boolean ra=r.contains(strip(a)),rb=r.contains(strip(b)); return ra==rb?a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString()):ra?-1:1;})
            .forEach(p->{String id=strip(p); if(s.add(id)) f.add(Map.of("id",id,"name",id,"source","user"));}); }
        catch(Exception e) { log.warn("scan err: {}",e.getMessage()); }
        Path sys=Paths.get(System.getenv("SystemRoot"),"Fonts");
        for(String n:new String[]{"simhei","msyh","simsun","arial","kaiu","fangsong"})
            if(s.add(n)&&(Files.exists(sys.resolve(n+".ttf"))||Files.exists(sys.resolve(n+".ttc")))) f.add(Map.of("id",n,"name",n,"source","system"));
        return f;
    }

    @PostMapping("/import") public ApiResponse<Map<String,Object>> imp(@RequestBody Map<String,Object> b) {
        @SuppressWarnings("unchecked") List<String> srcs=(List<String>)b.getOrDefault("sources",List.of()); int n=0;
        try { Files.createDirectories(DIR); for(String sp:srcs) { Path s=Paths.get(sp.replace("\\","/"));
            if(Files.isDirectory(s)) for(Path p:Files.walk(s).filter(Files::isRegularFile).toList()) if(isFont(p)) n+=cp(p);
            else if(Files.isRegularFile(s)&&isFont(s)) n+=cp(s); } }
        catch(Exception e) { return ApiResponse.fail("err: "+e.getMessage()); }
        return ApiResponse.ok(Map.of("imported",n,"fonts",scan()));
    }

    @PostMapping("/upload") public ApiResponse<Map<String,Object>> up(@RequestParam("files") List<MultipartFile> fs) {
        int n=0; try { Files.createDirectories(DIR);
            for(MultipartFile f:fs) { if(f.getOriginalFilename()==null||EXTS.stream().noneMatch(f.getOriginalFilename().toLowerCase()::endsWith)) continue;
                Path d=DIR.resolve(f.getOriginalFilename()); if(!Files.exists(d)) Files.copy(f.getInputStream(),d); n++; } }
        catch(Exception e) { return ApiResponse.fail("err: "+e.getMessage()); }
        return ApiResponse.ok(Map.of("imported",n,"fonts",scan()));
    }

    @GetMapping("/preview") public ResponseEntity<byte[]> pv(@RequestParam(defaultValue="simhei") String font) {
        try { Path fp=find(font); if(fp==null) return ResponseEntity.notFound().build();
            Font df=Font.createFont(Font.TRUETYPE_FONT,fp.toFile()).deriveFont(64f),bf=df.deriveFont(26f),cf=df.deriveFont(16f);
            BufferedImage im=new BufferedImage(560,210,BufferedImage.TYPE_INT_RGB); Graphics2D g=im.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(254,252,249)); g.fillRect(0,0,560,210); g.setColor(new Color(212,133,10)); g.fillRect(0,0,560,3);
            g.setFont(df); g.setColor(new Color(30,27,24)); g.drawString("字体样张",24,60);
            g.setFont(bf); g.setColor(new Color(92,86,79)); g.drawString("ABCDEFGHIJKLM  abcdefghijklm  0123456789",24,110);
            g.setColor(new Color(218,212,202)); g.drawLine(24,128,536,128);
            g.setColor(new Color(60,55,48)); g.drawString("永和九年岁在癸丑暮春之初会于会稽山阴之兰亭",24,158);
            g.setFont(cf); g.setColor(new Color(160,155,148)); g.drawString(font+"  64px/26px/16px",24,195); g.dispose();
            ByteArrayOutputStream bo=new ByteArrayOutputStream(); ImageIO.write(im,"PNG",bo); return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(bo.toByteArray()); }
        catch(Exception e) { return ResponseEntity.internalServerError().build(); }
    }

    @GetMapping("/file/{id}") public ResponseEntity<FileSystemResource> file(@PathVariable String id) {
        Path p=find(id); if(p==null) return ResponseEntity.notFound().build();
        String e=p.getFileName().toString().toLowerCase(),m=e.endsWith(".ttf")||e.endsWith(".otf")?"font/"+e.substring(1):e.endsWith(".ttc")?"font/collection":"application/octet-stream";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE,m).header(HttpHeaders.CACHE_CONTROL,"max-age=3600").body(new FileSystemResource(p));
    }
    @GetMapping("/file") public ResponseEntity<FileSystemResource> file2(@RequestParam String font) { return file(font); }

    @PostMapping("/mark-used") public ApiResponse<Void> mark(@RequestBody Map<String,String> b) {
        String f=b.getOrDefault("font","").trim(); if(f.isEmpty()) return ApiResponse.ok();
        try { Files.createDirectories(DIR); List<String> r=recent(); r.remove(f); r.add(0,f); if(r.size()>20) r=r.subList(0,20); Files.write(RECENT,r); }
        catch(Exception e) { log.warn("mark err: {}",e.getMessage()); } return ApiResponse.ok();
    }

    Path find(String id) { try { if(Files.isDirectory(DIR)) for(String e:EXTS) { Path p=DIR.resolve(id+e); if(Files.isRegularFile(p)) return p; } } catch(Exception x) {}
        Path s=Paths.get(System.getenv("SystemRoot"),"Fonts"); for(String e:EXTS) { Path p=s.resolve(id+e); if(Files.isRegularFile(p)) return p; } return null; }
    String strip(Path p) { String n=p.getFileName().toString(); int d=n.lastIndexOf('.'); return d>0?n.substring(0,d):n; }
    boolean isFont(Path p) { return EXTS.stream().anyMatch(p.getFileName().toString().toLowerCase()::endsWith); }
    int cp(Path s) throws IOException { Path d=DIR.resolve(s.getFileName()); if(Files.exists(d)) return 0; Files.copy(s,d); return 1; }
    List<String> recent() { try { return Files.isRegularFile(RECENT)?new ArrayList<>(Files.readAllLines(RECENT)):new ArrayList<>(); } catch(Exception x) { return new ArrayList<>(); } }
}