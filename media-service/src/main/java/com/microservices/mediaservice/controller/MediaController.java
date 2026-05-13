package com.microservices.mediaservice.controller;

import com.microservices.mediaservice.dto.MediaUploadResponse;
import com.microservices.mediaservice.entity.MediaFile;
import com.microservices.mediaservice.service.MediaService;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/media")
public class MediaController {

	private final MediaService mediaService;

	public MediaController(MediaService mediaService) {
		this.mediaService = mediaService;
	}

	// Upload media file
	@PostMapping("/upload")
	public MediaUploadResponse uploadFile(
			@RequestParam UUID roomId,
			@RequestParam UUID senderId,
			@RequestParam("file") MultipartFile file
	) {
		return mediaService.uploadFile(roomId, senderId, file);
	}

	// Get media files by room
	@GetMapping("/room/{roomId}")
	public List<MediaFile> getMediaByRoom(@PathVariable UUID roomId) {
		return mediaService.getMediaByRoom(roomId);
	}

	@GetMapping("/preview")
	public String generatePreviewUrl(
			@RequestParam String fileName
	) {

		return mediaService.generatePresignedUrl(
				fileName
		);
	}

	@PostMapping("/upload/profile")
	public MediaUploadResponse uploadProfileImage(
			@RequestParam UUID userId,
			@RequestParam("file") MultipartFile file
	) {
		return mediaService.uploadFile(null, userId, file);
	}

	/*
 GROUP AVATAR UPLOAD
*/
	@PostMapping("/upload/group")
	public MediaUploadResponse uploadGroupImage(
			@RequestParam UUID roomId,
			@RequestParam("file") MultipartFile file
	) {

		/*
		 senderId reused safely
		*/
		return mediaService.uploadFile(
				roomId,
				roomId,
				file
		);
	}
}