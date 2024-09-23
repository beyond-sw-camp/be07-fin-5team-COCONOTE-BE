package com.example.coconote.api.mail.controller;


import com.example.coconote.api.mail.service.MailVerifyService;
import com.example.coconote.common.CommonResDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class MailVerifyController {

	private final MailVerifyService mailVerifyService;
//	private final MemberService memberService;

	@Autowired
	public MailVerifyController(MailVerifyService mailVerifyService) {
		this.mailVerifyService = mailVerifyService;
//		this.memberService = memberService;
	}

	@Operation(summary= "[이메일 인증] 인증 메일 요청 API")
	@PostMapping("/requestCode")
	public ResponseEntity<CommonResDto> requestEmail(@RequestParam("email") @Valid String email) {
		mailVerifyService.sendCodeToEmail(email);
		// 이메일 전송하고 레디스에 저장
		CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "이메일 인증 요청을 성공했습니다.", email);
		return new ResponseEntity<>(commonResDto, HttpStatus.OK);
	}

//	// 인증번호 검증 요청
//	@Operation(summary= "[인증번호 인증] 인증 번호 검증 API")
//	@GetMapping("/requestCode")
//	public ResponseEntity<?> verificationEmail(@RequestParam("email") @Valid String email,
//		@RequestParam("code") String authCode) {
//		boolean response = mailVerifyService.verifiedCode(email, authCode);	// 레디스의 정보와 입력한 정보를 비교
//		if(response){
//			CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "이메일 인증을 성공하였습니다", response);
//			return new ResponseEntity<>(commonResDto, HttpStatus.OK);
//		}else{
//			CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않습니다.");
//			return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
//		}
//
//	}
}
