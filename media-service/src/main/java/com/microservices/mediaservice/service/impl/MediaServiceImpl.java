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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {

//	The MediaServiceImpl class is a service implementation that provides methods for handling media file
//	uploads, retrieval, and generating pre-signed URLs for media files stored in AWS S3.
//	It interacts with the MediaFileRepository for database operations and uses the S3Client and S3Presigner for AWS S3 interactions.
	private final MediaFileRepository mediaFileRepository;
	private final S3Client s3Client;

	private final S3Presigner s3Presigner;

	@Value("${aws.s3.bucket-name}")
	private String bucketName;

	@Value("${aws.s3.base-url}")
	private String baseUrl;

//	The constructor of the MediaServiceImpl class takes instances of MediaFileRepository, S3Client,
//	and S3Presigner as parameters and assigns them to the corresponding fields for use in the service methods.
	public MediaServiceImpl(
			MediaFileRepository mediaFileRepository,
			S3Client s3Client,
			S3Presigner s3Presigner
	) {
		this.mediaFileRepository = mediaFileRepository;
		this.s3Client = s3Client;
		this.s3Presigner = s3Presigner;
	}

//	The uploadFile method handles the process of uploading a media file to AWS S3,
//	saving its metadata to the database,
	@Override
	public MediaUploadResponse uploadFile(
			UUID roomId,
			UUID senderId,
			MultipartFile file
	) {

		try {

			// Validate file
			if (file == null || file.isEmpty()) {
				throw new RuntimeException("File cannot be empty");
			}

			// Generate unique filename
			String storedFileName =
					UUID.randomUUID() + "_" + file.getOriginalFilename();

			// Upload to AWS S3
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

			// Generate S3 file URL
			String fileUrl =
					baseUrl + "/" + storedFileName;

			// Save metadata
			MediaFile mediaFile = new MediaFile();

			mediaFile.setRoomId(roomId);
			mediaFile.setSenderId(senderId);

			mediaFile.setFileName(file.getOriginalFilename());
			mediaFile.setFileType(file.getContentType());

			mediaFile.setFilePath(fileUrl);

			mediaFile.setUploadedAt(LocalDateTime.now());

			MediaFile savedMedia =
					mediaFileRepository.save(mediaFile);

			// Preserve existing frontend response structure
			return new MediaUploadResponse(
					savedMedia.getId(),
					savedMedia.getRoomId(),
					savedMedia.getSenderId(),
					savedMedia.getFileName(),
					savedMedia.getFileType(),
					savedMedia.getFilePath(),
					savedMedia.getUploadedAt()
			);

		} catch (IOException e) {

			throw new RuntimeException(
					"Failed to upload file to AWS S3",
					e
			);
		}
	}

//	The getMediaByRoom method retrieves a list of media files associated with a specific chat room,
//	sorted by the upload timestamp in descending order.
	@Override
	public List<MediaFile> getMediaByRoom(UUID roomId) {

		return mediaFileRepository
				.findByRoomIdOrderByUploadedAtDesc(roomId);
	}

//	The generatePresignedUrl method generates a pre-signed URL for a media file stored in AWS S3
//	based on the provided file name.
	@Override
	public String generatePresignedUrl(String fileName) {

//		The GetObjectRequest is built using the bucket name and the file name to specify which object in S3 we want to access.
		GetObjectRequest getObjectRequest =
				GetObjectRequest.builder()
						.bucket(bucketName)
						.key(fileName)
						.build();

//		The GetObjectPresignRequest is built using the GetObjectRequest and a specified signature duration of 30 minutes.
		GetObjectPresignRequest presignRequest =
				GetObjectPresignRequest.builder()
						.signatureDuration(Duration.ofMinutes(30))
						.getObjectRequest(getObjectRequest)
						.build();

//		The S3Presigner is used to generate a PresignedGetObjectRequest based on the GetObjectPresignRequest,
		PresignedGetObjectRequest presignedRequest =
				s3Presigner.presignGetObject(
						presignRequest
				);

		return presignedRequest.url().toString();
	}
}