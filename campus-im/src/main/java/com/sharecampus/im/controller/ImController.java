package com.sharecampus.im.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.common.security.UserContext;
import com.sharecampus.im.entity.ImMessage;
import com.sharecampus.im.service.ImService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/im")
@RequiredArgsConstructor
public class ImController {

    private final ImService imService;

    @GetMapping("/messages/{sessionId}")
    public Result<List<ImMessage>> messages(@PathVariable String sessionId,
                                            @RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "50") int size) {
        return Result.success(imService.getMessages(sessionId, page, size));
    }

    @GetMapping("/offline-messages")
    public Result<List<String>> offline() {
        return Result.success(imService.pullOffline(UserContext.getUserId()));
    }
}
