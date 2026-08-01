package com.lmserver.controller.gg;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.common.Copywritings;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.CopywritingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** REST Controller [/api/copywriting] */
@RestController
@RequestMapping("/api/copywriting")
@RequiredArgsConstructor
public class CopywritingController {

    private final CopywritingService service;

    @GetMapping("/list")
    public PagedResponse<Copywritings> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String region) {
        return service.list(principal.getUserId(), page, size, region);
    }

    @PostMapping("/create")
    public ApiResponse<Copywritings> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, Object> body) {
        String region = (String) body.getOrDefault("region", "通用");
        String content = (String) body.get("content");
        if (content == null || content.isBlank()) return ApiResponse.fail("内容不能为空");
        return ApiResponse.ok(service.create(principal.getUserId(), region, content,
                body.get("is_public") != null ? Integer.valueOf(body.get("is_public").toString()) : 0));
    }

    @PutMapping("/{id}")
    public ApiResponse<Copywritings> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Copywritings c = service.update(id, body.get("region"), body.get("content"), body.get("effectiveness"));
        return c != null ? ApiResponse.ok(c) : ApiResponse.fail("不存在");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { service.delete(id); return ApiResponse.ok(); }

    @PostMapping("/batch-delete")
    public ApiResponse<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        service.batchDelete(body.getOrDefault("ids", List.of())); return ApiResponse.ok();
    }
}
