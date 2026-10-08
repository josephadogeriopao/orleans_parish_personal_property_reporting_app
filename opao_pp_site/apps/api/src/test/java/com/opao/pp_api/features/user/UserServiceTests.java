package com.opao.pp_api.features.user;

import com.opao.pp_api.features.user.mapper.UserMapper;
import com.opao.pp_api.features.user.model.User;
import com.opao.pp_api.features.user.model.UserEntity;
import com.opao.pp_api.features.user_role.constants.UserRoles;
import com.opao.pp_api.features.user_status.constants.UserStatuses;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTests {
    @Mock private UserRepository repository;
    @Mock private UserMapper mapper;
    @InjectMocks private UserService service;

    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPaths {
        @Test
        @DisplayName("Creates a user with default role and status")
        void create_AppliesDefaults() {
            User domain = User.builder().username("jdoe").fullName("Jane Doe").build();
            UserEntity entity = new UserEntity();
            when(repository.findByUsername("jdoe")).thenReturn(Optional.empty());
            when(mapper.toEntity(domain)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(domain);

            assertSame(domain, service.create(domain));
            assertEquals(UserRoles.TAX_PREPARER, domain.getUserRoleId());
            assertEquals(UserStatuses.ENABLED, domain.getUserStatusId());
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("Maps a user lookup by username")
        void getUserByUsername_Success() {
            UserEntity entity = new UserEntity();
            User domain = User.builder().id(8).username("jdoe").build();
            when(repository.findByUsername("jdoe")).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            assertEquals(domain, service.getUserByUsername("jdoe").orElseThrow());
        }

        @Test
        @DisplayName("Updates an existing user profile")
        void updateUser_Success() {
            UserEntity existing = new UserEntity();
            User input = User.builder().fullName("Updated Name").build();
            User updated = User.builder().id(8).fullName("Updated Name").build();
            when(repository.findById(8)).thenReturn(Optional.of(existing));
            when(repository.save(existing)).thenReturn(existing);
            when(mapper.toDomain(existing)).thenReturn(updated);

            assertSame(updated, service.updateUser(8, input).orElseThrow());
            verify(mapper).updateEntityFromDomain(input, existing);
        }
    }

    @Nested
    @DisplayName("Exception Case Scenarios")
    class ExceptionCases {
        @Test
        @DisplayName("Rejects creation when the username is already registered")
        void create_DuplicateUsername() {
            User domain = User.builder().username("taken").build();
            when(repository.findByUsername("taken")).thenReturn(Optional.of(new UserEntity()));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> service.create(domain));

            assertEquals("Username is already taken", exception.getMessage());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Propagates persistence errors during user creation")
        void create_RepositoryFailure() {
            User domain = User.builder().username("new-user").build();
            UserEntity entity = new UserEntity();
            IllegalStateException failure = new IllegalStateException("database unavailable");
            when(repository.findByUsername("new-user")).thenReturn(Optional.empty());
            when(mapper.toEntity(domain)).thenReturn(entity);
            when(repository.save(entity)).thenThrow(failure);

            assertSame(failure, assertThrows(IllegalStateException.class, () -> service.create(domain)));
            verify(mapper, never()).toDomain(any());
        }
    }

    @Nested
    @DisplayName("Edge Case Scenarios")
    class EdgeCases {
        @Test
        @DisplayName("Returns an empty list when no users exist")
        void getAllUsers_Empty() {
            when(repository.findAll()).thenReturn(List.of());

            assertTrue(service.getAllUsers().isEmpty());
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("Returns empty for an identifier that is not present")
        void getUserById_Missing() {
            when(repository.findById(Integer.MAX_VALUE)).thenReturn(Optional.empty());

            assertTrue(service.getUserById(Integer.MAX_VALUE).isEmpty());
            verifyNoInteractions(mapper);
        }
    }
}