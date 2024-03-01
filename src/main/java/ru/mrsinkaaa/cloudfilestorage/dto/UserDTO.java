package ru.mrsinkaaa.cloudfilestorage.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

    private String login;
    private String password;
}
