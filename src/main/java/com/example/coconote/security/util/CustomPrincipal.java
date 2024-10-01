package com.example.coconote.security.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CustomPrincipal {
    private String email;
    private Long memberId;
    private String nickname;
}
