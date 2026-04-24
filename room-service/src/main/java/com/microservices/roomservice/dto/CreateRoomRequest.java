package com.microservices.roomservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreateRoomRequest {

	@NotBlank
	private String name;

	@NotBlank
	private String type; // GROUP / DM

	private List<UUID> memberIds;
}