package com.example.coconote.api.mail.service;

import com.example.coconote.api.mail.dto.MailReqDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailVerifyService {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.auth-code-expiration-millis}")
	private long authCodeExpirationMillis;

	@Autowired
	public MailVerifyService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	// 메일 전송
	public void sendEmail(MailReqDto mailReqDto) {
		// 전송할 이메일 저장

		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(mailReqDto.getReceiver());
		message.setSubject(mailReqDto.getTitle());
		message.setText(mailReqDto.getContents());
		mailSender.send(message);    //이메일 전송


	}

	@Transactional
	public void sendCodeToEmail(String email) {
		String title = "워크스페이스 초대 메일";
		MailReqDto mailReqDto = MailReqDto.builder()
			.receiver(email)
			.title(title)
			.contents("http://localhost:8080/login")
			.build();

		// 메일 전송하고
		this.sendEmail(mailReqDto);
	}
}
