package com.opao.pp_api;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;
// 💡 Import the clashing auto-configuration explicitly
import de.codecentric.boot.admin.server.config.AdminServerNotifierAutoConfiguration; 

@SpringBootApplication(
    scanBasePackages = { "com.opao.pp_api" },
    // 💡 FIX: Forcefully block the admin server mail notifier configuration on boot permanently
    exclude = { AdminServerNotifierAutoConfiguration.class } 
)
@RestController 
public class PpApiApplication {

    public static void main(String[] args) {
        // 💡 Load local .env properties into system memory cleanly for Turborepo and AWS
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./apps/api") // Maps to Turborepo Local dev executions
                    .ignoreIfMissing()      // 💡 CRUCIAL FOR AWS: Silently ignores the file in cloud deployments
                    .load();
            
            dotenv.entries().forEach(entry -> 
                System.setProperty(entry.getKey(), entry.getValue())
            );
        } catch (Exception e) {
            // Fallback for root execution directory architectures
            try {
                Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
                dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
            } catch (Exception ignored) {}
        }

        SpringApplication.run(PpApiApplication.class, args);
    }
}
