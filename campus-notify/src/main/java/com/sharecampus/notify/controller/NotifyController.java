package com.sharecampus.notify.controller;

import com.sharecampus.common.core.model.Result;
import com.sharecampus.common.security.UserContext;
import com.sharecampus.notify.entity.MessageRecord;
import com.sharecampus.notify.entity.UserMessage;
import com.sharecampus.notify.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @GetMapping("/messages")
    public Result<List<MessageRecord>> myMessages(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(notifyService.myMessages(userId));
    }

    @GetMapping("/inbox")
    public Result<List<UserMessage>> myInbox(@RequestHeader("X-User-Id") Long userId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return Result.success(notifyService.myInbox(userId, page, size));
    }
}
