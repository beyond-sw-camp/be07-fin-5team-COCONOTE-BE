package com.example.coconote.api.workspace.mail.service;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.api.workspace.mail.dto.MailReqDto;
import com.example.coconote.api.workspace.workspace.dto.response.WorkspaceListResDto;
import com.example.coconote.api.workspace.workspace.entity.Workspace;
import com.example.coconote.api.workspace.workspace.repository.WorkspaceRepository;
import com.example.coconote.api.workspace.workspaceMember.entity.WorkspaceMember;
import com.example.coconote.api.workspace.workspaceMember.entity.WsRole;
import com.example.coconote.api.workspace.workspaceMember.repository.WorkspaceMemberRepository;
import com.example.coconote.common.IsDeleted;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class MailVerifyService {

	private final SearchService searchService;
	// JWT Secret Key를 환경 변수나 설정 파일에서 불러옴
	@Value("${jwt.secret}")
	private String jwtSecret;

	private final long jwtExpiration = 86400000; // 24시간

	private final JavaMailSender mailSender;
	private final MemberRepository memberRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final WorkspaceRepository workspaceRepository;

	@Value("${spring.mail.auth-code-expiration-millis}")
	private long authCodeExpirationMillis;

	@Autowired
	public MailVerifyService(JavaMailSender mailSender, MemberRepository memberRepository, WorkspaceMemberRepository workspaceMemberRepository, WorkspaceRepository workspaceRepository, SearchService searchService) {
		this.mailSender = mailSender;
		this.memberRepository = memberRepository;
		this.workspaceMemberRepository = workspaceMemberRepository;
		this.workspaceRepository = workspaceRepository;
		this.searchService = searchService;
	}

	// 메일 전송
	public void sendEmail(MailReqDto mailReqDto) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(mailReqDto.getReceiver());
		message.setSubject(mailReqDto.getTitle());
		message.setText(mailReqDto.getContents());
		mailSender.send(message); // 이메일 전송
	}

	@Transactional
	public void sendCodeToEmail(Long workspaceId, String receiverEmail, String senderEmail) {
		Member member = memberRepository.findByEmail(senderEmail)
				.orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
		Workspace workspace = workspaceRepository.findById(workspaceId)
				.orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));
		WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N)
				.orElseThrow(() -> new EntityNotFoundException("워크스페이스 회원을 찾을 수 없습니다."));

		if (workspaceMember.getWsRole().equals(WsRole.USER)) {
			throw new IllegalArgumentException("워크스페이스 초대 권한이 없습니다.");
		}

		// 초대 링크 생성
		String token = generateJwtToken(receiverEmail, workspaceId);
		String invitationLink = "http://localhost:8082/invite?token=" + token;

		// 이메일 내용 생성
		String title = "워크스페이스 초대 메일";
		String contents = "워크스페이스에 가입하려면 다음 링크를 클릭하세요: " + invitationLink;
		MailReqDto mailReqDto = MailReqDto.builder()
				.receiver(receiverEmail)
				.title(title)
				.contents(contents)
				.build();

		// 메일 전송
		this.sendEmail(mailReqDto);
	}

	// JWT 토큰 생성
	private String generateJwtToken(String email, Long workspaceId) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("email", email);
		claims.put("workspaceId", workspaceId);

		// 안전한 키 생성
		SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

		return Jwts.builder()
				.setClaims(claims)
				.setSubject(email)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // 만료 시간 설정
				.signWith(secretKey, SignatureAlgorithm.HS256) // 서명 알고리즘과 secret 사용
				.compact();
	}

	// JWT 토큰 검증
	public Claims validateJwtToken(String token) {
		SecretKey secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes());

		return Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	// 초대 링크에서 받은 토큰을 검증하고 워크스페이스 가입 처리
	@Transactional
	public WorkspaceListResDto processInvitation(String token) {
		Claims claims = validateJwtToken(token);
		String email = claims.get("email", String.class);
		Long workspaceId = claims.get("workspaceId", Long.class);

		// 이메일과 워크스페이스 정보로 회원 가입 로직 수행
		Member member = memberRepository.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."));
		Workspace workspace = workspaceRepository.findById(workspaceId)
				.orElseThrow(() -> new EntityNotFoundException("워크스페이스를 찾을 수 없습니다."));

		// 이미 가입된 회원인지 확인
		WorkspaceMember workspaceMember = workspaceMemberRepository.findByMemberAndWorkspaceAndIsDeleted(member, workspace, IsDeleted.N)
				.orElse(null);

		if (workspaceMember != null) {
			throw new IllegalArgumentException("이미 가입된 회원입니다.");
		}

		// 회원 가입 처리
		workspaceMember = WorkspaceMember.builder()
				.member(member)
				.workspace(workspace)
				.wsRole(WsRole.USER)
				.build();

		workspaceMemberRepository.save(workspaceMember);
		searchService.indexWorkspaceMember(workspaceId, workspaceMember);
		return workspace.fromEntity();
	}
}
