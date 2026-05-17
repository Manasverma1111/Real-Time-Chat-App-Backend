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

//	The MediaController class is a REST controller that handles HTTP requests related to media file management.
	private final MediaService mediaService;

//	The constructor of the MediaController class takes a MediaService instance
//	as a parameter and assigns it to the mediaService field.
	public MediaController(MediaService mediaService) {
		this.mediaService = mediaService;
	}

//	The uploadFile method is mapped to the POST /media/upload endpoint and is responsible for handling file uploads.
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

//	The generatePreviewUrl method is mapped to the GET /media/preview endpoint
//	and generates a pre-signed URL for previewing a media file based on the provided file name.
	@GetMapping("/preview")
	public String generatePreviewUrl(
			@RequestParam String fileName
	) {

		return mediaService.generatePresignedUrl(
				fileName
		);
	}

//	The uploadProfileImage method is mapped to the POST /media/upload/profile endpoint
//	and is responsible for handling profile image uploads for users.
	@PostMapping("/upload/profile")
	public MediaUploadResponse uploadProfileImage(
			@RequestParam UUID userId,
			@RequestParam("file") MultipartFile file
	) {
		return mediaService.uploadFile(null, userId, file);
	}

//	The uploadGroupImage method is mapped to the POST /media/upload/group endpoint
//	and is responsible for handling group image uploads for chat rooms.
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