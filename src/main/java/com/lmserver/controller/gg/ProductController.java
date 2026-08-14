package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.gg.*;
import com.lmserver.entity.common.AuditLog;
import com.lmserver.mapper.gg.*;
import com.lmserver.mapper.common.SalesPersonsMapper;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
/**
 * 产品管理控制器 — /api/products/*，GG平台产品的完整CRUD+下拉选项
 */

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    @Autowired private ProductsMapper productsMapper;
    @Autowired private PackagesMapper packagesMapper;
    @Autowired private ProductAssetsMapper productAssetsMapper;
    @Autowired private DelistChecksMapper delistChecksMapper;
    @Autowired private com.lmserver.mapper.common.AuditLogMapper auditLogMapper;
    @Autowired private com.lmserver.mapper.gg.MccMapper mccMapper;
    @Autowired private com.lmserver.mapper.gg.ProductRunnersMapper productRunnersMapper;
    @Autowired private com.lmserver.mapper.common.UsersMapper usersMapper;
    @Autowired private com.lmserver.mapper.common.SalesPersonsMapper salesPersonsMapper;
    @Autowired private JdbcTemplate jdbcTemplate;

    @GetMapping("/list")
    /** 分页列表查询 — 支持多条件筛选 */
    public PagedResponse<com.lmserver.dto.response.ProductDto> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status) {
        return productService.list(principal.getUserId(), page, size, search, region, status);
    }

    /** 产品成效素材列表（前端复制链接用），返回 {success, assets:[...]} */
    @GetMapping("/{id}/assets")
    public Map<String, Object> assets(@PathVariable Long id) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> assets = jdbcTemplate.queryForList(
                    "SELECT v.*, pa.added_by, pa.added_at, u.display_name AS added_by_name " +
                    "FROM product_assets pa " +
                    "JOIN videos v ON pa.video_id = v.id AND pa.video_owner_id = v.owner_id " +
                    "LEFT JOIN users u ON pa.added_by = u.id " +
                    "WHERE pa.product_id = ? ORDER BY pa.added_by, pa.added_at DESC", id);
            result.put("success", true);
            result.put("assets", assets);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }

    /** 产品详情 — 含包列表/runners/关联账户/MCC信息，所有字段对齐前端期望 */
    @GetMapping("/{id}/detail")
    public ApiResponse<com.lmserver.dto.response.ProductDetailDto> detail(@PathVariable Long id) {
        com.lmserver.dto.response.ProductDto base = productsMapper.selectProductDtoById(id);
        if (base == null) return ApiResponse.fail("产品不存在");

        com.lmserver.dto.response.ProductDetailDto dto = new com.lmserver.dto.response.ProductDetailDto();
        // 复制父类字段
        dto.setId(base.getId());
        dto.setProductName(base.getProductName());
        dto.setKpi(base.getKpi());
        dto.setRegion(base.getRegion());
        dto.setStatus(base.getStatus());
        dto.setCustomer(base.getCustomer());
        dto.setOwnerId(base.getOwnerId());
        dto.setAgencyRatio(base.getAgencyRatio());
        dto.setIsArchived(base.getIsArchived());
        dto.setCreatedAt(base.getCreatedAt());
        dto.setMccId(base.getMccId());
        dto.setSalesPersonId(base.getSalesPersonId());
        dto.setMccName(base.getMccName());
        dto.setMccCode(base.getMccCode());
        dto.setSalesPersonName(base.getSalesPersonName());
        dto.setAssetCount(base.getAssetCount());
        dto.setRelatedAccountCount(base.getRelatedAccountCount());

        // 包列表
        var pkgs = packagesMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Packages>()
                        .eq(Packages::getProductId, id));
        dto.setPackages(pkgs.stream().map(p -> new com.lmserver.dto.response.PackageDto(
                p.getId(), p.getProductId(), p.getSeriesName(), p.getPackageName(),
                p.getUrl(), p.getStatus(), p.getCreatedAt())).toList());

        // 关联账户 + status_count
        List<Map<String, Object>> relatedAccounts = new ArrayList<>();
        Map<String, Long> statusCount = new LinkedHashMap<>();
        if (base.getMccId() != null) {
            collectMccAccounts(base.getMccId(), relatedAccounts, new HashSet<>());
            for (var acct : relatedAccounts) {
                Object st = acct.get("status");
                String sname = st != null ? st.toString() : "未知";
                statusCount.merge(sname, 1L, Long::sum);
            }
        }
        dto.setRelatedAccounts(relatedAccounts);
        dto.setStatusCount(statusCount);

        // 在跑人员
        var runners = productRunnersMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductRunners>()
                        .eq(ProductRunners::getProductId, id));
        dto.setRunners(runners);
        dto.setRunnerIdList(runners.stream().map(ProductRunners::getUserId).toList());

        return ApiResponse.ok(dto);
    }

    @Autowired private com.lmserver.mapper.gg.AccountsMapper accountsMapper;

    /** 递归收集 MCC 及子MCC 下的所有账户 */
    private void collectMccAccounts(Long mccId, List<Map<String, Object>> result, Set<Long> visited) {
        if (!visited.add(mccId)) return;
        var accts = accountsMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Accounts>()
                        .eq(Accounts::getMccId, mccId).isNull(Accounts::getDeletedAt));
        for (var a : accts)
            result.add(Map.of("id", a.getId(), "name", a.getName(), "account_id", a.getAccountId(), "status_id", a.getStatusId()));
        var children = mccMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Mcc>()
                        .eq(Mcc::getParentMccId, mccId));
        for (var c : children) collectMccAccounts(c.getId(), result, visited);
    }

    @PostMapping("/create")
    /** 新增记录 — 返回创建后的完整对象 */
    public ApiResponse<Products> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String name = str(body, "product_name");
        if (name == null || name.isBlank()) return ApiResponse.fail("产品名不能为空");
        return ApiResponse.ok(productService.create(
                principal.getUserId(), name,
                str(body, "kpi"), str(body, "region"), str(body, "status"),
                str(body, "customer"), lng(body, "sales_person_id"),
                lng(body, "mcc_id"), dbl(body, "agency_ratio")));
    }

    @PutMapping("/{id}")
    /** 更新记录 — 部分字段更新，只改传入的非 null 字段 */
    public ApiResponse<Products> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Products p = productService.update(id,
                str(body, "product_name"), str(body, "kpi"), str(body, "region"),
                str(body, "status"), str(body, "customer"), lng(body, "sales_person_id"),
                lng(body, "mcc_id"), dbl(body, "agency_ratio"));
        return p != null ? ApiResponse.ok(p) : ApiResponse.fail("产品不存在");
    }

    @DeleteMapping("/{id}")
    /** 软删除 — 设置 is_archived=1 + deleted_at */
    public ApiResponse<Void> delete(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Products p = productService.getById(id);
        if (p == null) return ApiResponse.fail("产品不存在");
        if (!p.getOwnerId().equals(principal.getUserId())) return ApiResponse.fail("无权限");
        p.setIsArchived(1L);
        p.setDeletedAt(java.time.LocalDateTime.now());
        productsMapper.updateById(p);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/restore")
    /** 恢复软删除 — 清除 is_archived + deleted_at */
    public ApiResponse<Void> restore(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Products p = productService.getById(id);
        if (p == null) return ApiResponse.fail("产品不存在");
        if (!p.getOwnerId().equals(principal.getUserId())) return ApiResponse.fail("无权限");
        p.setIsArchived(0L);
        p.setDeletedAt(null);
        productsMapper.updateById(p);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}/permanent")
    public ApiResponse<Void> deletePermanent(@PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        Products p = productService.getById(id);
        if (p == null) return ApiResponse.fail("产品不存在");
        if (!p.getOwnerId().equals(principal.getUserId())) return ApiResponse.fail("无权限");
        if (p.getDeletedAt() == null) return ApiResponse.fail("请先删除产品再永久删除");

        // 清理关联数据
        packagesMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Packages>()
                .eq(Packages::getProductId, id));
        productAssetsMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductAssets>()
                .eq(ProductAssets::getProductId, id));
        delistChecksMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DelistChecks>()
                .eq(DelistChecks::getProductId, id));
        // 写入审计日志
        AuditLog al = new AuditLog();
        al.setUserId(principal.getUserId());
        al.setAction("delete_product");
        al.setTargetType("product");
        al.setTargetId(id);
        al.setTargetName(p.getProductName());
        al.setCreatedAt(java.time.LocalDateTime.now());
        auditLogMapper.insert(al);

        productsMapper.deleteById(id);
        return ApiResponse.ok();
    }

    @GetMapping("/options")
    /** 获取下拉选项 — 返回 id + name 的简略列表 */
    public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(productService.options(principal.getUserId()));
    }

    @PostMapping("/batch-update") public ApiResponse<Integer> batchUpdate(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked") List<Long> ids = (List<Long>) body.getOrDefault("ids", List.of());
        int c=0; for (Long id : ids) { productService.update(id, str(body,"product_name"), str(body,"kpi"),
                str(body,"region"), str(body,"status"), str(body,"customer"), lng(body,"sales_person_id"),
                lng(body,"mcc_id"), dbl(body,"agency_ratio")); c++; }
        return ApiResponse.ok(c);
    }

    @GetMapping("/deleted") public PagedResponse<Products> deleted(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue="1") int page, @RequestParam(defaultValue="20") int size) {
        var qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Products>()
                .eq(Products::getOwnerId, principal.getUserId()).isNotNull(Products::getDeletedAt);
        var pg = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<Products>(page, size);
        productsMapper.selectPage(pg, qw); return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }
    // productsMapper 已在上方声明

    @PostMapping("/{id}/archive")
    public ApiResponse<Void> archive(@PathVariable Long id) {
        Products p = productService.getById(id);
        if (p != null) { p.setIsArchived(1L); productService.update(id,null,null,null,null,null,null,null,null); }
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/unarchive")
    public ApiResponse<Void> unarchive(@PathVariable Long id) {
        Products p = productService.getById(id);
        if (p != null) { p.setIsArchived(0L); productService.update(id,null,null,null,null,null,null,null,null); }
        return ApiResponse.ok();
    }

    private String str(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? v.toString() : null; }
    /** 产品合并 — 对齐 Python products_merge */
    @PostMapping("/merge")
    public ApiResponse<Map<String, Object>> merge(@AuthenticationPrincipal UserPrincipal p,
            @RequestBody Map<String, Object> body) {
        try {
            Long masterId = lng(body, "master_id");
            @SuppressWarnings("unchecked")
            List<Long> mergeIds = ((List<Object>) body.getOrDefault("merge_ids", List.of()))
                    .stream().map(o -> Long.valueOf(o.toString())).toList();

            if (masterId == null || mergeIds.isEmpty())
                return ApiResponse.fail("请指定主产品和被合并产品");
            if (mergeIds.contains(masterId))
                return ApiResponse.fail("主产品不能在被合并列表中");

            Products master = productsMapper.selectById(masterId);
            if (master == null) return ApiResponse.fail("主产品不存在");

            Set<Long> allRunners = new HashSet<>(parseRunnerIds(master.getRunnerIds()));
            int mergedPackages = 0;
            for (Long mid : mergeIds) {
                Products sub = productsMapper.selectById(mid);
                if (sub == null) continue;
                allRunners.addAll(parseRunnerIds(sub.getRunnerIds()));

                var pkgs = packagesMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Packages>()
                                .eq(Packages::getProductId, mid));
                for (Packages pkg : pkgs) {
                    if (packagesMapper.selectCount(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Packages>()
                                    .eq(Packages::getProductId, masterId)
                                    .eq(Packages::getPackageName, pkg.getPackageName())
                                    .eq(Packages::getUrl, pkg.getUrl())) == 0) {
                        pkg.setId(null);
                        pkg.setProductId(masterId);
                        packagesMapper.insert(pkg);
                        mergedPackages++;
                    }
                }

                // 清理副产品
                productAssetsMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductAssets>()
                        .eq(ProductAssets::getProductId, mid));
                delistChecksMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DelistChecks>()
                        .eq(DelistChecks::getProductId, mid));
                productRunnersMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductRunners>()
                        .eq(ProductRunners::getProductId, mid));
                packagesMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Packages>()
                        .eq(Packages::getProductId, mid));
                productsMapper.deleteById(mid);
            }

            // 更新主产品 runner_ids
            String runnerJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(new ArrayList<>(allRunners));
            master.setRunnerIds(runnerJson);
            productsMapper.updateById(master);
            productRunnersMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductRunners>()
                    .eq(ProductRunners::getProductId, masterId));
            for (Long uid : allRunners) {
                ProductRunners pr = new ProductRunners();
                pr.setProductId(masterId); pr.setUserId(uid);
                productRunnersMapper.insert(pr);
            }

            return ApiResponse.ok(Map.of("merged_packages", mergedPackages,
                    "merged_products", mergeIds.size(), "total_runners", allRunners.size()));
        } catch (Exception e) {
            return ApiResponse.fail("合并失败: " + e.getMessage());
        }
    }

    /** 文本导入 — 解析 Google Play 链接提取包名/系列名 */
    @PostMapping("/import-text")
    public ApiResponse<Map<String, Object>> importText(@AuthenticationPrincipal UserPrincipal p,
            @RequestBody Map<String, Object> body) {
        String text = (String) body.getOrDefault("text", "");
        Long productId = lng(body, "product_id");
        if (productId == null) return ApiResponse.fail("请指定产品");

        int imported = 0, skipped = 0;
        for (String line : text.split("\n")) {
            line = line.trim();
            if (line.isBlank()) continue;
            // 提取包名（Google Play URL 的 id= 参数）
            String pkgName = line;
            if (line.contains("id=")) {
                int idx = line.indexOf("id=") + 3;
                int end = line.indexOf("&", idx);
                pkgName = end > 0 ? line.substring(idx, end) : line.substring(idx);
            }
            // 检查重名
            if (packagesMapper.selectCount(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Packages>()
                            .eq(Packages::getProductId, productId)
                            .eq(Packages::getPackageName, pkgName)) == 0) {
                Packages pkg = new Packages();
                pkg.setProductId(productId);
                pkg.setPackageName(pkgName);
                pkg.setUrl(line);
                pkg.setStatus("");
                pkg.setCreatedAt(java.time.LocalDateTime.now());
                packagesMapper.insert(pkg);
                imported++;
            } else {
                skipped++;
            }
        }
        return ApiResponse.ok(Map.of("imported", imported, "skipped", skipped));
    }

    /** 更新产品 runner 列表 — 新增 runner 自动分配 MCC 权限链 */
    @PutMapping("/{pid}/runners")
    public ApiResponse<Void> updateRunners(@PathVariable Long pid,
            @AuthenticationPrincipal UserPrincipal p,
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> runnerIds = ((List<Object>) body.getOrDefault("runner_ids", List.of()))
                .stream().map(o -> Long.valueOf(o.toString())).toList();

        Products prod = productsMapper.selectById(pid);
        if (prod == null) return ApiResponse.fail("产品不存在");

        try {
            // 更新 runner_ids JSON 列
            prod.setRunnerIds(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(runnerIds));
            productsMapper.updateById(prod);

            // 同步 product_runners 关联表
            productRunnersMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ProductRunners>()
                    .eq(ProductRunners::getProductId, pid));
            for (Long uid : runnerIds) {
                ProductRunners pr = new ProductRunners();
                pr.setProductId(pid);
                pr.setUserId(uid);
                productRunnersMapper.insert(pr);
                // 自动分配 MCC 权限链
                if (prod.getMccId() != null) {
                    assignMccChainToUser(prod.getMccId(), uid, new HashSet<>());
                }
            }
        } catch (Exception e) {
            return ApiResponse.fail("更新失败: " + e.getMessage());
        }
        return ApiResponse.ok();
    }

    // ── 辅助 ──

    private Set<Long> parseRunnerIds(String json) {
        if (json == null || json.isBlank() || "[]".equals(json)) return Set.of();
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {})
                    .stream().filter(Objects::nonNull).collect(Collectors.toSet());
        } catch (Exception e) { return Set.of(); }
    }

    /** 递归将用户加入 MCC 及上级链的 shared_user_ids */
    private void assignMccChainToUser(Long mccId, Long userId, Set<Long> visited) {
        if (visited.contains(mccId)) return;
        visited.add(mccId);
        var mcc = mccMapper.selectById(mccId);
        if (mcc == null) return;
        if (!mcc.getOwnerId().equals(userId)) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                List<Long> shared = mcc.getSharedUserIds() != null
                        ? om.readValue(mcc.getSharedUserIds(), new com.fasterxml.jackson.core.type.TypeReference<List<Long>>() {})
                        : new ArrayList<>();
                if (!shared.contains(userId)) {
                    shared.add(userId);
                    mcc.setSharedUserIds(om.writeValueAsString(shared));
                    mccMapper.updateById(mcc);
                }
            } catch (Exception ignored) {}
        }
        if (mcc.getParentMccId() != null) {
            assignMccChainToUser(mcc.getParentMccId(), userId, visited);
        }
    }

    private Long lng(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? Long.valueOf(v.toString()) : null; }
    private Double dbl(Map<String, Object> m, String k) { Object v = m.get(k); return v != null ? Double.valueOf(v.toString()) : null; }
}
