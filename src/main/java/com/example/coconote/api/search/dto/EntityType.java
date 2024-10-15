package com.example.coconote.api.search.dto;

import lombok.Getter;

@Getter
public enum EntityType {
    CHANNEL("channel"),
    WORKSPACE_MEMBER("workspace_member"),
    THREAD("thread"),
    FILE("file"),
    CANVAS_BLOCK("canvas_block"),;

    private final String value;

    EntityType(String value) {
        this.value = value;
    }
}
