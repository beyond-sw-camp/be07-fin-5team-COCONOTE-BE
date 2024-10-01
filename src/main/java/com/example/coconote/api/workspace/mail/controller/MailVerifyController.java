package com.example.coconote.api.workspace.mail.controller;


import com.example.coconote.api.workspace.mail.service.MailVerifyService;
import com.example.coconote.common.CommonResDto;
import com.example.coconote.security.util.CustomPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class MailVerifyController {

	private final MailVerifyService mailVerifyService;

	@Autowired
	public MailVerifyController(MailVerifyService mailVerifyService) {
		this.mailVerifyService = mailVerifyService;
	}

	@Operation(summary= "워크스페이스 가입 요청 메일 보내기(워크스페이스 최고관리자, 관리자)")
	@PostMapping("/email/request/{workspaceId}")
	public ResponseEntity<CommonResDto> requestEmail(@RequestParam("email") @Valid String email, @PathVariable Long workspaceId, @AuthenticationPrincipal CustomPrincipal customPrincipal) {
		mailVerifyService.sendCodeToEmail(workspaceId, email, customPrincipal.getEmail());
		CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "워크스페이스 가입요청을 이메일로 전송했습니다.", email);
		return new ResponseEntity<>(commonResDto, HttpStatus.OK);
	}

}
