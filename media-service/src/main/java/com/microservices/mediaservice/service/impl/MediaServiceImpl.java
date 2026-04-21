package com.microservices.mediaservice.service.impl;

import com.microservices.mediaservice.dto.MediaUploadResponse;
import com.microservices.mediaservice.entity.MediaFile;
import com.microservices.mediaservice.repository.MediaFileRepository;
import com.microservices.mediaservice.service.MediaService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {

	private final MediaFileRepository mediaFileRepository;

	// Upload directory from config
	@Value("${file.upload-dir}")
	private String uploadDir;

	public MediaServiceImpl(MediaFileRepository mediaFileRepository) {
		this.mediaFileRepository = mediaFileRepository;
	}

	@Override
	public MediaUploadResponse uploadFile(UUID roomId, UUID senderId, MultipartFile file) {
		try {
			// Ensure upload directory exists
			Path uploadPath = Paths.get(uploadDir);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			// Generate unique file name
			String storedFileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

			// Save file to disk
			Path filePath = uploadPath.resolve(storedFileName);
			Files.copy(file.getInputStream(), filePath);

			// Prepare entity
			MediaFile mediaFile = new MediaFile();
			mediaFile.setRoomId(roomId);
			mediaFile.setSenderId(senderId);
			mediaFile.setFileName(file.getOriginalFilename());
			mediaFile.setFileType(file.getContentType());
			mediaFile.setFilePath(filePath.toString());
			mediaFile.setUploadedAt(LocalDateTime.now());

			// Persist to DB
			MediaFile saved = mediaFileRepository.save(mediaFile);

			// Return response DTO
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
			// Wrap checked exception
			throw new RuntimeException("File upload failed", e);
		}
	}

	@Override
	public List<MediaFile> getMediaByRoom(UUID roomId) {
		// Fetch media for a room (latest first)
		return mediaFileRepository.findByRoomIdOrderByUploadedAtDesc(roomId);
	}
}