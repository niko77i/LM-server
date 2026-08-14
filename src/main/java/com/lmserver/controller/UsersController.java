package com.lmserver.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lmserver.dto.response.ApiResponse;
import com.lmserver.dto.response.UserBriefDto;
import com.lmserver.entity.common.Users;
import com.lmserver.mapper.common.UsersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户查询控制器 — /api/users/*，runner 选择器等通用用户名单。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersMapper usersMapper;

    /** 返回非 hidden 用户名单（runner 选择器用），前端读 res.data */
    @GetMapping("/names")
    public ApiResponse<List<UserBriefDto>> names() {
        List<Users> users = usersMapper.selectList(
                new LambdaQueryWrapper<Users>().ne(Users::getRole, "hidden"));
        List<UserBriefDto> result = users.stream()
                .map(u -> new UserBriefDto(u.getId(), u.getUsername(), u.getDisplayName(), u.getPlatform(), u.getRole()))
                .toList();
        return ApiResponse.ok(result);
    }
}
