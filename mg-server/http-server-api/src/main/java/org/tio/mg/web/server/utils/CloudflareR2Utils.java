package org.tio.mg.web.server.utils;

import cn.hutool.core.util.StrUtil;
import org.tio.mg.service.vo.MgConst;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;

public class CloudflareR2Utils {

    // 私有构造函数防止实例化
    private CloudflareR2Utils() {
    }

    /**
     * 构建 S3Client 实例
     */
    private static S3Client buildS3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                MgConst.CloudflareR2.R2_ACCESS_KEY,
                MgConst.CloudflareR2.R2_SECRET_KEY
        );

        return S3Client.builder()
                .endpointOverride(URI.create(MgConst.CloudflareR2.R2_ENDPOINT))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))
                .build();
    }

    /**
     * 构建 S3Presigner 实例
     */
    private static S3Presigner buildS3Presigner() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                MgConst.CloudflareR2.R2_ACCESS_KEY,
                MgConst.CloudflareR2.R2_SECRET_KEY
        );

        return S3Presigner.builder()
                .endpointOverride(URI.create(MgConst.CloudflareR2.R2_ENDPOINT))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("auto"))
                .build();
    }

    /**
     * 列出所有存储桶（Bucket）
     */
    public static List<Bucket> listBuckets() {
        try (S3Client s3Client = buildS3Client()) {
            return s3Client.listBuckets().buckets();
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to list buckets: " + e.getMessage(), e);
        }
    }

    /**
     * 列出指定 Bucket 下的所有对象
     */
    public static List<S3Object> listObjects(String bucketName) {
        try (S3Client s3Client = buildS3Client()) {
            return s3Client.listObjectsV2(b -> b.bucket(bucketName)).contents();
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to list objects in bucket " + bucketName + ": " + e.getMessage(), e);
        }
    }

    /**
     * 上传文件到 R2 存储
     */
    public static void uploadFile(String bucketName, String objectKey, InputStream inputStream, long size, String contentType) {
        try (S3Client s3Client = buildS3Client()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentLength(size)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, size));
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件类型对应的目录路径
     */
    public static String getFileFolder(String fileType) {
        if (StrUtil.isBlank(fileType)) {
            return "other/";
        }

        switch (fileType) {
            case "cover":
                return "cover/";
            case "auth":
                return "auth/";
            case "mauth":
                return "mauth/";
            case "collection":
                return "collection/";
            case "paycert":
                return "paycert/";
            case "video":
                return "video/";
            default:
                return "other/";
        }
    }

    /**
     * 生成带签名的下载链接（有效期为24小时）
     */
    public static String generatePresignedUrl(String bucketName, String objectKey) {
        GetObjectPresignRequest request = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofHours(24))
                .getObjectRequest(b -> b.bucket(bucketName).key(objectKey))
                .build();
        try (S3Presigner presigner = buildS3Presigner()) {
            return presigner.presignGetObject(request).url().toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL: " + e.getMessage(), e);
        }
    }

    /**
     * 获取公共文件访问 地址
     */
    public static String getPublicObjectUrl(String bucketName, String objectKey) {
        return String.format("%s/%s/%s", MgConst.CloudflareR2.R2_ENDPOINT, bucketName, objectKey);
    }

    /**
     * 上传文件并设置为公共读权限
     */
    public static void uploadFilePublic(String bucketName, String objectKey, InputStream inputStream, long size, String contentType) {
        if (StrUtil.isNotBlank(objectKey) && objectKey.startsWith("/")) {
            objectKey = objectKey.substring(1);
        }
        try (S3Client s3Client = buildS3Client()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentLength(size)
                    .contentType(contentType)
                    .acl(ObjectCannedACL.PUBLIC_READ) // 关键：设置为 public-read
                    .build();

            PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, size));
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to upload file to R2: " + e.getMessage(), e);
        }
    }


    /**
     * 删除指定对象
     */
    public static void deleteObject(String bucketName, String objectKey) {
        try (S3Client s3Client = buildS3Client()) {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (S3Exception e) {
            throw new RuntimeException("Failed to delete object: " + e.getMessage(), e);
        }
    }
}