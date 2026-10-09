package com.opao.pp_api.features.user.mapper;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.opao.pp_api.features.user.dto.request.UserUpdateRequest;
import com.opao.pp_api.features.user.model.User;
import com.opao.pp_api.features.user.model.UserEntity;
import com.opao.pp_api.features.user_role.model.UserRole;
import com.opao.pp_api.features.user_role.model.UserRoleEntity;
import com.opao.pp_api.features.user_status.model.UserStatus;
import com.opao.pp_api.features.user_status.model.UserStatusEntity;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface UserMapper {

    @Mapping(source = "userId", target = "id")
    @Mapping(source = "emailAddress", target = "email")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "userRoleId.userRoleId", target = "userRoleId")
    @Mapping(source = "userStatus.userStatusId", target = "userStatusId")
    @Mapping(source = "lastLoginTime", target = "lastLoginTime")
    @Mapping(source = "failedLogins", target = "failedLogins")
    User toDomain(UserEntity entity);

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "email", target = "emailAddress")
    @Mapping(source = "password", target = "password")
    @Mapping(source = "userRoleId", target = "userRoleId")
    @Mapping(source = "userStatusId", target = "userStatus")
    @Mapping(target = "formCollection", ignore = true)
    @Mapping(source = "lastLoginTime", target = "lastLoginTime")
    @Mapping(source = "failedLogins", target = "failedLogins")
    UserEntity toEntity(User domain);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(source = "email", target = "emailAddress")
    @Mapping(source = "password     ", target = "password")
    @Mapping(source = "userRoleId", target = "userRoleId")
    @Mapping(source = "userStatusId", target = "userStatus")
    @Mapping(target = "formCollection", ignore = true)
    void updateEntityFromDomain(User domain, @MappingTarget UserEntity existingEntity);

    /**
     * 🚀 FIXED: Explicitly defined mappings from the record fields.
     * This bypasses any automatic property-naming discovery flaws with Java records.
     */
    @Mapping(source = "username", target = "username")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "phoneNumber", target = "phoneNumber")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    //@Mapping(target = "isActive", ignore = true)
    @Mapping(target = "userRoleId", ignore = true)
    @Mapping(target = "userStatusId", ignore = true)
    void updateDomainFromRequest(UserUpdateRequest request, @MappingTarget User domain);


    // --- Entity/Domain Sub-object Mappers ---

    default UserRole mapToDomainRole(Integer roleId) {
        if (roleId == null) return null;
        UserRole role = new UserRole();
        role.setId(roleId); 
        return role;
    }

    default UserStatus mapToDomainStatus(Integer statusId) {
        if (statusId == null) return null;
        UserStatus status = new UserStatus();
        status.setId(statusId);
        return status;
    }

    default UserRoleEntity mapToEntityRole(UserRole domain) {
        if (domain == null || domain.getId() == null) return null;
        UserRoleEntity entity = new UserRoleEntity();
        entity.setUserRoleId(domain.getId());
        return entity;
    }

    default UserStatusEntity mapToEntityStatus(UserStatus domain) {
        if (domain == null || domain.getId() == null) return null;
        UserStatusEntity entity = new UserStatusEntity();
        entity.setUserStatusId(domain.getId());
        return entity;
    }

    default boolean mapIsActive(UserEntity entity) {
        if (entity == null || entity.getUserStatus() == null) return false;
        return "Enabled".equalsIgnoreCase(entity.getUserStatus().getName());
    }
}
