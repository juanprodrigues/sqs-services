package com.ecommerce.factura.service;

import java.io.File;

import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
public class S3StorageService {

    private final S3Client s3Client;

    private final String bucket =
            "facturas-bucket";

    public S3StorageService(
            S3Client s3Client) {

        this.s3Client = s3Client;
    }

    public String subirFactura(
            File pdf) {

        String key =
                pdf.getName();

        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build(),
                RequestBody.fromFile(
                        pdf));

        return key;
    }
}