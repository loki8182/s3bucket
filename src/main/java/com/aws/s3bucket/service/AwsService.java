package com.aws.s3bucket.service;

import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;

//import io.grpc.internal.IoUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;

import java.util.List;

@Service
public class AwsService {
	private static final Logger LOG = LogManager.getLogger(AwsService.class);
	private AmazonS3 s3client;

	@Value("${aws.access.key.id}")
	private String accessKey;

	@Value("${aws.secret.access.key}")
	private String secretKey;

	@Value("${aws.s3.region}")
	private String region;
	@Value("${aws.s3.endpointUrl}")
	private String endpointUrl;
	@Value("${aws.s3.bucket.name}")
	private String bucketName;

	@PostConstruct
	private void initializeAmazon() {
		AWSCredentials credentials = new BasicAWSCredentials(this.accessKey, this.secretKey);
		this.s3client = new AmazonS3Client(credentials);
	}

	public String uploadFile(MultipartFile multipartFile, String userId) {
		String fileName = userId + ".mp4";
		LOG.info("the file to be uploaded" + fileName);
		String fileUrl = "";
		try {
			// converting multipartfile to normalFile
			File file = convertMultiPartToFile(multipartFile);
			// fileName = generateFileName(multipartFile);
			
			fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
			uploadFileTos3bucket(fileName, file);
			file.delete();
		} catch (Exception e) {
			LOG.error(e.getMessage() + e.getCause());
		}

		return "sucesss";
	}

	public String uploadImage(MultipartFile multipartFile, String userId) {
		String fileName = userId + ".png";
		LOG.info("the file to be uploaded" + fileName);
		String fileUrl = "";
		try {
			File file = convertMultiPartToFile(multipartFile);
			fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
			uploadFileTos3bucket(fileName, file);
			file.delete();
		} catch (Exception e) {
			LOG.error(e.getMessage() + e.getCause());
		}

		return fileUrl;
	}
	public String uploadGlbFile(MultipartFile multipartFile, String userId) {
		String fileName = userId + ".glb";
		LOG.info("the file to be uploaded" + fileName);
		String fileUrl = "";
		try {
			File file = convertMultiPartToFile(multipartFile);
			fileUrl = endpointUrl + "/" + bucketName + "/" + fileName;
			uploadFileTos3bucket(fileName, file);
			file.delete();
		} catch (Exception e) {
			LOG.error(e.getMessage() + e.getCause());
		}
		return fileUrl;
	}
	//the method check is to convert the multipartFile to File which needs to upload 
	//because we while receive the file as the mulltipartfile from the api
	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}
	//the method is to upload the file to s3bucket
	private void uploadFileTos3bucket(String fileName, File file) {
		s3client.putObject(
				new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
	}
	//the method is to delete the file from the s3bucket
	public Object deleteFileFromS3Bucket(String fileName) {
		s3client.deleteObject(new DeleteObjectRequest(bucketName, fileName));

		return "Successfully deleted";
	}
	//the method is to downlaod the video and image file and stream it on the browser
	public void downloadFileFromS3Bucket(String fileName, HttpServletResponse response) throws IOException {

		byte[] content;
		S3Object object = s3client.getObject(new GetObjectRequest(bucketName, fileName));
		S3ObjectInputStream objectContent = object.getObjectContent();

		content = IOUtils.toByteArray(objectContent);
		List<byte[]> byteArrayList = new ArrayList<>();

		byteArrayList.add(content);

		if (fileName.contains("mp4")) {
			response.setContentType("video/mp4");
		} else {
			response.setContentType("image/png");
		}
		ServletOutputStream out = response.getOutputStream();
		InputStream fin = new ByteArrayInputStream(byteArrayList.get(0));
		byte[] buf = new byte[4096];
		int read;
		while ((read = fin.read(buf)) != -1) {
			out.write(buf, 0, read);
		}

		fin.close();
		out.flush();
		out.close();

	}
//the method check the lastmodified time of each file in the bucket
	public String lastmodified(String fileName) {
		// S3Object object = s3client.getObject(new GetObjectRequest(bucketName,
		// fileName));
		S3Object fullObject = s3client.getObject(new GetObjectRequest(bucketName, fileName));
		ObjectMetadata objectMetadata = fullObject.getObjectMetadata();
		String lastModified = objectMetadata.getLastModified().toString();

		System.out.println(lastModified);
		return lastModified;
	}

}
