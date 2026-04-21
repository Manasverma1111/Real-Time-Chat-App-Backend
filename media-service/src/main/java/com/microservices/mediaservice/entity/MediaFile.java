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

	// Primary key (UUID for uniqueness)
	@Id
	@GeneratedValue
	private UUID id;

	// Chat room reference
	private UUID roomId;

	// Uploader user ID
	private UUID senderId;

	// Original file name
	private String fileName;

	// MIME type (e.g., image/png)
	private String fileType;

	// Storage location/path
	private String filePath;

	// Upload timestamp
	private LocalDateTime uploadedAt;
}