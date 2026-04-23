package com.microservices.mediaservice.repository;

import com.microservices.mediaservice.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

	// Fetch media files by roomId sorted by latest first
	List<MediaFile> findByRoomIdOrderByUploadedAtDesc(UUID roomId);
}