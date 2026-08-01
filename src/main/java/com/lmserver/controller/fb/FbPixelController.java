package com.lmserver.controller.fb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.fb.FbPixelBms;
import com.lmserver.entity.fb.FbPixels;
import com.lmserver.mapper.fb.FbPixelBmsMapper;
import com.lmserver.mapper.fb.FbPixelsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FB Pixel 管理控制器 — /api/fb/pixels/* 和 /api/fb/pixel-bms/*。
 */
@RestController
@RequestMapping("/api/fb")
@RequiredArgsConstructor
public class FbPixelController {

    private final FbPixelBmsMapper pixelBmMapper;
    private final FbPixelsMapper pixelMapper;

    // ──── Pixel BM ────
    @GetMapping("/pixel-bms/list")
    /** BM 列表查询 — 支持名称/ID搜索和状态筛选 */
    public ApiResponse<List<FbPixelBms>> listBms() {
        return ApiResponse.ok(pixelBmMapper.selectList(null));
    }
    @PostMapping("/pixel-bms/create")
    /** 创建 BM — 新建商务管理平台记录 */
    public ApiResponse<FbPixelBms> createBm(@RequestBody Map<String, String> body) {
        FbPixelBms bm = new FbPixelBms();
        bm.setName(body.get("name")); bm.setBmId(body.get("bm_id"));
        bm.setNote(body.get("note")); bm.setStatus("normal");
        pixelBmMapper.insert(bm);
        return ApiResponse.ok(bm);
    }
    @PutMapping("/pixel-bms/{id}")
    /** 更新 BM — 可改名和备注 */
    public ApiResponse<FbPixelBms> updateBm(@PathVariable Long id, @RequestBody Map<String, String> body) {
        FbPixelBms bm = pixelBmMapper.selectById(id);
        if (bm == null) return ApiResponse.fail("不存在");
        if (body.containsKey("name")) bm.setName(body.get("name"));
        if (body.containsKey("note")) bm.setNote(body.get("note"));
        pixelBmMapper.updateById(bm);
        return ApiResponse.ok(bm);
    }
    @DeleteMapping("/pixel-bms/{id}")
    /** 删除 BM — 软删除，记录删除时间 */
    public ApiResponse<Void> deleteBm(@PathVariable Long id) {
        FbPixelBms bm = pixelBmMapper.selectById(id);
        if (bm != null) { bm.setStatus("deleted"); pixelBmMapper.updateById(bm); }
        return ApiResponse.ok();
    }

    // ──── Pixel ────
    @GetMapping("/pixels/list")
    public ApiResponse<List<FbPixels>> listPixels() {
        return ApiResponse.ok(pixelMapper.selectList(null));
    }
    @PostMapping("/pixels/create")
    public ApiResponse<FbPixels> createPixel(@RequestBody Map<String, String> body) {
        FbPixels p = new FbPixels();
        p.setPixelBmId(Long.valueOf(body.get("pixel_bm_id")));
        p.setPixelName(body.get("pixel_name")); p.setPixelId(body.get("pixel_id"));
        pixelMapper.insert(p);
        return ApiResponse.ok(p);
    }
    @DeleteMapping("/pixels/{id}")
    public ApiResponse<Void> deletePixel(@PathVariable Long id) {
        pixelMapper.deleteById(id);
        return ApiResponse.ok();
    }
}
