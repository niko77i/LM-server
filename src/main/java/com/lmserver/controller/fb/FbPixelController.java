package com.lmserver.controller.fb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.*;
import com.lmserver.mapper.fb.*;
import com.lmserver.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/fb")
@RequiredArgsConstructor
public class FbPixelController {

    @Autowired private FbPixelBmsMapper pixelBmMapper;
    @Autowired private FbPixelsMapper pixelMapper;
    @Autowired private FbBmsMapper bmsMapper;

    // ═══════ Pixel BM ═══════

    @GetMapping("/pixel-bms/list")
    public PagedResponse<Map<String, Object>> listBms(@AuthenticationPrincipal UserPrincipal p,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search, @RequestParam(required = false) String status) {
        var qw = new LambdaQueryWrapper<FbPixelBms>().eq(FbPixelBms::getOwnerId, p.getUserId());
        if (status != null && !status.isBlank()) qw.eq(FbPixelBms::getStatus, status);
        else qw.ne(FbPixelBms::getStatus, "deleted");
        if (search != null && !search.isBlank())
            qw.and(w -> w.like(FbPixelBms::getName, search).or().like(FbPixelBms::getBmId, search));
        qw.orderByDesc(FbPixelBms::getCreatedAt);
        var pg = pixelBmMapper.selectPage(new Page<>(page, size), qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (FbPixelBms bm : pg.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", bm.getId()); item.put("name", bm.getName()); item.put("bm_id", bm.getBmId());
            item.put("status", bm.getStatus()); item.put("note", bm.getNote());
            item.put("pixel_count", pixelMapper.selectCount(
                    new LambdaQueryWrapper<FbPixels>().eq(FbPixels::getPixelBmId, bm.getId())));
            items.add(item);
        }
        return PagedResponse.of(items, pg.getTotal(), page, size);
    }

    @PostMapping("/pixel-bms/create") public ApiResponse<FbPixelBms> createBm(
            @AuthenticationPrincipal UserPrincipal p, @RequestBody Map<String, String> body) {
        FbPixelBms bm = new FbPixelBms(); bm.setName(body.get("name")); bm.setBmId(body.get("bm_id"));
        bm.setNote(body.get("note")); bm.setStatus("normal"); bm.setOwnerId(p.getUserId());
        bm.setCreatedAt(LocalDateTime.now()); pixelBmMapper.insert(bm);
        return ApiResponse.ok(bm);
    }

    @PutMapping("/pixel-bms/{id}") public ApiResponse<FbPixelBms> updateBm(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        FbPixelBms bm = pixelBmMapper.selectById(id);
        if (bm == null) return ApiResponse.fail("不存在");
        if (body.containsKey("name")) bm.setName(body.get("name"));
        if (body.containsKey("note")) bm.setNote(body.get("note"));
        pixelBmMapper.updateById(bm);
        return ApiResponse.ok(bm);
    }

    @DeleteMapping("/pixel-bms/{id}") public ApiResponse<Void> deleteBm(@PathVariable Long id) {
        FbPixelBms bm = pixelBmMapper.selectById(id);
        if (bm != null) { bm.setDeletedAt(LocalDateTime.now()); bm.setStatus("deleted"); pixelBmMapper.updateById(bm); }
        return ApiResponse.ok();
    }

    @GetMapping("/pixel-bms/options") public ApiResponse<?> bmOptions(@AuthenticationPrincipal UserPrincipal p) {
        var list = pixelBmMapper.selectList(new LambdaQueryWrapper<FbPixelBms>()
                .eq(FbPixelBms::getOwnerId, p.getUserId()).ne(FbPixelBms::getStatus, "deleted"));
        return ApiResponse.ok(list.stream().map(b -> Map.of("id", b.getId(), "name", b.getName(), "bm_id", b.getBmId())).toList());
    }

    @GetMapping("/pixel-bms/{bid}/pixels") public ApiResponse<List<FbPixels>> pixelsByBm(@PathVariable Long bid) {
        return ApiResponse.ok(pixelMapper.selectList(new LambdaQueryWrapper<FbPixels>().eq(FbPixels::getPixelBmId, bid)));
    }

    @PostMapping("/pixel-bms/{bid}/pixels") public ApiResponse<FbPixels> addPixelToBm(
            @PathVariable Long bid, @RequestBody Map<String, String> body) {
        FbPixels p = new FbPixels(); p.setPixelBmId(bid);
        p.setPixelName(body.get("pixel_name")); p.setPixelId(body.get("pixel_id"));
        pixelMapper.insert(p);
        return ApiResponse.ok(p);
    }

    // ═══════ Pixel ═══════

    @GetMapping("/pixels/list")
    public PagedResponse<Map<String, Object>> listPixels(@AuthenticationPrincipal UserPrincipal p,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        var qw = new LambdaQueryWrapper<FbPixels>();
        if (search != null && !search.isBlank())
            qw.and(w -> w.like(FbPixels::getPixelName, search).or().like(FbPixels::getPixelId, search));
        var pg = pixelMapper.selectPage(new Page<>(page, size), qw);

        List<Map<String, Object>> items = new ArrayList<>();
        for (FbPixels px : pg.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", px.getId()); item.put("pixel_name", px.getPixelName()); item.put("pixel_id", px.getPixelId());
            item.put("pixel_bm_id", px.getPixelBmId());
            FbPixelBms bm = pixelBmMapper.selectById(px.getPixelBmId());
            item.put("bm_name", bm != null ? bm.getName() : "");
            item.put("bm_bm_id", bm != null ? bm.getBmId() : "");
            items.add(item);
        }
        return PagedResponse.of(items, pg.getTotal(), page, size);
    }

    @PostMapping("/pixels/create") public ApiResponse<FbPixels> createPixel(@RequestBody Map<String, String> body) {
        FbPixels p = new FbPixels();
        p.setPixelBmId(Long.valueOf(body.get("pixel_bm_id")));
        p.setPixelName(body.get("pixel_name")); p.setPixelId(body.get("pixel_id"));
        pixelMapper.insert(p);
        return ApiResponse.ok(p);
    }

    @PutMapping("/pixels/{id}") public ApiResponse<FbPixels> updatePixel(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        FbPixels p = pixelMapper.selectById(id);
        if (p == null) return ApiResponse.fail("不存在");
        if (body.containsKey("pixel_name")) p.setPixelName(body.get("pixel_name"));
        pixelMapper.updateById(p);
        return ApiResponse.ok(p);
    }

    @DeleteMapping("/pixels/{id}") public ApiResponse<Void> deletePixel(@PathVariable Long id) {
        pixelMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
