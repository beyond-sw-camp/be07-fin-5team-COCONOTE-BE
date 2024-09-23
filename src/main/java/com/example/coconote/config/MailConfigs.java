package com.example.coconote.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfigs {

	@Value("${spring.mail.host}")
	private String host;

	@Value("${spring.mail.port}")
	private int port;

	@Value("${spring.mail.username}")
	private String username;

	@Value("${spring.mail.password}")
	private String password;

	@Value("${spring.mail.properties.mail.smtp.auth}")
	private boolean auth;

	@Value("${spring.mail.properties.mail.smtp.starttls.enable}")
	private boolean starttlsEnable;

	@Value("${spring.mail.properties.mail.smtp.starttls.required}")
	private boolean starttlsRequired;

	@Value("${spring.mail.properties.mail.smtp.connectiontimeout}")
	private int connectionTimeout;

	@Value("${spring.mail.properties.mail.smtp.timeout}")
	private int timeout;

	@Value("${spring.mail.properties.mail.smtp.writetimeout}")
	private int writeTimeout;
}