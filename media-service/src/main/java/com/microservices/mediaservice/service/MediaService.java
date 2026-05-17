package com.microservices.mediaservice.service;

import com.microservices.mediaservice.dto.MediaUploadResponse;
import com.microservices.mediaservice.entity.MediaFile;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface MediaService {

// Upload file and return metadata response
//	The uploadFile method is responsible for handling the process of uploading a media file,
	MediaUploadResponse uploadFile(
			UUID roomId,
			UUID senderId,
			MultipartFile file
	);

//	The getMediaByRoom method retrieves a list of media files associated with a specific room ID.
	List<MediaFile> getMediaByRoom(UUID roomId);

//	The generatePresignedUrl method generates a pre-signed URL for previewing a media file based on the provided file name.
	String generatePresignedUrl(String fileName);
}