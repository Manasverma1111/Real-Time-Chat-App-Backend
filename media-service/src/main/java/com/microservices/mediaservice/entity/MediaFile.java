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


//package com.microservices.mediaservice.entity;
//
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import lombok.Data;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//@Data
//@Entity
//@Table(name = "media_files")
//public class MediaFile {
//
//	// Primary key (UUID for uniqueness)
//	@Id
//	@GeneratedValue
//	private UUID id;
//
//	// Chat room reference
//	private UUID roomId;
//
//	public UUID getId() {
//		return id;
//	}
//
//	public void setId(UUID id) {
//		this.id = id;
//	}
//
//	public UUID getRoomId() {
//		return roomId;
//	}
//
//	public void setRoomId(UUID roomId) {
//		this.roomId = roomId;
//	}
//
//	public UUID getSenderId() {
//		return senderId;
//	}
//
//	public void setSenderId(UUID senderId) {
//		this.senderId = senderId;
//	}
//
//	public String getFileName() {
//		return fileName;
//	}
//
//	public void setFileName(String fileName) {
//		this.fileName = fileName;
//	}
//
//	public String getFileType() {
//		return fileType;
//	}
//
//	public void setFileType(String fileType) {
//		this.fileType = fileType;
//	}
//
//	public String getFilePath() {
//		return filePath;
//	}
//
//	public void setFilePath(String filePath) {
//		this.filePath = filePath;
//	}
//
//	public LocalDateTime getUploadedAt() {
//		return uploadedAt;
//	}
//
//	public void setUploadedAt(LocalDateTime uploadedAt) {
//		this.uploadedAt = uploadedAt;
//	}
//
//	// Uploader user ID
//	private UUID senderId;
//
//	// Original file name
//	private String fileName;
//
//	// MIME type (e.g., image/png)
//	private String fileType;
//
//	// Storage location/path
//	private String filePath;
//
//	// Upload timestamp
//	private LocalDateTime uploadedAt;
//}