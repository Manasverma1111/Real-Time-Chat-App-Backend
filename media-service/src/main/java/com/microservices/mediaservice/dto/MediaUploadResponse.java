package com.connecthub.media.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class MediaUploadResponse {
	private UUID id;
	private UUID roomId;
	private UUID senderId;
	private String fileName;
	private String fileType;
	private String filePath;
	private LocalDateTime uploadedAt;
}