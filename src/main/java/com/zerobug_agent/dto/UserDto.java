package com.zerobug_agent.dto;

import com.zerobug_agent.entity.User;
import com.zerobug_agent.entity.UserRole;
public class UserDto {
    private Long id;
    private String email;
    private String fullName;
    private UserRole role;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .build();
    }

    public UserDto() {
    }

    public UserDto(Long id, String email, String fullName, UserRole role) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public static UserDtoBuilder builder() {
        return new UserDtoBuilder();
    }

    public static class UserDtoBuilder {
        private Long id;
        private String email;
        private String fullName;
        private UserRole role;

        public UserDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserDtoBuilder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public UserDtoBuilder role(UserRole role) {
            this.role = role;
            return this;
        }

        public UserDto build() {
            return new UserDto(id, email, fullName, role);
        }
    }
}
