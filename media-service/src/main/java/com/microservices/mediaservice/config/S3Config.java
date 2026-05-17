package com.microservices.mediaservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import software.amazon.awssdk.regions.Region;

import software.amazon.awssdk.services.s3.S3Client;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

//    The S3Config class is a Spring configuration class that sets up the necessary beans
//    for interacting with Amazon S3.
    @Value("${aws.access-key}")
    private String accessKey;

    @Value("${aws.secret-key}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

//    The awsCredentials() method creates an AwsBasicCredentials bean using the access key and secret key
    @Bean
    public AwsBasicCredentials awsCredentials() {

        return AwsBasicCredentials.create(
                accessKey,
                secretKey
        );
    }

//    The s3Client() method creates an S3Client bean configured with the specified region and credentials provider
    @Bean
    public S3Client s3Client(
            AwsBasicCredentials credentials
    ) {

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials)
                )
                .build();
    }

//    The s3Presigner() method creates an S3Presigner bean,
//    which is used for generating pre-signed URLs for S3 operations,
//    configured with the same region and credentials provider as the S3Client.
    @Bean
    public S3Presigner s3Presigner(
            AwsBasicCredentials credentials
    ) {

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(credentials)
                )
                .build();
    }
}