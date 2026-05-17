package com.microservices.mediaservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
//@AllArgsConstructor
public class MediaUploadResponse {

//	The MediaUploadResponse class is a Data Transfer Object (DTO)
//	that represents the response returned after a media file is uploaded.
	public MediaUploadResponse(UUID id, UUID roomId, UUID senderId, String fileName, String fileType, String filePath, LocalDateTime uploadedAt) {
		this.id = id;
		this.roomId = roomId;
		this.senderId = senderId;
		this.fileName = fileName;
		this.fileType = fileType;
		this.filePath = filePath;
		this.uploadedAt = uploadedAt;
	}

//	The class contains fields for the media file ID, associated room ID, uploader user ID,
//	original file name, MIME type, file storage path, and upload timestamp.
	// Media file ID
	private UUID id;

	// Associated room ID
	private UUID roomId;

	// Uploader user ID
	private UUID senderId;

	// Original file name
	private String fileName;

	// MIME type
	private String fileType;

	// File storage path
	private String filePath;

	// Upload timestamp
	private LocalDateTime uploadedAt;
}