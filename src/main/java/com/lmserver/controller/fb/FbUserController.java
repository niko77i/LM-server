package com.lmserver.controller.fb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.UserBriefDto;
import com.lmserver.entity.common.Users;
import com.lmserver.mapper.common.UsersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FB 平台用户查询 — /api/fb/users。
 * 过滤规则：排除 hidden 角色，只返回 platform='fb' 或 role='developer' 的用户。
 */
@RestController
@RequestMapping("/api/fb/users")
@RequiredArgsConstructor
public class FbUserController {

    private final UsersMapper usersMapper;

    @GetMapping
    public ApiResponse<List<UserBriefDto>> list() {
        var qw = new LambdaQueryWrapper<Users>()
                .ne(Users::getRole, "hidden")
                .and(w -> w.eq(Users::getPlatform, "fb").or().eq(Users::getRole, "developer"));
        return ApiResponse.ok(usersMapper.selectList(qw).stream()
                .map(u -> new UserBriefDto(u.getId(), u.getUsername(), u.getDisplayName(), u.getPlatform(), u.getRole()))
                .collect(Collectors.toList()));
    }
}
