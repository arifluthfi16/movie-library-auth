package com.authservice.dto;

import com.authservice.api.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponseDTO {
    private long id;
    private String username;
    private String role;
    private String country;

    public static UserResponseDTO fromUserEntity (User user) {
        UserResponseDTO responseDTO = new UserResponseDTO();
        responseDTO.setRole(user.getRole());
        responseDTO.setId(user.getId());
        responseDTO.setUsername(user.getUsername());
        responseDTO.setCountry(user.getCountry());
        return responseDTO;
    }
}
