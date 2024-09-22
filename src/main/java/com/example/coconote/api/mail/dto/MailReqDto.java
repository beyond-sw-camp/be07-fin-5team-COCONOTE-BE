package com.example.coconote.api.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailReqDto {
	// 인증 메일 전송
	private String receiver;
	private String title;
	private String contents;
}
