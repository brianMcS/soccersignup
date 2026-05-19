package com.soccersignup.backend.dto;

import com.soccersignup.backend.model.PlayerRole;

import java.util.Set;

public record UpdateRolesRequest(Set<PlayerRole> roles) {
}
