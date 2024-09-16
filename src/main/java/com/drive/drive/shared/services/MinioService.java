package com.drive.drive.shared.services;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.drive.drive.modules.file.entities.FileEntity;
import com.drive.drive.modules.folder.entities.FolderEntity;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveBucketArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.http.Method;

@Service
public class MinioService {

  MinioClient minioClient;

  MinioService(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  public ObjectWriteResponse addObject(FileEntity file, FolderEntity folder, MultipartFile formFile) throws Exception {
    final String contentType = file.getFileType();

    return minioClient.putObject(
        PutObjectArgs
            .builder()
            .bucket(folder.getCode())
            .object(file.getCode())
            .stream(formFile.getInputStream(), formFile.getSize(), -1)
            .contentType(contentType).build());
  }

  public void deleteObject(FileEntity file) throws Exception {
    String bucket = file.getFolder().getCode();
    String object = file.getCode();

    minioClient.removeObject(
        RemoveObjectArgs
            .builder()
            .bucket(bucket)
            .object(object)
            .build());
  }

  public String getDownloadUrl(String bucket, String object) throws Exception {
    return minioClient.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs
            .builder()
            .method(Method.GET)
            .bucket(bucket)
            .object(object)
            .expiry(1, TimeUnit.DAYS)
            .build());
  }

  public void createBucketIfNotExists(String bucketName) throws Exception {
    boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    if (!found) {
      minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }
  }

  public void deleteBucket(String bucketName) throws Exception {
    minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(bucketName).build());
    minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
  }

  public InputStream download(String bucketName, String objectName) throws Exception {
    return minioClient.getObject(GetObjectArgs.builder()
        .bucket(bucketName)
        .object(objectName)
        .build());
  }

  public void replaceObject(FileEntity file, FolderEntity folder, MultipartFile formFile) throws Exception {
    deleteObject(file);
    addObject(file, folder, formFile);
  }
}
