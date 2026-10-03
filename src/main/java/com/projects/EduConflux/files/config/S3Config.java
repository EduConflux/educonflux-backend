package com.projects.EduConflux.files.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${supabase.s3.endpointUrl:}")
    private String endpointUrl;

    @Value("${supabase.s3.accessKey:}")
    private String accessKey;

    @Value("${supabase.s3.secretKey:}")
    private String secretKey;

    @Value("${supabase.s3.region:ap-south-1}")
    private String region;

    @Bean
    public S3Client s3Client() {
        String effectiveEndpoint = (endpointUrl != null && !endpointUrl.trim().isEmpty())
                ? endpointUrl.trim()
                : "https://glikkukbfkflvdlptebb.storage.supabase.co/storage/v1/s3";
        String effectiveAccessKey = (accessKey != null && !accessKey.trim().isEmpty()) ? accessKey.trim() : "placeholder";
        String effectiveSecretKey = (secretKey != null && !secretKey.trim().isEmpty()) ? secretKey.trim() : "placeholder";

        AwsBasicCredentials credentials = AwsBasicCredentials.create(effectiveAccessKey, effectiveSecretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(URI.create(effectiveEndpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }
}
