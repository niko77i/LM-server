package com.lmserver.controller.fb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.FbPixelBmDto;
import com.lmserver.dto.response.FbPixelDto;
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

/**
 * FB Pixel 和 Pixel BM 管理控制器 — 对齐 Python fb_routes.py pixel-bms + pixels 段。
 *
 * Pixel BM (7接口):
 * <ul>
 * <li>GET /api/fb/pixel-bms/list           — 分页+搜索+BMs像素计数</li>
 * <li>POST /api/fb/pixel-bms/create        — 新建 Pixel BM</li>
 * <li>PUT /api/fb/pixel-bms/{id}           — 更新名称/备注</li>
 * <li>DELETE /api/fb/pixel-bms/{id}        — 软删除(deleted_at+status)</li>
 * <li>GET /api/fb/pixel-bms/options        — 下拉选项(过滤已删除)</li>
 * <li>GET /api/fb/pixel-bms/{bid}/pixels   — 查看BM下的像素列表</li>
 * <li>POST /api/fb/pixel-bms/{bid}/pixels  — 在BM下创建像素</li>
 * </ul>
 *
 * Pixel (5接口):
 * <ul>
 * <li>GET /api/fb/pixels/list    — 分页+搜索+JOIN BM名称</li>
 * <li>POST /api/fb/pixels/create — 创建像素</li>
 * <li>PUT /api/fb/pixels/{id}    — 更新像素名</li>
 * <li>DELETE /api/fb/pixels/{id} — 物理删除</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/fb")
@RequiredArgsConstructor
public class FbPixelController {

    @Autowired private FbPixelBmsMapper pixelBmMapper;
    @Autowired private FbPixelsMapper pixelMapper;
    @Autowired private FbBmsMapper bmsMapper;

    // ═══════ Pixel BM ═══════

    @GetMapping("/pixel-bms/list")
    public PagedResponse<FbPixelBmDto> listBms(@AuthenticationPrincipal UserPrincipal p,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search, @RequestParam(required = false) String status) {
        Page<FbPixelBmDto> pg = new Page<>(page, size);
        List<FbPixelBmDto> items = pixelBmMapper.selectFbPixelBmDtos(pg, p.getUserId(),
                search != null && !search.isBlank() ? search : null,
                status != null && !status.isBlank() ? status : "deleted");
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
    public PagedResponse<FbPixelDto> listPixels(@AuthenticationPrincipal UserPrincipal p,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Page<FbPixelDto> pg = new Page<>(page, size);
        List<FbPixelDto> items = pixelMapper.selectFbPixelDtos(pg,
                search != null && !search.isBlank() ? search : null);
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
