package com.drive.drive.user.dto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenDto {
    private String token;

    public TokenDto() {
    }

    public TokenDto(String token) {
        this.token = token;
    }
}