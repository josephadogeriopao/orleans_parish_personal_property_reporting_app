package com.opao.pp_api.services;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine emailTemplateEngine;

    @Value("${application.frontend-url}")
    private String frontendUrl;

    @Value("${application.email.from-address}")
    private String fromAddress;

    @Value("${application.email.from-name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender, TemplateEngine emailTemplateEngine) {
        this.mailSender = mailSender;
        this.emailTemplateEngine = emailTemplateEngine;
    }

    public void sendVerificationEmail(String toAddress, String recipientName, String verificationCode) {
        log.info("Compiling and dispatching outbound verification email block out to: {}", toAddress);
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // Passing true configures multipart formatting (HTML payload + plain-text fallback structural maps)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            // 💡 Next.js URL compilation replaces legacy System.getProperty site variables completely
            String activationUrl = frontendUrl + "/auth/verify?code=" + verificationCode;

            // 1. Bind variables securely into Thymeleaf Context scope tracking metrics
            Context context = new Context();
            context.setVariable("name", recipientName);
            context.setVariable("activationUrl", activationUrl);
            context.setVariable("expirationDuration", "1 hour");

            // 2. Compile presentation targets straight from file templates paths
            String processedSubject = emailTemplateEngine.process("register/subject.txt", context);
            String processedHtml = emailTemplateEngine.process("register/html.html", context);
            String processedText = emailTemplateEngine.process("register/text.txt", context);

            // 3. Assemble structural network envelope attributes
            helper.setFrom(fromAddress, fromName);
            helper.setTo(toAddress);
            helper.setSubject(processedSubject.trim());
            helper.setText(processedText, processedHtml); // Enforces dual text/html alternative delivery layers

            // 4. Ship payload out across network pipelines
            mailSender.send(message);
            log.info("Email notification block compiled and successfully passed down to SMTP transport gateway.");
            
        } catch (Exception e) {
            log.error("Critical failure during mail generation / template mapping thread pipeline execution context", e);
            throw new RuntimeException("Outbound communications processing exception occurred", e);
        }
    }
}
