package com.microservices.mediaservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "media_files")
public class MediaFile {

	@Id
	@GeneratedValue
	private UUID id;

	// Chat room reference
	private UUID roomId;

	// Uploader user ID
	private UUID senderId;

	// Original file name
	private String fileName;

	// MIME type (image/png, application/pdf, etc.)
	private String fileType;

	/*
	 * IMPORTANT:
	 * Previously this stored local path:
	 * uploads/file.jpg
	 *
	 * Now it stores AWS S3 URL:
	 * https://bucket-name.s3.region.amazonaws.com/file.jpg
	 */
	private String filePath;

	// Upload timestamp
	private LocalDateTime uploadedAt;
}
