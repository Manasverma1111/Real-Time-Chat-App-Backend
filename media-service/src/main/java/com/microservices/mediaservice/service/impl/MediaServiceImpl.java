package com.microservices.mediaservice.service.impl;

import com.microservices.mediaservice.dto.MediaUploadResponse;
import com.microservices.mediaservice.entity.MediaFile;
import com.microservices.mediaservice.repository.MediaFileRepository;
import com.microservices.mediaservice.service.MediaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {

	private final MediaFileRepository mediaFileRepository;
	private final S3Client s3Client;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	@Value("${aws.region}")
	private String region;

	public MediaServiceImpl(
			MediaFileRepository mediaFileRepository,
			S3Client s3Client
	) {
		this.mediaFileRepository = mediaFileRepository;
		this.s3Client = s3Client;
	}

	@Override
	public MediaUploadResponse uploadFile(
			UUID roomId,
			UUID senderId,
			MultipartFile file
	) {

		try {
			// Generate unique filename
			String storedFileName =
					UUID.randomUUID() + "_" + file.getOriginalFilename();

			// Upload file to AWS S3
			PutObjectRequest putObjectRequest =
					PutObjectRequest.builder()
							.bucket(bucketName)
							.key(storedFileName)
							.contentType(file.getContentType())
							.build();

			s3Client.putObject(
					putObjectRequest,
					RequestBody.fromBytes(file.getBytes())
			);

			// Generate public S3 URL
			String fileUrl =
					"https://" + bucketName +
							".s3." + region +
							".amazonaws.com/" +
							storedFileName;

			// Save metadata to DB
			MediaFile mediaFile = new MediaFile();
			mediaFile.setRoomId(roomId);
			mediaFile.setSenderId(senderId);
			mediaFile.setFileName(file.getOriginalFilename());
			mediaFile.setFileType(file.getContentType());
			mediaFile.setFilePath(fileUrl);
			mediaFile.setUploadedAt(LocalDateTime.now());

			MediaFile saved =
					mediaFileRepository.save(mediaFile);

			// Return response without breaking frontend
			return new MediaUploadResponse(
					saved.getId(),
					saved.getRoomId(),
					saved.getSenderId(),
					saved.getFileName(),
					saved.getFileType(),
					saved.getFilePath(),
					saved.getUploadedAt()
			);

		} catch (IOException e) {
			throw new RuntimeException(
					"File upload to AWS S3 failed",
					e
			);
		}
	}

	@Override
	public List<MediaFile> getMediaByRoom(UUID roomId) {
		return mediaFileRepository
				.findByRoomIdOrderByUploadedAtDesc(roomId);
	}
}



//package com.microservices.mediaservice.service.impl;
//
//import com.microservices.mediaservice.dto.MediaUploadResponse;
//import com.microservices.mediaservice.entity.MediaFile;
//import com.microservices.mediaservice.repository.MediaFileRepository;
//import com.microservices.mediaservice.service.MediaService;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//public class MediaServiceImpl implements MediaService {
//
//	private final MediaFileRepository mediaFileRepository;
//
//	// Upload directory from config
//	@Value("${file.upload-dir}")
//	private String uploadDir;
//
//	public MediaServiceImpl(MediaFileRepository mediaFileRepository) {
//		this.mediaFileRepository = mediaFileRepository;
//	}
//
//	@Override
//	public MediaUploadResponse uploadFile(UUID roomId, UUID senderId, MultipartFile file) {
//		try {
//			// Ensure upload directory exists
//			Path uploadPath = Paths.get(uploadDir);
//			if (!Files.exists(uploadPath)) {
//				Files.createDirectories(uploadPath);
//			}
//
//			// Generate unique file name
//			String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//
//			// Save file to disk
//			Path filePath = uploadPath.resolve(storedFileName);
//			Files.copy(file.getInputStream(), filePath);
//
//			// Prepare entity
//			MediaFile mediaFile = new MediaFile();
//			mediaFile.setRoomId(roomId);
//			mediaFile.setSenderId(senderId);
//			mediaFile.setFileName(file.getOriginalFilename());
//			mediaFile.setFileType(file.getContentType());
//			mediaFile.setFilePath(filePath.toString());
//			mediaFile.setUploadedAt(LocalDateTime.now());
//
//			// Persist to DB
//			MediaFile saved = mediaFileRepository.save(mediaFile);
//
//			// Return response DTO
//			return new MediaUploadResponse(
//					saved.getId(),
//					saved.getRoomId(),
//					saved.getSenderId(),
//					saved.getFileName(),
//					saved.getFileType(),
//					saved.getFilePath(),
//					saved.getUploadedAt()
//			);
//
//		} catch (IOException e) {
//			// Wrap checked exception
//			throw new RuntimeException("File upload failed", e);
//		}
//	}
//
//	@Override
//	public List<MediaFile> getMediaByRoom(UUID roomId) {
//		// Fetch media for a room (latest first)
//		return mediaFileRepository.findByRoomIdOrderByUploadedAtDesc(roomId);
//	}
//}