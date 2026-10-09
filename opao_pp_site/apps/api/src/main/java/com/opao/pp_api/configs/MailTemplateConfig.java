package com.opao.pp_api.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
public class MailTemplateConfig {

    @Bean
    public TemplateEngine emailTemplateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(emailTemplateResolver());
        return templateEngine;
    }

    private ITemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/emails/"); // 💡 Base resource folder mapping path
        resolver.setSuffix(""); // 💡 Kept blank to allow choosing extension formats (.html, .txt) dynamically
        resolver.setTemplateMode(TemplateMode.HTML); 
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false); // Switch to true in stable staging/production settings
        return resolver;
    }
}
