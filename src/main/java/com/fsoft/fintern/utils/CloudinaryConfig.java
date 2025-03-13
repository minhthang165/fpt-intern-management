package com.fsoft.fintern.utils;

import com.cloudinary.Cloudinary;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.internal.Logger;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {
    @Value("${CLOUDINARY_NAME}")
    private String cloudName;
    @Value("${CLOUDINARY_API_KEY}")
    private String apiKey;
    @Value("${CLOUDINARY_API_SECRET}")
    private String apiSecret;
    @Bean
    public Cloudinary getCloudinaryConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new Cloudinary(config);
    }
}