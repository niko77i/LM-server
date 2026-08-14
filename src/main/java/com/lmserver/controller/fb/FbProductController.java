package com.lmserver.controller.fb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.*;
import com.lmserver.mapper.fb.*;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.FbService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/fb/products")
@RequiredArgsConstructor
public class FbProductController {

    private final FbService fbService;
    @Autowired private FbProductsMapper productsMapper;
    @Autowired private FbProductBmsMapper productBmsMapper;
    @Autowired private FbProductRunnersMapper productRunnersMapper;
    @Autowired private FbLinesMapper linesMapper;
    @Autowired private FbBmsMapper bmsMapper;

    @GetMapping("/list")
    public PagedResponse<com.lmserver.dto.response.FbProductDto> list(@AuthenticationPrincipal UserPrincipal p,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search, @RequestParam(required = false) String region,
            @RequestParam(required = false) String status, @RequestParam(required = false) Boolean archived) {
        return fbService.listProducts(p.getUserId(), page, size, search, region);
    }

    @GetMapping("/options") public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal p) {
        return ApiResponse.ok(fbService.productOptions(p.getUserId()));
    }

    /** runner-products — 返回当前用户作为在跑人员的产品及对应线 */
    @GetMapping("/runner-products")
    public ApiResponse<List<Map<String, Object>>> runnerProducts(@AuthenticationPrincipal UserPrincipal p) {
        var prs = productRunnersMapper.selectList(
                new LambdaQueryWrapper<FbProductRunners>().eq(FbProductRunners::getUserId, p.getUserId()));
        List<Map<String, Object>> result = new ArrayList<>();
        for (var pr : prs) {
            FbProducts prod = productsMapper.selectById(pr.getProductId());
            if (prod == null || prod.getIsArchived() != null && prod.getIsArchived() == 1) continue;
            var lines = linesMapper.selectList(
                    new LambdaQueryWrapper<FbLines>().eq(FbLines::getProductId, prod.getId()));
            result.add(Map.of("id", prod.getId(), "product_name", prod.getProductName(),
                    "lines", lines.stream().map(l -> Map.of("id", l.getId(), "line_name", l.getLineName())).toList()));
        }
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<FbProducts> detail(@PathVariable Long id) {
        FbProducts p = fbService.getProductById(id);
        return p != null ? ApiResponse.ok(p) : ApiResponse.fail("产品不存在");
    }

    @PostMapping("/create")
    public ApiResponse<FbProducts> create(@AuthenticationPrincipal UserPrincipal p,
            @RequestBody Map<String, Object> body) {
        String name = (String) body.get("product_name");
        if (name == null || name.isBlank()) return ApiResponse.fail("产品名不能为空");
        Long spId = lng(body, "sales_person_id");
        Double ratio = dbl(body, "agency_ratio");
        FbProducts prod = fbService.createProduct(p.getUserId(), name, (String) body.get("kpi"),
                (String) body.get("region"), spId, ratio);

        // 处理关联数据
        @SuppressWarnings("unchecked")
        List<Long> bmIds = (List<Long>) body.getOrDefault("bm_ids", List.of());
        for (Long bmId : bmIds) {
            FbProductBms pb = new FbProductBms(); pb.setProductId(prod.getId()); pb.setBmId(bmId);
            productBmsMapper.insert(pb);
        }
        @SuppressWarnings("unchecked")
        List<Long> runnerIds = (List<Long>) body.getOrDefault("runner_ids", List.of());
        for (Long uid : runnerIds) {
            FbProductRunners pr = new FbProductRunners(); pr.setProductId(prod.getId()); pr.setUserId(uid);
            productRunnersMapper.insert(pr);
        }
        @SuppressWarnings("unchecked")
        List<Map<String, String>> lines = (List<Map<String, String>>) body.getOrDefault("lines", List.of());
        for (var l : lines) {
            FbLines fl = new FbLines(); fl.setProductId(prod.getId());
            fl.setLineName(l.get("line_name")); fl.setLink(l.get("link"));
            if (l.containsKey("pixel_id")) fl.setPixelId(Long.valueOf(l.get("pixel_id")));
            linesMapper.insert(fl);
        }
        return ApiResponse.ok(prod);
    }

    @PutMapping("/{id}")
    public ApiResponse<FbProducts> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long spId = lng(body, "sales_person_id");
        Double ratio = dbl(body, "agency_ratio");
        FbProducts prod = fbService.updateProduct(id, (String) body.get("product_name"),
                (String) body.get("kpi"), (String) body.get("region"), spId, ratio);
        if (prod == null) return ApiResponse.fail("产品不存在");

        // 更新关联：bm_ids — 先删后插
        @SuppressWarnings("unchecked")
        List<Long> bmIds = (List<Long>) body.getOrDefault("bm_ids", List.of());
        if (!bmIds.isEmpty()) {
            productBmsMapper.delete(new LambdaQueryWrapper<FbProductBms>().eq(FbProductBms::getProductId, id));
            for (Long bmId : bmIds) {
                FbProductBms pb = new FbProductBms(); pb.setProductId(id); pb.setBmId(bmId);
                productBmsMapper.insert(pb);
            }
        }
        @SuppressWarnings("unchecked")
        List<Long> runnerIds = (List<Long>) body.getOrDefault("runner_ids", List.of());
        if (!runnerIds.isEmpty()) {
            productRunnersMapper.delete(new LambdaQueryWrapper<FbProductRunners>().eq(FbProductRunners::getProductId, id));
            for (Long uid : runnerIds) {
                FbProductRunners pr = new FbProductRunners(); pr.setProductId(id); pr.setUserId(uid);
                productRunnersMapper.insert(pr);
            }
        }
        @SuppressWarnings("unchecked")
        List<Map<String, String>> lines = (List<Map<String, String>>) body.getOrDefault("lines", List.of());
        if (!lines.isEmpty()) {
            linesMapper.delete(new LambdaQueryWrapper<FbLines>().eq(FbLines::getProductId, id));
            for (var l : lines) {
                FbLines fl = new FbLines(); fl.setProductId(id);
                fl.setLineName(l.get("line_name")); fl.setLink(l.get("link"));
                if (l.containsKey("pixel_id")) fl.setPixelId(Long.valueOf(l.get("pixel_id")));
                linesMapper.insert(fl);
            }
        }
        return ApiResponse.ok(prod);
    }

    /** 软删除 — 归档 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal p) {
        FbProducts prod = productsMapper.selectById(id);
        if (prod != null && prod.getOwnerId().equals(p.getUserId())) {
            prod.setIsArchived(1L); productsMapper.updateById(prod);
        }
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/restore")
    public ApiResponse<Void> restore(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal p) {
        FbProducts prod = productsMapper.selectById(id);
        if (prod != null && prod.getOwnerId().equals(p.getUserId())) {
            prod.setIsArchived(0L); productsMapper.updateById(prod);
        }
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/detail")
    public ApiResponse<com.lmserver.dto.response.FbProductDto> fullDetail(@PathVariable Long id) {
        com.lmserver.dto.response.FbProductDto dto = productsMapper.selectFbProductDtoById(id);
        if (dto == null) return ApiResponse.fail("不存在");

        // 填充集合字段
        var pbs = productBmsMapper.selectList(
                new LambdaQueryWrapper<FbProductBms>().eq(FbProductBms::getProductId, id));
        List<com.lmserver.dto.response.BmBriefDto> bms = new ArrayList<>();
        for (var pb : pbs) {
            FbBms bm = bmsMapper.selectById(pb.getBmId());
            bms.add(new com.lmserver.dto.response.BmBriefDto(
                    pb.getBmId(), bm != null ? bm.getName() : null, bm != null ? bm.getBmId() : null));
        }
        dto.setBms(bms);

        var runners = productRunnersMapper.selectList(
                new LambdaQueryWrapper<FbProductRunners>().eq(FbProductRunners::getProductId, id));
        dto.setRunnerIds(runners.stream().map(FbProductRunners::getUserId).toList());

        var lines = linesMapper.selectList(
                new LambdaQueryWrapper<FbLines>().eq(FbLines::getProductId, id));
        dto.setLines(lines.stream().map(l ->
            new com.lmserver.dto.response.LineBriefDto(l.getId(), l.getLineName(), l.getLink(), l.getPixelId())
        ).toList());

        return ApiResponse.ok(dto);
    }

    private Long lng(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? Long.valueOf(v.toString()) : null; }
    private Double dbl(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? Double.valueOf(v.toString()) : null; }
}
