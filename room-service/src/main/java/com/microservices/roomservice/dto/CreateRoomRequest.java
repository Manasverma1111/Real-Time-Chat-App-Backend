package com.microservices.roomservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateRoomRequest {

	@NotBlank
	private String name;

	/*
     GROUP / DM
    */
	@NotBlank
	private String type;

	/*
     PUBLIC / PRIVATE

     Optional to preserve backward compatibility
    */
	private String visibility;

	/*
     Optional group description
    */
	private String description;

	private List<UUID> memberIds;
}