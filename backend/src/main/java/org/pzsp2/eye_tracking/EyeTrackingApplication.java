package org.pzsp2.eye_tracking;

import org.pzsp2.eye_tracking.auth.jwt.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class EyeTrackingApplication {

	public static void main(String[] args) {
		SpringApplication.run(EyeTrackingApplication.class, args);
	}

}
