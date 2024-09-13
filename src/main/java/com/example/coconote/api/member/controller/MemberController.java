package com.example.coconote.api.member.controller;


import com.example.coconote.api.thread.service.ThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/member")
@RequiredArgsConstructor
public class MemberController {
    private final ThreadService threadService;

    @PostMapping("create")
    public String create() {
        return "create";
    }
}
