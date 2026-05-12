package com.microservices.mediaservice.service;

import com.microservices.mediaservice.dto.MediaUploadResponse;
import com.microservices.mediaservice.entity.MediaFile;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MediaService {

	// Upload file and return metadata response
	MediaUploadResponse uploadFile(
			UUID roomId,
			UUID senderId,
			MultipartFile file
	);

	List<MediaFile> getMediaByRoom(UUID roomId);

	String generatePresignedUrl(String fileName);
}