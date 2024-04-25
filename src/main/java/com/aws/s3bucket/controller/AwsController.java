package com.aws.s3bucket.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aws.s3bucket.service.AwsService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("amazon")
public class AwsController {
	@Autowired
	AwsService awsService;
/*To upload the videofile to s3bucket*/
	@PostMapping("/uploadFile")
    public String uploadFile(@RequestParam(value="userId",required=true)String userId,@RequestParam(value = "file",required = true) MultipartFile file) {
        return awsService.uploadFile(file,userId);
    }
	/*To upload the imagefile to s3bucket*/	
	@PostMapping("/uploadImage")
    public String uploadImage(@RequestParam(value="userId",required=true)String userId,@RequestParam(value = "file",required = true) MultipartFile file) {
        return awsService.uploadImage(file,userId);
    }
	/*To get the status of the file in the s3bucket*/	
	@GetMapping("/fileStatus/{fileName}")
	public String checkFileStatus(@PathVariable(value="fileName",required = true)String fileName){
		  
		return awsService.lastmodified(fileName);
		  
		 }
	/*To  delete  the file in the s3bucket*/	
    @DeleteMapping("/deleteFile")
    public Object deleteFile(@RequestParam(value = "url") String fileUrl) {
        return awsService.deleteFileFromS3Bucket(fileUrl);
   
}
    /*To  download  the file and  from the s3bucket*/
    @GetMapping("downloadFile/{fileName}")
     public void downloadFile(@PathVariable(value="fileName")String fileName,HttpServletResponse response) throws IOException {
    	 //amazonClient.downloadFileFromS3Bucket(fileName,response);
    	awsService.downloadFileFromS3Bucket(fileName, response);
    }
}
