package com.qxkj.admin.controller;

import com.qxkj.common.request.AiChatRequest;
import com.qxkj.common.response.AiChatResponse;
import com.qxkj.common.result.CommonResult;
import com.qxkj.service.service.AiChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台 AI 问答（DeepSeek）
 */
@Slf4j
@RestController
@RequestMapping("api/admin/ai")
@Api(tags = "AI助手")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @ApiOperation(value = "AI对话")
    @RequestMapping(value = "/chat", method = RequestMethod.POST)
    public CommonResult<AiChatResponse> chat(@RequestBody @Validated AiChatRequest request) {
        return CommonResult.success(aiChatService.chat(request));
    }
}
