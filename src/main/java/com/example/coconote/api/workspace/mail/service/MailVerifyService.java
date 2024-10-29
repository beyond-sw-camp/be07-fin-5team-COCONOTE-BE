package com.example.coconote.api.workspace.mail.service;

import com.example.coconote.api.channel.channel.entity.Channel;
import com.example.coconote.api.channel.channel.entity.ChannelType;
import com.example.coconote.api.channel.channelMember.entity.ChannelMember;
import com.example.coconote.api.channel.channelMember.repository.ChannelMemberRepository;
import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.member.repository.MemberRepository;
import com.example.coconote.api.search.dto.EntityType;
import com.example.coconote.api.search.dto.IndexEntityMessage;
import com.example.coconote.api.search.entity.ChannelDocument;
import com.example.coconote.api.search.entity.WorkspaceMemberDocument;
import com.example.coconote.api.search.mapper.WorkspaceMemberMapper;
import com.example.coconote.api.search.service.SearchService;
import com.example.coconote.api.section.entity.Section;
import com.example.coconote.api.section.entity.SectionType;
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
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MailVerifyService {

	private final SearchService searchService;
	// JWT Secret Key를 환경 변수나 설정 파일에서 불러옴
	@Value("${jwt.secret}")
	private String jwtSecret;

	private final long jwtExpiration = 86400000; // 24시간

	private final JavaMailSender mailSender;
	private final MemberRepository memberRepository;
	private final WorkspaceMemberRepository workspaceMemberRepository;
	private final ChannelMemberRepository channelMemberRepository;
	private final WorkspaceRepository workspaceRepository;
	private final WorkspaceMemberMapper workspaceMemberMapper;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Value("${spring.mail.auth-code-expiration-millis}")
	private long authCodeExpirationMillis;



	// 메일 전송
	public void sendEmail(MailReqDto mailReqDto) throws MessagingException {
//		SimpleMailMessage message = new SimpleMailMessage();
//		message.setTo(mailReqDto.getReceiver());
//		message.setSubject(mailReqDto.getTitle());
//		message.setText(mailReqDto.getContents());
//		mailSender.send(message); // 이메일 전송

		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		helper.setTo(mailReqDto.getReceiver());
		helper.setSubject(mailReqDto.getTitle());
		helper.setText(mailReqDto.getContents(), true); // 두 번째 파라미터 true로 HTML 설정

		mailSender.send(message); // 이메일 전송
	}

	@Transactional
	public void sendCodeToEmail(Long workspaceId, String receiverEmail, String senderEmail) throws MessagingException {
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
		String memberNickname = member.getNickname();
		String workspaceName = workspace.getName();

		// 이메일 내용 생성
		String title = "[COCONOTE] "+workspaceName+" 워크스페이스 초대 메일 안내";
//		String contents = "워크스페이스에 가입하려면 다음 링크를 클릭하세요: " + invitationLink;
		String contents = "<!--\n" +
				"* This email was built using Tabular.\n" +
				"* For more information, visit https://tabular.email\n" +
				"-->\n" +
				"<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
				"<html\n" +
				"  xmlns=\"http://www.w3.org/1999/xhtml\"\n" +
				"  xmlns:v=\"urn:schemas-microsoft-com:vml\"\n" +
				"  xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n" +
				"  lang=\"en\"\n" +
				">\n" +
				"  <head>\n" +
				"    <title></title>\n" +
				"    <meta charset=\"UTF-8\" />\n" +
				"    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
				"    <!--[if !mso]>-->\n" +
				"    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" />\n" +
				"    <!--<![endif]-->\n" +
				"    <meta name=\"x-apple-disable-message-reformatting\" content=\"\" />\n" +
				"    <meta content=\"target-densitydpi=device-dpi\" name=\"viewport\" />\n" +
				"    <meta content=\"true\" name=\"HandheldFriendly\" />\n" +
				"    <meta content=\"width=device-width\" name=\"viewport\" />\n" +
				"    <meta\n" +
				"      name=\"format-detection\"\n" +
				"      content=\"telephone=no, date=no, address=no, email=no, url=no\"\n" +
				"    />\n" +
				"    <style type=\"text/css\">\n" +
				"      table {\n" +
				"        border-collapse: separate;\n" +
				"        table-layout: fixed;\n" +
				"        mso-table-lspace: 0pt;\n" +
				"        mso-table-rspace: 0pt;\n" +
				"      }\n" +
				"      table td {\n" +
				"        border-collapse: collapse;\n" +
				"      }\n" +
				"      .ExternalClass {\n" +
				"        width: 100%;\n" +
				"      }\n" +
				"      .ExternalClass,\n" +
				"      .ExternalClass p,\n" +
				"      .ExternalClass span,\n" +
				"      .ExternalClass font,\n" +
				"      .ExternalClass td,\n" +
				"      .ExternalClass div {\n" +
				"        line-height: 100%;\n" +
				"      }\n" +
				"      .gmail-mobile-forced-width {\n" +
				"        display: none;\n" +
				"        display: none !important;\n" +
				"      }\n" +
				"      body,\n" +
				"      a,\n" +
				"      li,\n" +
				"      p,\n" +
				"      h1,\n" +
				"      h2,\n" +
				"      h3 {\n" +
				"        -ms-text-size-adjust: 100%;\n" +
				"        -webkit-text-size-adjust: 100%;\n" +
				"      }\n" +
				"      html {\n" +
				"        -webkit-text-size-adjust: none !important;\n" +
				"      }\n" +
				"      body,\n" +
				"      #innerTable {\n" +
				"        -webkit-font-smoothing: antialiased;\n" +
				"        -moz-osx-font-smoothing: grayscale;\n" +
				"      }\n" +
				"      #innerTable img + div {\n" +
				"        display: none;\n" +
				"        display: none !important;\n" +
				"      }\n" +
				"      img {\n" +
				"        margin: 0;\n" +
				"        padding: 0;\n" +
				"        -ms-interpolation-mode: bicubic;\n" +
				"      }\n" +
				"      h1,\n" +
				"      h2,\n" +
				"      h3,\n" +
				"      p,\n" +
				"      a {\n" +
				"        line-height: inherit;\n" +
				"        overflow-wrap: normal;\n" +
				"        white-space: normal;\n" +
				"        word-break: break-word;\n" +
				"      }\n" +
				"      a {\n" +
				"        text-decoration: none;\n" +
				"      }\n" +
				"      h1,\n" +
				"      h2,\n" +
				"      h3,\n" +
				"      p {\n" +
				"        min-width: 100% !important;\n" +
				"        width: 100% !important;\n" +
				"        max-width: 100% !important;\n" +
				"        display: inline-block !important;\n" +
				"        border: 0;\n" +
				"        padding: 0;\n" +
				"        margin: 0;\n" +
				"      }\n" +
				"      a[x-apple-data-detectors] {\n" +
				"        color: inherit !important;\n" +
				"        text-decoration: none !important;\n" +
				"        font-size: inherit !important;\n" +
				"        font-family: inherit !important;\n" +
				"        font-weight: inherit !important;\n" +
				"        line-height: inherit !important;\n" +
				"      }\n" +
				"      u + #body a {\n" +
				"        color: inherit;\n" +
				"        text-decoration: none;\n" +
				"        font-size: inherit;\n" +
				"        font-family: inherit;\n" +
				"        font-weight: inherit;\n" +
				"        line-height: inherit;\n" +
				"      }\n" +
				"      a[href^=\"mailto\"],\n" +
				"      a[href^=\"tel\"],\n" +
				"      a[href^=\"sms\"] {\n" +
				"        color: inherit;\n" +
				"        text-decoration: none;\n" +
				"      }\n" +
				"    </style>\n" +
				"    <style type=\"text/css\">\n" +
				"      @media (min-width: 481px) {\n" +
				"        .hd {\n" +
				"          display: none !important;\n" +
				"        }\n" +
				"      }\n" +
				"    </style>\n" +
				"    <style type=\"text/css\">\n" +
				"      @media (max-width: 480px) {\n" +
				"        .hm {\n" +
				"          display: none !important;\n" +
				"        }\n" +
				"      }\n" +
				"    </style>\n" +
				"    <style type=\"text/css\">\n" +
				"      @media (max-width: 480px) {\n" +
				"        .t43 {\n" +
				"          padding: 0 0 22px !important;\n" +
				"          width: 480px !important;\n" +
				"        }\n" +
				"        .t30,\n" +
				"        .t39,\n" +
				"        .t51,\n" +
				"        .t6 {\n" +
				"          text-align: center !important;\n" +
				"        }\n" +
				"        .t29,\n" +
				"        .t38,\n" +
				"        .t5,\n" +
				"        .t50 {\n" +
				"          vertical-align: top !important;\n" +
				"          width: 600px !important;\n" +
				"        }\n" +
				"        .t10,\n" +
				"        .t34,\n" +
				"        .t55 {\n" +
				"          width: 480px !important;\n" +
				"        }\n" +
				"        .t3 {\n" +
				"          border-top-left-radius: 0 !important;\n" +
				"          border-top-right-radius: 0 !important;\n" +
				"          padding: 20px 30px !important;\n" +
				"        }\n" +
				"        .t27 {\n" +
				"          border-bottom-right-radius: 0 !important;\n" +
				"          border-bottom-left-radius: 0 !important;\n" +
				"          padding: 30px !important;\n" +
				"        }\n" +
				"        .t57 {\n" +
				"          mso-line-height-alt: 20px !important;\n" +
				"          line-height: 20px !important;\n" +
				"        }\n" +
				"        .t46 {\n" +
				"          width: 380px !important;\n" +
				"        }\n" +
				"        .t1 {\n" +
				"          width: 44px !important;\n" +
				"        }\n" +
				"        .t20 {\n" +
				"          width: 420px !important;\n" +
				"        }\n" +
				"      }\n" +
				"    </style>\n" +
				"    <style type=\"text/css\">\n" +
				"      @media (max-width: 480px) {\n" +
				"        [class~=\"x_t43\"] {\n" +
				"          padding-left: 0px !important;\n" +
				"          padding-top: 0px !important;\n" +
				"          padding-bottom: 22px !important;\n" +
				"          padding-right: 0px !important;\n" +
				"          width: 480px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t39\"] {\n" +
				"          text-align: center !important;\n" +
				"        }\n" +
				"        [class~=\"x_t38\"] {\n" +
				"          vertical-align: top !important;\n" +
				"          width: 600px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t10\"] {\n" +
				"          width: 480px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t6\"] {\n" +
				"          text-align: center !important;\n" +
				"        }\n" +
				"        [class~=\"x_t5\"] {\n" +
				"          vertical-align: top !important;\n" +
				"          width: 600px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t3\"] {\n" +
				"          border-top-left-radius: 0px !important;\n" +
				"          border-top-right-radius: 0px !important;\n" +
				"          padding-left: 30px !important;\n" +
				"          padding-top: 20px !important;\n" +
				"          padding-bottom: 20px !important;\n" +
				"          padding-right: 30px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t34\"] {\n" +
				"          width: 480px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t30\"] {\n" +
				"          text-align: center !important;\n" +
				"        }\n" +
				"        [class~=\"x_t29\"] {\n" +
				"          vertical-align: top !important;\n" +
				"          width: 600px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t27\"] {\n" +
				"          border-bottom-right-radius: 0px !important;\n" +
				"          border-bottom-left-radius: 0px !important;\n" +
				"          padding-left: 30px !important;\n" +
				"          padding-top: 30px !important;\n" +
				"          padding-bottom: 30px !important;\n" +
				"          padding-right: 30px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t57\"] {\n" +
				"          mso-line-height-alt: 20px !important;\n" +
				"          line-height: 20px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t55\"] {\n" +
				"          width: 480px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t51\"] {\n" +
				"          text-align: center !important;\n" +
				"        }\n" +
				"        [class~=\"x_t50\"] {\n" +
				"          vertical-align: top !important;\n" +
				"          width: 600px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t46\"] {\n" +
				"          width: 380px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t1\"] {\n" +
				"          width: 44px !important;\n" +
				"        }\n" +
				"        [class~=\"x_t20\"] {\n" +
				"          width: 420px !important;\n" +
				"        }\n" +
				"      }\n" +
				"    </style>\n" +
				"    <!--[if !mso]>-->\n" +
				"    <link\n" +
				"      href=\"https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@500;700;800&amp;family=Albert+Sans:wght@500&amp;display=swap\"\n" +
				"      rel=\"stylesheet\"\n" +
				"      type=\"text/css\"\n" +
				"    />\n" +
				"    <!--<![endif]-->\n" +
				"    <!--[if mso]>\n" +
				"      <xml>\n" +
				"        <o:OfficeDocumentSettings>\n" +
				"          <o:AllowPNG />\n" +
				"          <o:PixelsPerInch>96</o:PixelsPerInch>\n" +
				"        </o:OfficeDocumentSettings>\n" +
				"      </xml>\n" +
				"    <![endif]-->\n" +
				"  </head>\n" +
				"  <body\n" +
				"    id=\"body\"\n" +
				"    class=\"t60\"\n" +
				"    style=\"\n" +
				"      min-width: 100%;\n" +
				"      margin: 0px;\n" +
				"      padding: 0px;\n" +
				"      background-color: #e0e0e0;\n" +
				"    \"\n" +
				"  >\n" +
				"    <div class=\"t59\" style=\"background-color: #e0e0e0\">\n" +
				"      <table\n" +
				"        role=\"presentation\"\n" +
				"        width=\"100%\"\n" +
				"        cellpadding=\"0\"\n" +
				"        cellspacing=\"0\"\n" +
				"        border=\"0\"\n" +
				"        align=\"center\"\n" +
				"      >\n" +
				"        <tr>\n" +
				"          <td\n" +
				"            class=\"t58\"\n" +
				"            style=\"\n" +
				"              font-size: 0;\n" +
				"              line-height: 0;\n" +
				"              mso-line-height-rule: exactly;\n" +
				"              background-color: #e0e0e0;\n" +
				"            \"\n" +
				"            valign=\"top\"\n" +
				"            align=\"center\"\n" +
				"          >\n" +
				"            <!--[if mso]>\n" +
				"              <v:background\n" +
				"                xmlns:v=\"urn:schemas-microsoft-com:vml\"\n" +
				"                fill=\"true\"\n" +
				"                stroke=\"false\"\n" +
				"              >\n" +
				"                <v:fill color=\"#E0E0E0\" />\n" +
				"              </v:background>\n" +
				"            <![endif]-->\n" +
				"            <table\n" +
				"              role=\"presentation\"\n" +
				"              width=\"100%\"\n" +
				"              cellpadding=\"0\"\n" +
				"              cellspacing=\"0\"\n" +
				"              border=\"0\"\n" +
				"              align=\"center\"\n" +
				"              id=\"innerTable\"\n" +
				"            >\n" +
				"              <tr>\n" +
				"                <td align=\"center\">\n" +
				"                  <table\n" +
				"                    class=\"t44\"\n" +
				"                    role=\"presentation\"\n" +
				"                    cellpadding=\"0\"\n" +
				"                    cellspacing=\"0\"\n" +
				"                    style=\"margin-left: auto; margin-right: auto\"\n" +
				"                  >\n" +
				"                    <tr>\n" +
				"                      <!--[if mso]>\n" +
				"<td width=\"566\" class=\"t43\" style=\"padding:50px 10px 31px 10px;\">\n" +
				"<![endif]-->\n" +
				"                      <!--[if !mso]>-->\n" +
				"                      <td\n" +
				"                        class=\"t43\"\n" +
				"                        style=\"width: 546px; padding: 50px 10px 31px 10px\"\n" +
				"                      >\n" +
				"                        <!--<![endif]-->\n" +
				"                        <div\n" +
				"                          class=\"t42\"\n" +
				"                          style=\"width: 100%; text-align: center\"\n" +
				"                        >\n" +
				"                          <div class=\"t41\" style=\"display: inline-block\">\n" +
				"                            <table\n" +
				"                              class=\"t40\"\n" +
				"                              role=\"presentation\"\n" +
				"                              cellpadding=\"0\"\n" +
				"                              cellspacing=\"0\"\n" +
				"                              align=\"center\"\n" +
				"                              valign=\"top\"\n" +
				"                            >\n" +
				"                              <tr class=\"t39\">\n" +
				"                                <td></td>\n" +
				"                                <td class=\"t38\" width=\"546\" valign=\"top\">\n" +
				"                                  <table\n" +
				"                                    role=\"presentation\"\n" +
				"                                    width=\"100%\"\n" +
				"                                    cellpadding=\"0\"\n" +
				"                                    cellspacing=\"0\"\n" +
				"                                    class=\"t37\"\n" +
				"                                    style=\"width: 100%\"\n" +
				"                                  >\n" +
				"                                    <tr>\n" +
				"                                      <td\n" +
				"                                        class=\"t36\"\n" +
				"                                        style=\"background-color: transparent\"\n" +
				"                                      >\n" +
				"                                        <table\n" +
				"                                          role=\"presentation\"\n" +
				"                                          width=\"100%\"\n" +
				"                                          cellpadding=\"0\"\n" +
				"                                          cellspacing=\"0\"\n" +
				"                                          style=\"width: 100% !important\"\n" +
				"                                        >\n" +
				"                                          <tr>\n" +
				"                                            <td align=\"center\">\n" +
				"                                              <table\n" +
				"                                                class=\"t11\"\n" +
				"                                                role=\"presentation\"\n" +
				"                                                cellpadding=\"0\"\n" +
				"                                                cellspacing=\"0\"\n" +
				"                                                style=\"\n" +
				"                                                  margin-left: auto;\n" +
				"                                                  margin-right: auto;\n" +
				"                                                \"\n" +
				"                                              >\n" +
				"                                                <tr>\n" +
				"                                                  <!--[if mso]>\n" +
				"<td width=\"546\" class=\"t10\">\n" +
				"<![endif]-->\n" +
				"                                                  <!--[if !mso]>-->\n" +
				"                                                  <td\n" +
				"                                                    class=\"t10\"\n" +
				"                                                    style=\"width: 546px\"\n" +
				"                                                  >\n" +
				"                                                    <!--<![endif]-->\n" +
				"                                                    <div\n" +
				"                                                      class=\"t9\"\n" +
				"                                                      style=\"\n" +
				"                                                        width: 100%;\n" +
				"                                                        text-align: center;\n" +
				"                                                      \"\n" +
				"                                                    >\n" +
				"                                                      <div\n" +
				"                                                        class=\"t8\"\n" +
				"                                                        style=\"\n" +
				"                                                          display: inline-block;\n" +
				"                                                        \"\n" +
				"                                                      >\n" +
				"                                                        <table\n" +
				"                                                          class=\"t7\"\n" +
				"                                                          role=\"presentation\"\n" +
				"                                                          cellpadding=\"0\"\n" +
				"                                                          cellspacing=\"0\"\n" +
				"                                                          align=\"center\"\n" +
				"                                                          valign=\"top\"\n" +
				"                                                        >\n" +
				"                                                          <tr class=\"t6\">\n" +
				"                                                            <td></td>\n" +
				"                                                            <td\n" +
				"                                                              class=\"t5\"\n" +
				"                                                              width=\"546\"\n" +
				"                                                              valign=\"top\"\n" +
				"                                                            >\n" +
				"                                                              <table\n" +
				"                                                                role=\"presentation\"\n" +
				"                                                                width=\"100%\"\n" +
				"                                                                cellpadding=\"0\"\n" +
				"                                                                cellspacing=\"0\"\n" +
				"                                                                class=\"t4\"\n" +
				"                                                                style=\"\n" +
				"                                                                  width: 100%;\n" +
				"                                                                \"\n" +
				"                                                              >\n" +
				"                                                                <tr>\n" +
				"                                                                  <td\n" +
				"                                                                    class=\"t3\"\n" +
				"                                                                    style=\"\n" +
				"                                                                      overflow: hidden;\n" +
				"                                                                      background-color: #162138;\n" +
				"                                                                      padding: 49px\n" +
				"                                                                        50px\n" +
				"                                                                        42px\n" +
				"                                                                        50px;\n" +
				"                                                                      border-radius: 18px\n" +
				"                                                                        18px 0 0;\n" +
				"                                                                    \"\n" +
				"                                                                  >\n" +
				"                                                                    <table\n" +
				"                                                                      role=\"presentation\"\n" +
				"                                                                      width=\"100%\"\n" +
				"                                                                      cellpadding=\"0\"\n" +
				"                                                                      cellspacing=\"0\"\n" +
				"                                                                      style=\"\n" +
				"                                                                        width: 100% !important;\n" +
				"                                                                      \"\n" +
				"                                                                    >\n" +
				"                                                                      <tr>\n" +
				"                                                                        <td\n" +
				"                                                                          align=\"left\"\n" +
				"                                                                        >\n" +
				"                                                                          <table\n" +
				"                                                                            class=\"t2\"\n" +
				"                                                                            role=\"presentation\"\n" +
				"                                                                            cellpadding=\"0\"\n" +
				"                                                                            cellspacing=\"0\"\n" +
				"                                                                            style=\"\n" +
				"                                                                              margin-right: auto;\n" +
				"                                                                            \"\n" +
				"                                                                          >\n" +
				"                                                                            <tr>\n" +
				"                                                                              <!--[if mso]>\n" +
				"<td width=\"259\" class=\"t1\">\n" +
				"<![endif]-->\n" +
				"                                                                              <!--[if !mso]>-->\n" +
				"                                                                              <td\n" +
				"                                                                                class=\"t1\"\n" +
				"                                                                                style=\"\n" +
				"                                                                                  width: 259px;\n" +
				"                                                                                \"\n" +
				"                                                                              >\n" +
				"                                                                                <!--<![endif]-->\n" +
				"                                                                                <div\n" +
				"                                                                                  style=\"\n" +
				"                                                                                    font-size: 0px;\n" +
				"                                                                                  \"\n" +
				"                                                                                >\n" +
				"                                                                                  <img\n" +
				"                                                                                    class=\"t0\"\n" +
				"                                                                                    style=\"\n" +
				"                                                                                      display: block;\n" +
				"                                                                                      border: 0;\n" +
				"                                                                                      height: auto;\n" +
				"                                                                                      width: 100%;\n" +
				"                                                                                      margin: 0;\n" +
				"                                                                                      max-width: 100%;\n" +
				"                                                                                    \"\n" +
				"                                                                                    width=\"259\"\n" +
				"                                                                                    height=\"37.28125\"\n" +
				"                                                                                    alt=\"\"\n" +
				"                                                                                    src=\"https://b4351c77-647e-49d9-b437-31ea93fda96e.b-cdn.net/e/d5250c1d-300a-4d64-b871-36d22bdde9bc/49dd3a32-900b-449e-a381-3d23556f9991.png\"\n" +
				"                                                                                  />\n" +
				"                                                                                </div>\n" +
				"                                                                              </td>\n" +
				"                                                                            </tr>\n" +
				"                                                                          </table>\n" +
				"                                                                        </td>\n" +
				"                                                                      </tr>\n" +
				"                                                                    </table>\n" +
				"                                                                  </td>\n" +
				"                                                                </tr>\n" +
				"                                                              </table>\n" +
				"                                                            </td>\n" +
				"                                                            <td></td>\n" +
				"                                                          </tr>\n" +
				"                                                        </table>\n" +
				"                                                      </div>\n" +
				"                                                    </div>\n" +
				"                                                  </td>\n" +
				"                                                </tr>\n" +
				"                                              </table>\n" +
				"                                            </td>\n" +
				"                                          </tr>\n" +
				"                                          <tr>\n" +
				"                                            <td align=\"center\">\n" +
				"                                              <table\n" +
				"                                                class=\"t35\"\n" +
				"                                                role=\"presentation\"\n" +
				"                                                cellpadding=\"0\"\n" +
				"                                                cellspacing=\"0\"\n" +
				"                                                style=\"\n" +
				"                                                  margin-left: auto;\n" +
				"                                                  margin-right: auto;\n" +
				"                                                \"\n" +
				"                                              >\n" +
				"                                                <tr>\n" +
				"                                                  <!--[if mso]>\n" +
				"<td width=\"546\" class=\"t34\">\n" +
				"<![endif]-->\n" +
				"                                                  <!--[if !mso]>-->\n" +
				"                                                  <td\n" +
				"                                                    class=\"t34\"\n" +
				"                                                    style=\"width: 546px\"\n" +
				"                                                  >\n" +
				"                                                    <!--<![endif]-->\n" +
				"                                                    <div\n" +
				"                                                      class=\"t33\"\n" +
				"                                                      style=\"\n" +
				"                                                        width: 100%;\n" +
				"                                                        text-align: center;\n" +
				"                                                      \"\n" +
				"                                                    >\n" +
				"                                                      <div\n" +
				"                                                        class=\"t32\"\n" +
				"                                                        style=\"\n" +
				"                                                          display: inline-block;\n" +
				"                                                        \"\n" +
				"                                                      >\n" +
				"                                                        <table\n" +
				"                                                          class=\"t31\"\n" +
				"                                                          role=\"presentation\"\n" +
				"                                                          cellpadding=\"0\"\n" +
				"                                                          cellspacing=\"0\"\n" +
				"                                                          align=\"center\"\n" +
				"                                                          valign=\"top\"\n" +
				"                                                        >\n" +
				"                                                          <tr class=\"t30\">\n" +
				"                                                            <td></td>\n" +
				"                                                            <td\n" +
				"                                                              class=\"t29\"\n" +
				"                                                              width=\"546\"\n" +
				"                                                              valign=\"top\"\n" +
				"                                                            >\n" +
				"                                                              <table\n" +
				"                                                                role=\"presentation\"\n" +
				"                                                                width=\"100%\"\n" +
				"                                                                cellpadding=\"0\"\n" +
				"                                                                cellspacing=\"0\"\n" +
				"                                                                class=\"t28\"\n" +
				"                                                                style=\"\n" +
				"                                                                  width: 100%;\n" +
				"                                                                \"\n" +
				"                                                              >\n" +
				"                                                                <tr>\n" +
				"                                                                  <td\n" +
				"                                                                    class=\"t27\"\n" +
				"                                                                    style=\"\n" +
				"                                                                      overflow: hidden;\n" +
				"                                                                      background-color: #f8f8f8;\n" +
				"                                                                      padding: 40px\n" +
				"                                                                        50px\n" +
				"                                                                        40px\n" +
				"                                                                        50px;\n" +
				"                                                                      border-radius: 0\n" +
				"                                                                        0 18px\n" +
				"                                                                        18px;\n" +
				"                                                                    \"\n" +
				"                                                                  >\n" +
				"                                                                    <table\n" +
				"                                                                      role=\"presentation\"\n" +
				"                                                                      width=\"100%\"\n" +
				"                                                                      cellpadding=\"0\"\n" +
				"                                                                      cellspacing=\"0\"\n" +
				"                                                                      style=\"\n" +
				"                                                                        width: 100% !important;\n" +
				"                                                                      \"\n" +
				"                                                                    >\n" +
				"                                                                      <tr>\n" +
				"                                                                        <td\n" +
				"                                                                          align=\"left\"\n" +
				"                                                                        >\n" +
				"                                                                          <table\n" +
				"                                                                            class=\"t15\"\n" +
				"                                                                            role=\"presentation\"\n" +
				"                                                                            cellpadding=\"0\"\n" +
				"                                                                            cellspacing=\"0\"\n" +
				"                                                                            style=\"\n" +
				"                                                                              margin-right: auto;\n" +
				"                                                                            \"\n" +
				"                                                                          >\n" +
				"                                                                            <tr>\n" +
				"                                                                              <!--[if mso]>\n" +
				"<td width=\"381\" class=\"t14\">\n" +
				"<![endif]-->\n" +
				"                                                                              <!--[if !mso]>-->\n" +
				"                                                                              <td\n" +
				"                                                                                class=\"t14\"\n" +
				"                                                                                style=\"\n" +
				"                                                                                  width: 381px;\n" +
				"                                                                                \"\n" +
				"                                                                              >\n" +
				"                                                                                <!--<![endif]-->\n" +
				"                                                                                <h1\n" +
				"                                                                                  class=\"t13\"\n" +
				"                                                                                  style=\"\n" +
				"                                                                                    margin: 0;\n" +
				"                                                                                    margin: 0;\n" +
				"                                                                                    font-family: Noto\n" +
				"                                                                                        Sans\n" +
				"                                                                                        KR,\n" +
				"                                                                                      BlinkMacSystemFont,\n" +
				"                                                                                      Segoe\n" +
				"                                                                                        UI,\n" +
				"                                                                                      Helvetica\n" +
				"                                                                                        Neue,\n" +
				"                                                                                      Arial,\n" +
				"                                                                                      sans-serif;\n" +
				"                                                                                    line-height: 41px;\n" +
				"                                                                                    font-weight: 800;\n" +
				"                                                                                    font-style: normal;\n" +
				"                                                                                    font-size: 30px;\n" +
				"                                                                                    text-decoration: none;\n" +
				"                                                                                    text-transform: none;\n" +
				"                                                                                    letter-spacing: -1.56px;\n" +
				"                                                                                    direction: ltr;\n" +
				"                                                                                    color: #191919;\n" +
				"                                                                                    text-align: left;\n" +
				"                                                                                    mso-line-height-rule: exactly;\n" +
				"                                                                                    mso-text-raise: 3px;\n" +
				"                                                                                  \"\n" +
				"                                                                                >\n" +
				"                                                                                  <span\n" +
				"                                                                                    class=\"t12\"\n" +
				"                                                                                    style=\"\n" +
				"                                                                                      margin: 0;\n" +
				"                                                                                      margin: 0;\n" +
				"                                                                                      color: #69a0f2;\n" +
				"                                                                                      mso-line-height-rule: exactly;\n" +
				"                                                                                    \"\n" +
				"                                                                                    >"+workspaceName+"워크스페이스</span\n" +
				"                                                                                  >\n" +
				"                                                                                  로<br />초대\n" +
				"                                                                                  합니다!\n" +
				"                                                                                </h1>\n" +
				"                                                                              </td>\n" +
				"                                                                            </tr>\n" +
				"                                                                          </table>\n" +
				"                                                                        </td>\n" +
				"                                                                      </tr>\n" +
				"                                                                      <tr>\n" +
				"                                                                        <td>\n" +
				"                                                                          <div\n" +
				"                                                                            class=\"t16\"\n" +
				"                                                                            style=\"\n" +
				"                                                                              mso-line-height-rule: exactly;\n" +
				"                                                                              mso-line-height-alt: 25px;\n" +
				"                                                                              line-height: 25px;\n" +
				"                                                                              font-size: 1px;\n" +
				"                                                                              display: block;\n" +
				"                                                                            \"\n" +
				"                                                                          >\n" +
				"                                                                            &nbsp;&nbsp;\n" +
				"                                                                          </div>\n" +
				"                                                                        </td>\n" +
				"                                                                      </tr>\n" +
				"                                                                      <tr>\n" +
				"                                                                        <td\n" +
				"                                                                          align=\"left\"\n" +
				"                                                                        >\n" +
				"                                                                          <table\n" +
				"                                                                            class=\"t21\"\n" +
				"                                                                            role=\"presentation\"\n" +
				"                                                                            cellpadding=\"0\"\n" +
				"                                                                            cellspacing=\"0\"\n" +
				"                                                                            style=\"\n" +
				"                                                                              margin-right: auto;\n" +
				"                                                                            \"\n" +
				"                                                                          >\n" +
				"                                                                            <tr>\n" +
				"                                                                              <!--[if mso]>\n" +
				"<td width=\"446\" class=\"t20\">\n" +
				"<![endif]-->\n" +
				"                                                                              <!--[if !mso]>-->\n" +
				"                                                                              <td\n" +
				"                                                                                class=\"t20\"\n" +
				"                                                                                style=\"\n" +
				"                                                                                  width: 446px;\n" +
				"                                                                                \"\n" +
				"                                                                              >\n" +
				"                                                                                <!--<![endif]-->\n" +
				"                                                                                <p\n" +
				"                                                                                  class=\"t19\"\n" +
				"                                                                                  style=\"\n" +
				"                                                                                    margin: 0;\n" +
				"                                                                                    margin: 0;\n" +
				"                                                                                    font-family: Noto\n" +
				"                                                                                        Sans\n" +
				"                                                                                        KR,\n" +
				"                                                                                      BlinkMacSystemFont,\n" +
				"                                                                                      Segoe\n" +
				"                                                                                        UI,\n" +
				"                                                                                      Helvetica\n" +
				"                                                                                        Neue,\n" +
				"                                                                                      Arial,\n" +
				"                                                                                      sans-serif;\n" +
				"                                                                                    line-height: 22px;\n" +
				"                                                                                    font-weight: 500;\n" +
				"                                                                                    font-style: normal;\n" +
				"                                                                                    font-size: 14px;\n" +
				"                                                                                    text-decoration: none;\n" +
				"                                                                                    text-transform: none;\n" +
				"                                                                                    letter-spacing: -0.56px;\n" +
				"                                                                                    direction: ltr;\n" +
				"                                                                                    color: #333333;\n" +
				"                                                                                    text-align: left;\n" +
				"                                                                                    mso-line-height-rule: exactly;\n" +
				"                                                                                    mso-text-raise: 2px;\n" +
				"                                                                                  \"\n" +
				"                                                                                >\n" +
				"                                                                                  <span\n" +
				"                                                                                    class=\"t17\"\n" +
				"                                                                                    style=\"\n" +
				"                                                                                      margin: 0;\n" +
				"                                                                                      margin: 0;\n" +
				"                                                                                      font-weight: 700;\n" +
				"                                                                                      mso-line-height-rule: exactly;\n" +
				"                                                                                    \"\n" +
				"                                                                                    >"+memberNickname+"님</span\n" +
				"                                                                                  >\n" +
				"                                                                                  안녕하세요.\n" +
				"                                                                                  코코노트를\n" +
				"                                                                                  이용해주셔서\n" +
				"                                                                                  감사합니다.<br />아래\n" +
				"                                                                                  <span\n" +
				"                                                                                    class=\"t18\"\n" +
				"                                                                                    style=\"\n" +
				"                                                                                      margin: 0;\n" +
				"                                                                                      margin: 0;\n" +
				"                                                                                      font-weight: 700;\n" +
				"                                                                                      mso-line-height-rule: exactly;\n" +
				"                                                                                    \"\n" +
				"                                                                                    >참여하기 </span\n" +
				"                                                                                  >버튼을\n" +
				"                                                                                  통해\n" +
				"                                                                                  코코노트와\n" +
				"                                                                                  함께\n" +
				"                                                                                  지식을\n" +
				"                                                                                  관리하세요!\n" +
				"                                                                                </p>\n" +
				"                                                                              </td>\n" +
				"                                                                            </tr>\n" +
				"                                                                          </table>\n" +
				"                                                                        </td>\n" +
				"                                                                      </tr>\n" +
				"                                                                      <tr>\n" +
				"                                                                        <td>\n" +
				"                                                                          <div\n" +
				"                                                                            class=\"t22\"\n" +
				"                                                                            style=\"\n" +
				"                                                                              mso-line-height-rule: exactly;\n" +
				"                                                                              mso-line-height-alt: 15px;\n" +
				"                                                                              line-height: 15px;\n" +
				"                                                                              font-size: 1px;\n" +
				"                                                                              display: block;\n" +
				"                                                                            \"\n" +
				"                                                                          >\n" +
				"                                                                            &nbsp;&nbsp;\n" +
				"                                                                          </div>\n" +
				"                                                                        </td>\n" +
				"                                                                      </tr>\n" +
				"                                                                      <tr>\n" +
				"                                                                        <td\n" +
				"                                                                          align=\"left\"\n" +
				"                                                                        >\n" +
				"                                                                          <table\n" +
				"                                                                            class=\"t25\"\n" +
				"                                                                            role=\"presentation\"\n" +
				"                                                                            cellpadding=\"0\"\n" +
				"                                                                            cellspacing=\"0\"\n" +
				"                                                                            style=\"\n" +
				"                                                                              margin-right: auto;\n" +
				"                                                                            \"\n" +
				"                                                                          >\n" +
				"                                                                            <tr>\n" +
				"                                                                              <!--[if mso]>\n" +
				"<td width=\"234\" class=\"t24\" style=\"background-color:#69A0F2;overflow:hidden;text-align:center;line-height:44px;mso-line-height-rule:exactly;mso-text-raise:8px;padding:0 30px 0 30px;border-radius:40px 40px 40px 40px;\">\n" +
				"<![endif]-->\n" +
				"                                                                              <!--[if !mso]>-->\n" +
				"                                                                              <td\n" +
				"                                                                                class=\"t24\"\n" +
				"                                                                                style=\"\n" +
				"                                                                                  background-color: #69a0f2;\n" +
				"                                                                                  overflow: hidden;\n" +
				"                                                                                  width: 174px;\n" +
				"                                                                                  text-align: center;\n" +
				"                                                                                  line-height: 44px;\n" +
				"                                                                                  mso-line-height-rule: exactly;\n" +
				"                                                                                  mso-text-raise: 8px;\n" +
				"                                                                                  padding: 0\n" +
				"                                                                                    30px\n" +
				"                                                                                    0\n" +
				"                                                                                    30px;\n" +
				"                                                                                  border-radius: 40px\n" +
				"                                                                                    40px\n" +
				"                                                                                    40px\n" +
				"                                                                                    40px;\n" +
				"                                                                                \"\n" +
				"                                                                              >\n" +
				"                                                                                <!--<![endif]-->\n" +
				"                                                                                <a\n" +
				"                                                                                  class=\"t23\"\n" +
				"                                                                                  href=\""+invitationLink+"\"\n" +
				"                                                                                  style=\"\n" +
				"                                                                                    display: block;\n" +
				"                                                                                    margin: 0;\n" +
				"                                                                                    margin: 0;\n" +
				"                                                                                    font-family: Noto\n" +
				"                                                                                        Sans\n" +
				"                                                                                        KR,\n" +
				"                                                                                      BlinkMacSystemFont,\n" +
				"                                                                                      Segoe\n" +
				"                                                                                        UI,\n" +
				"                                                                                      Helvetica\n" +
				"                                                                                        Neue,\n" +
				"                                                                                      Arial,\n" +
				"                                                                                      sans-serif;\n" +
				"                                                                                    line-height: 44px;\n" +
				"                                                                                    font-weight: 500;\n" +
				"                                                                                    font-style: normal;\n" +
				"                                                                                    font-size: 16px;\n" +
				"                                                                                    text-decoration: none;\n" +
				"                                                                                    text-transform: uppercase;\n" +
				"                                                                                    letter-spacing: 2.4px;\n" +
				"                                                                                    direction: ltr;\n" +
				"                                                                                    color: #ffffff;\n" +
				"                                                                                    text-align: center;\n" +
				"                                                                                    mso-line-height-rule: exactly;\n" +
				"                                                                                    mso-text-raise: 8px;\n" +
				"                                                                                  \"\n" +
				"                                                                                  target=\"_blank\"\n" +
				"                                                                                  >참여하기</a\n" +
				"                                                                                >\n" +
				"                                                                              </td>\n" +
				"                                                                            </tr>\n" +
				"                                                                          </table>\n" +
				"                                                                        </td>\n" +
				"                                                                      </tr>\n" +
				"                                                                      <tr>\n" +
				"                                                                        <td>\n" +
				"                                                                          <div\n" +
				"                                                                            class=\"t26\"\n" +
				"                                                                            style=\"\n" +
				"                                                                              mso-line-height-rule: exactly;\n" +
				"                                                                              mso-line-height-alt: 15px;\n" +
				"                                                                              line-height: 15px;\n" +
				"                                                                              font-size: 1px;\n" +
				"                                                                              display: block;\n" +
				"                                                                            \"\n" +
				"                                                                          >\n" +
				"                                                                            &nbsp;&nbsp;\n" +
				"                                                                          </div>\n" +
				"                                                                        </td>\n" +
				"                                                                      </tr>\n" +
				"                                                                    </table>\n" +
				"                                                                  </td>\n" +
				"                                                                </tr>\n" +
				"                                                              </table>\n" +
				"                                                            </td>\n" +
				"                                                            <td></td>\n" +
				"                                                          </tr>\n" +
				"                                                        </table>\n" +
				"                                                      </div>\n" +
				"                                                    </div>\n" +
				"                                                  </td>\n" +
				"                                                </tr>\n" +
				"                                              </table>\n" +
				"                                            </td>\n" +
				"                                          </tr>\n" +
				"                                        </table>\n" +
				"                                      </td>\n" +
				"                                    </tr>\n" +
				"                                  </table>\n" +
				"                                </td>\n" +
				"                                <td></td>\n" +
				"                              </tr>\n" +
				"                            </table>\n" +
				"                          </div>\n" +
				"                        </div>\n" +
				"                      </td>\n" +
				"                    </tr>\n" +
				"                  </table>\n" +
				"                </td>\n" +
				"              </tr>\n" +
				"              <tr>\n" +
				"                <td align=\"center\">\n" +
				"                  <table\n" +
				"                    class=\"t56\"\n" +
				"                    role=\"presentation\"\n" +
				"                    cellpadding=\"0\"\n" +
				"                    cellspacing=\"0\"\n" +
				"                    style=\"margin-left: auto; margin-right: auto\"\n" +
				"                  >\n" +
				"                    <tr>\n" +
				"                      <!--[if mso]>\n" +
				"<td width=\"600\" class=\"t55\">\n" +
				"<![endif]-->\n" +
				"                      <!--[if !mso]>-->\n" +
				"                      <td class=\"t55\" style=\"width: 600px\">\n" +
				"                        <!--<![endif]-->\n" +
				"                        <div\n" +
				"                          class=\"t54\"\n" +
				"                          style=\"width: 100%; text-align: center\"\n" +
				"                        >\n" +
				"                          <div class=\"t53\" style=\"display: inline-block\">\n" +
				"                            <table\n" +
				"                              class=\"t52\"\n" +
				"                              role=\"presentation\"\n" +
				"                              cellpadding=\"0\"\n" +
				"                              cellspacing=\"0\"\n" +
				"                              align=\"center\"\n" +
				"                              valign=\"top\"\n" +
				"                            >\n" +
				"                              <tr class=\"t51\">\n" +
				"                                <td></td>\n" +
				"                                <td class=\"t50\" width=\"600\" valign=\"top\">\n" +
				"                                  <table\n" +
				"                                    role=\"presentation\"\n" +
				"                                    width=\"100%\"\n" +
				"                                    cellpadding=\"0\"\n" +
				"                                    cellspacing=\"0\"\n" +
				"                                    class=\"t49\"\n" +
				"                                    style=\"width: 100%\"\n" +
				"                                  >\n" +
				"                                    <tr>\n" +
				"                                      <td\n" +
				"                                        class=\"t48\"\n" +
				"                                        style=\"padding: 0 50px 0 50px\"\n" +
				"                                      >\n" +
				"                                        <table\n" +
				"                                          role=\"presentation\"\n" +
				"                                          width=\"100%\"\n" +
				"                                          cellpadding=\"0\"\n" +
				"                                          cellspacing=\"0\"\n" +
				"                                          style=\"width: 100% !important\"\n" +
				"                                        >\n" +
				"                                          <tr>\n" +
				"                                            <td align=\"center\">\n" +
				"                                              <table\n" +
				"                                                class=\"t47\"\n" +
				"                                                role=\"presentation\"\n" +
				"                                                cellpadding=\"0\"\n" +
				"                                                cellspacing=\"0\"\n" +
				"                                                style=\"\n" +
				"                                                  margin-left: auto;\n" +
				"                                                  margin-right: auto;\n" +
				"                                                \"\n" +
				"                                              >\n" +
				"                                                <tr>\n" +
				"                                                  <!--[if mso]>\n" +
				"<td width=\"420\" class=\"t46\">\n" +
				"<![endif]-->\n" +
				"                                                  <!--[if !mso]>-->\n" +
				"                                                  <td\n" +
				"                                                    class=\"t46\"\n" +
				"                                                    style=\"width: 420px\"\n" +
				"                                                  >\n" +
				"                                                    <!--<![endif]-->\n" +
				"                                                    <p\n" +
				"                                                      class=\"t45\"\n" +
				"                                                      style=\"\n" +
				"                                                        margin: 0;\n" +
				"                                                        margin: 0;\n" +
				"                                                        font-family: Albert Sans,\n" +
				"                                                          BlinkMacSystemFont,\n" +
				"                                                          Segoe UI,\n" +
				"                                                          Helvetica Neue, Arial,\n" +
				"                                                          sans-serif;\n" +
				"                                                        line-height: 22px;\n" +
				"                                                        font-weight: 500;\n" +
				"                                                        font-style: normal;\n" +
				"                                                        font-size: 12px;\n" +
				"                                                        text-decoration: none;\n" +
				"                                                        text-transform: none;\n" +
				"                                                        direction: ltr;\n" +
				"                                                        color: #888888;\n" +
				"                                                        text-align: center;\n" +
				"                                                        mso-line-height-rule: exactly;\n" +
				"                                                        mso-text-raise: 3px;\n" +
				"                                                      \"\n" +
				"                                                    >\n" +
				"                                                      © 2024 coconote Inc. All\n" +
				"                                                      Rights Reserved<br />\n" +
				"                                                    </p>\n" +
				"                                                  </td>\n" +
				"                                                </tr>\n" +
				"                                              </table>\n" +
				"                                            </td>\n" +
				"                                          </tr>\n" +
				"                                        </table>\n" +
				"                                      </td>\n" +
				"                                    </tr>\n" +
				"                                  </table>\n" +
				"                                </td>\n" +
				"                                <td></td>\n" +
				"                              </tr>\n" +
				"                            </table>\n" +
				"                          </div>\n" +
				"                        </div>\n" +
				"                      </td>\n" +
				"                    </tr>\n" +
				"                  </table>\n" +
				"                </td>\n" +
				"              </tr>\n" +
				"              <tr>\n" +
				"                <td>\n" +
				"                  <div\n" +
				"                    class=\"t57\"\n" +
				"                    style=\"\n" +
				"                      mso-line-height-rule: exactly;\n" +
				"                      mso-line-height-alt: 50px;\n" +
				"                      line-height: 50px;\n" +
				"                      font-size: 1px;\n" +
				"                      display: block;\n" +
				"                    \"\n" +
				"                  >\n" +
				"                    &nbsp;&nbsp;\n" +
				"                  </div>\n" +
				"                </td>\n" +
				"              </tr>\n" +
				"            </table>\n" +
				"          </td>\n" +
				"        </tr>\n" +
				"      </table>\n" +
				"    </div>\n" +
				"    <div\n" +
				"      class=\"gmail-mobile-forced-width\"\n" +
				"      style=\"white-space: nowrap; font: 15px courier; line-height: 0\"\n" +
				"    >\n" +
				"      &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;\n" +
				"      &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;\n" +
				"      &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;\n" +
				"    </div>\n" +
				"  </body>\n" +
				"</html>\n";



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
				.memberName(member.getNickname())
				.nickname(member.getNickname())
				.wsRole(WsRole.USER)
				.build();

		workspaceMemberRepository.save(workspaceMember);


		// 기본채널은 무조건 채널 회원으로 등록됨
		for(Section s : workspace.getSections()) {
			if(s.getSectionType().equals(SectionType.DEFAULT)) {
				for(Channel c : s.getChannels()) {
					if(c.getChannelType().equals(ChannelType.DEFAULT)) {
						ChannelMember channelMember = ChannelMember.builder()
								.workspaceMember(workspaceMember)
								.channel(c)
								.build();
						channelMemberRepository.save(channelMember);					}
				}
			}
		}


		WorkspaceMemberDocument document = workspaceMemberMapper.toDocument(workspaceMember);
		IndexEntityMessage<WorkspaceMemberDocument> indexEntityMessage = new IndexEntityMessage<>(workspace.getWorkspaceId(), EntityType.WORKSPACE_MEMBER , document);
		kafkaTemplate.send("workspace_member_entity_search", indexEntityMessage.toJson());

		return workspace.fromEntity();
	}
}
