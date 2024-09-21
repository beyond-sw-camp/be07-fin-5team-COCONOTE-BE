package com.example.coconote.api.thread.controller;


import com.example.coconote.api.thread.service.ThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/thread")
@RequiredArgsConstructor
public class ThreadController {
    private final ThreadService threadService;

    @PostMapping("create")
    public String create() {
        return "create";
    }
}
