package com.lmserver.controller.fb;

import com.lmserver.dto.response.ApiResponse;
import com.lmserver.entity.common.Users;
import com.lmserver.mapper.common.UsersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FB 平台用户查询 — /api/fb/users。
 */
@RestController
@RequestMapping("/api/fb/users")
@RequiredArgsConstructor
public class FbUserController {

    private final UsersMapper usersMapper;

    @GetMapping
    public ApiResponse<List<Users>> list() {
        return ApiResponse.ok(usersMapper.selectList(null).stream().peek(u -> u.setPassword(null)).collect(Collectors.toList()));
    }
}
