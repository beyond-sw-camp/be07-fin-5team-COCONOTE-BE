package com.example.coconote.api.workspace.mail.service;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.workspace.mail.dto.MailReqDto;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.entity.WsRole;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.example.coconote.common.IsDeleted;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailVerifyService {

	private final JavaMailSender mailSender;
	private final MemberRepository memberRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final WorkspaceRepository workspaceRepository;

	@Value("${spring.mail.auth-code-expiration-millis}")
	private long authCodeExpirationMillis;

	@Autowired
	public MailVerifyService(JavaMailSender mailSender, MemberRepository memberRepository, WorkspaceMemberRepository workspaceMemberRepository, WorkspaceRepository workspaceRepository) {
		this.mailSender = mailSender;
		this.memberRepository = memberRepository;
		this.workspaceMemberRepository = workspaceMemberRepository;
		this.workspaceRepository = workspaceRepository;
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
	public void sendCodeToEmail(Long workspaceId, String recieverEmail, String senderEmail) {
		Member member = memberRepository.findByEmail(senderEmail).orElseThrow(()-> new EntityNotFoundException("회원을 찾을 수 없습니다."));
		Workspace workspace = workspaceRepository.findById(workspaceId).orElseThrow(()-> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
		WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N).orElseThrow(()-> new EntityNotFoundException("워크스페이스 회원을 찾을 수 없습니다."));
		if(workspaceMember.getWsRole().equals(WsRole.USER)) {
			throw new IllegalArgumentException("워크스페이스 초대 권한이 없습니다.");
		}

		String title = "워크스페이스 초대 메일";
		String contents = "http://localhost:8080/api/v1/workspace/" + workspaceId + "{workspaceId}/member/create";
		MailReqDto mailReqDto = MailReqDto.builder()
			.receiver(recieverEmail)
			.title(title)
			.contents(contents)
			.build();

		// 메일 전송하고
		this.sendEmail(mailReqDto);
	}
}
