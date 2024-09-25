package com.example.coconote.api.workspace.mail.controller;


import com.example.coconote.api.workspace.mail.service.MailVerifyService;
import com.example.coconote.common.CommonResDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class MailVerifyController {

	private final MailVerifyService mailVerifyService;

	@Autowired
	public MailVerifyController(MailVerifyService mailVerifyService) {
		this.mailVerifyService = mailVerifyService;
	}

	@Operation(summary= "[이메일 인증] 인증 메일 요청 API")
	@PostMapping("/email/request")
	public ResponseEntity<CommonResDto> requestEmail(@RequestParam("email") @Valid String email) {
		mailVerifyService.sendCodeToEmail(email);
		// 이메일 전송하고 레디스에 저장
		CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "이메일 인증 요청을 성공했습니다.", email);
		return new ResponseEntity<>(commonResDto, HttpStatus.OK);
	}

}
