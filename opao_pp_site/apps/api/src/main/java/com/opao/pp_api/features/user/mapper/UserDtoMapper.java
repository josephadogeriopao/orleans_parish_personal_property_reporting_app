package com.opao.pp_api.features.user.mapper;

import org.springframework.stereotype.Component;
import com.opao.pp_api.features.user_status.constants.UserStatuses;
import com.opao.pp_api.features.user.dto.request.UserCreateRequest;
import com.opao.pp_api.features.user.dto.request.UserUpdateRequest;
import com.opao.pp_api.features.user_role.constants.UserRoles;
import com.opao.pp_api.features.user.dto.response.UserResponse;
import com.opao.pp_api.features.user.model.User;

@Component
public class UserDtoMapper {

    /**
     * Maps an incoming registration payload to a clean User business Domain Model.
     */
    public User toDomain(UserCreateRequest request) {
        if (request == null) return null;
        
        return User.builder()
                .username(request.getUsername())
                .fullName(request.getFullName())
                .email(request.getEmail()) 
                .phoneNumber(request.getPhoneNumber())
                .password(request.getClearTextPassword()) 
                .isActive(true) 
                .build();
    }

    /**
     * 💡 FIX: Accepts the path variable ID parameter explicitly to match the project's @PathVariable standard.
     */
    public User toDomain(Integer id, UserUpdateRequest request) {
        if (request == null) return null;
        
        return User.builder()
                .id(id) // ➔ Bound from URL Path variable
                .username(request.getUsername())
                .fullName(request.getFullName())                
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(request.getClearTextPassword())
                .userRoleId(request.getUserRoleId() != null ? UserRoles.fromId(request.getUserRoleId().intValue()) : null)
                .userStatusId(request.getUserStatusId() != null ? UserStatuses.fromId(request.getUserStatusId().intValue()) : null)     
                .build();
    }

    /**
     * Translates a populated internal business User Domain Model instance 
     * back out to a unified, client-safe UserResponse Record.
     * 
     * Handles both Post-Creation and Post-Update flows seamlessly.
     */
    public UserResponse toResponse(User user) {
        if (user == null) return null;

        // 💡 Gracefully extract identifiers and names from nested complex relational entities if present
        Integer roleId = (user.getUserRoleId() != null) ? user.getUserRoleId().getId() : null;
        String roleName = (user.getUserRoleId() != null) ? user.getUserRoleId().getName() : null;
        
        Integer statusId = (user.getUserStatusId() != null) ? user.getUserStatusId().getId() : null;
        String statusName = (user.getUserStatusId() != null) ? user.getUserStatusId().getName() : null;

        // 💡 Maps all 10 constructor arguments accurately to match your record definition
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.isActive(), // Evaluates cleanly to a Boolean wrapper object
            roleId,
            roleName,
            statusId,
            statusName
        );    

    }
}
