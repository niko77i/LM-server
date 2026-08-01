package com.lmserver.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.PagedResponse;
import com.lmserver.entity.common.Users;
import com.lmserver.mapper.common.UsersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsersMapper usersMapper;

    @GetMapping("/users")
    @PreAuthorize("hasAnyRole('DEVELOPER','ADMIN')")
    public PagedResponse<Users> listUsers(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pg = usersMapper.selectPage(
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size), null);
        pg.getRecords().forEach(u -> u.setPassword(null)); // 脱敏
        return PagedResponse.of(pg.getRecords(), pg.getTotal(), page, size);
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('DEVELOPER','ADMIN')")
    public ApiResponse<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Users u = usersMapper.selectById(id);
        if (u == null) return ApiResponse.fail("用户不存在");
        if (body.containsKey("role")) u.setRole((String) body.get("role"));
        if (body.containsKey("platform")) u.setPlatform((String) body.get("platform"));
        if (body.containsKey("display_name")) u.setDisplayName((String) body.get("display_name"));
        usersMapper.updateById(u);
        return ApiResponse.ok();
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('DEVELOPER')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        Users u = usersMapper.selectById(id);
        if (u != null) { u.setRole("hidden"); usersMapper.updateById(u); }
        return ApiResponse.ok();
    }
}
