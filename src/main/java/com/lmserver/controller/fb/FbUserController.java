package com.lmserver.controller.fb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.common.Users;
import com.lmserver.mapper.common.UsersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fb/users")
@RequiredArgsConstructor
public class FbUserController {

    private final UsersMapper usersMapper;

    @GetMapping
    public ApiResponse<?> list() {
        // 过滤: platform='fb' OR role='developer'，排除 role='hidden'
        var qw = new LambdaQueryWrapper<Users>()
                .ne(Users::getRole, "hidden")
                .and(w -> w.eq(Users::getPlatform, "fb").or().eq(Users::getRole, "developer"));
        return ApiResponse.ok(usersMapper.selectList(qw).stream()
                .peek(u -> u.setPassword(null))
                .map(u -> java.util.Map.of("id", u.getId(), "username", u.getUsername(),
                        "display_name", u.getDisplayName(), "platform", u.getPlatform(), "role", u.getRole()))
                .collect(Collectors.toList()));
    }
}
