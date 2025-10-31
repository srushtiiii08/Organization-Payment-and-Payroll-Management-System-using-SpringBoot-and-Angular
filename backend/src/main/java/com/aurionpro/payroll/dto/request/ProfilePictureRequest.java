package com.aurionpro.payroll.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ProfilePictureRequest {
    
    @NotBlank(message = "Profile picture URL is required")
    private String profilePictureUrl;

    public ProfilePictureRequest() {}

    public ProfilePictureRequest(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}