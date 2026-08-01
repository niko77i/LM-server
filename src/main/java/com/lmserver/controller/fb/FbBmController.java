package com.lmserver.controller.fb;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.fb.FbBms;
import com.lmserver.security.UserPrincipal;
import com.lmserver.service.FbService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** REST Controller [/api/fb/bms] */
@RestController
@RequestMapping("/api/fb/bms")
@RequiredArgsConstructor
public class FbBmController {

    private final FbService fbService;

    @GetMapping("/list")
    public PagedResponse<FbBms> list(@AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return fbService.listBms(principal.getUserId(), page, size, search, status);
    }

    @GetMapping("/{id}")
    public ApiResponse<FbBms> detail(@PathVariable Long id) {
        FbBms b = fbService.getBmById(id);
        return b != null ? ApiResponse.ok(b) : ApiResponse.fail("BM不存在");
    }

    @PostMapping("/create")
    public ApiResponse<FbBms> create(@AuthenticationPrincipal UserPrincipal principal,
            @RequestBody Map<String, String> body) {
        String name = body.get("name"), bmId = body.get("bm_id");
        if (name == null || bmId == null) return ApiResponse.fail("名称和BM ID不能为空");
        return ApiResponse.ok(fbService.createBm(principal.getUserId(), name, bmId, body.get("note")));
    }

    @PutMapping("/{id}")
    public ApiResponse<FbBms> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        FbBms b = fbService.updateBm(id, body.get("name"), body.get("note"));
        return b != null ? ApiResponse.ok(b) : ApiResponse.fail("BM不存在");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) { fbService.deleteBm(id); return ApiResponse.ok(); }

    @GetMapping("/options")
    public ApiResponse<?> options(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(fbService.bmOptions(principal.getUserId()));
    }
}
