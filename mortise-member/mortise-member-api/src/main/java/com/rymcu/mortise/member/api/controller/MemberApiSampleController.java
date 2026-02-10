package com.rymcu.mortise.member.api.controller;

import com.rymcu.mortise.core.result.GlobalResult;
import com.rymcu.mortise.web.annotation.ApiController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Sample public API controller for members.
 */
@Tag(name = "Member API")
@ApiController
@RequestMapping("/members")
public class MemberApiSampleController {

    @Operation(summary = "Public sample endpoint")
    @GetMapping("/sample")
    public GlobalResult<String> sample() {
        return GlobalResult.success("member api ok");
    }
}
