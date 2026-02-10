package com.rymcu.mortise.member.admin.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.web.annotation.AdminController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Sample admin controller for member management.
 */
@Tag(name = "Member Admin")
@AdminController
@RequestMapping("/members")
public class MemberAdminSampleController {

    @Operation(summary = "Admin sample endpoint")
    @GetMapping("/sample")
    public GlobalResult<String> sample() {
        return GlobalResult.success("member admin ok");
    }
}
