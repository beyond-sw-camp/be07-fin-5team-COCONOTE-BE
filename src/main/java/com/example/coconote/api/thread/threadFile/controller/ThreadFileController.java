package com.example.coconote.api.thread.threadFile.controller;

import com.example.coconote.api.thread.threadFile.service.ThreadFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ThreadFileController {
    private final ThreadFileService threadFileService;



}
