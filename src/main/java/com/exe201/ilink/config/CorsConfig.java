package com.exe201.ilink.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
@EnableWebMvc
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOrigins(
                        "http://localhost:3000",
            "http://souvi.s3-website-ap-northeast-1.amazonaws.com/"
            )
                .allowedHeaders("*")
                .allowedHeaders("*")
                .allowedMethods("*");
    }
}
