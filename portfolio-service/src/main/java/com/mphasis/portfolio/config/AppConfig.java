package com.mphasis.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
	
	// This creates a RestTemplate bean that our controller can use to make HTTP calls
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
