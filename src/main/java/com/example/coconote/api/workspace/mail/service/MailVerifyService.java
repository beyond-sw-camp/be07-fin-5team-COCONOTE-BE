package com.example.coconote.api.workspace.mail.service;

import com.example.coconote.api.workspace.mail.dto.MailReqDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailVerifyService {

	private final JavaMailSender mailSender;
//	private final RedisService redisService;

//	private static final String AUTH_CODE_PREFIX = "USER_AUTH_CODE : ";
//	private static final String AUTH_EMAIL_PREFIX = "EMAIL_CERTIFICATE : ";

	@Value("${spring.mail.auth-code-expiration-millis}")
	private long authCodeExpirationMillis;

	@Autowired
	public MailVerifyService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
//		this.redisService = redisService;
	}

	// 메일 전송
	public void sendEmail(MailReqDto mailReqDto) {
		// 전송할 이메일 저장
//
//		String redisAuthCode = redisService.getValues(AUTH_EMAIL_PREFIX + mailReqDto.getReceiver());
//		if(redisService.checkExistsValue(redisAuthCode) ){
//		// 	이미 회원가입한 내역이 존재한다면
//			throw new IllegalArgumentException("이미 인증 내역이 존재합니다.");
//		}

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(mailReqDto.getReceiver());
		message.setSubject(mailReqDto.getTitle());
		message.setText(mailReqDto.getContents());
		mailSender.send(message);    //이메일 전송


	}

//	// 	인증번호 검증
//	// 이메일을 입력받아 redis의 인증코드와 파라미터의 인증코드가 동일한지 비교
//	// 일치하면 true, 불일치하면 false;
//	public boolean verifiedCode(String email, String authCode) {
//		// 	존재하는 회원 정보가 있는지 확인
//		String redisAuthCode = redisService.getValues(AUTH_CODE_PREFIX + email);
//		boolean response = redisService.checkExistsValue(redisAuthCode) && redisAuthCode.equals(authCode);
//
//		if (response) {
//			// 기존의 AUTH_CODE_PRE
//			redisService.setValues(AUTH_EMAIL_PREFIX + email, "true",
//				Duration.ofMillis(this.authCodeExpirationMillis));
//			redisService.deleteValues(AUTH_CODE_PREFIX + email);
//		}
//		return response;
//	}

	@Transactional
	public void sendCodeToEmail(String email) {
		String title = "이메일 인증 번호";
//		String authCode = this.createCode();
		MailReqDto mailReqDto = MailReqDto.builder()
			.receiver(email)
			.title(title)
			.contents("test")
			.build();

		// 메일 전송하고
		this.sendEmail(mailReqDto);
		// redis에 code 저장
//		redisService.setValues(AUTH_CODE_PREFIX + email, authCode, Duration.ofMillis(this.authCodeExpirationMillis));
	}
}
