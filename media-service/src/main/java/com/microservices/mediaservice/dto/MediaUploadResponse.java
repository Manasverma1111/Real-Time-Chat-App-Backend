package com.microservices.mediaservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class MediaUploadResponse {

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