package com.opao.pp_api.features.user;

import com.opao.pp_api.common.exceptions.GlobalExceptionHandler;
import com.opao.pp_api.features.user.dto.request.UserCreateRequest;
import com.opao.pp_api.features.user.dto.request.UserUpdateRequest;
import com.opao.pp_api.features.user.dto.response.UserResponse;
import com.opao.pp_api.features.user.mapper.UserDtoMapper;
import com.opao.pp_api.features.user.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
class UserControllerTests {
    @Mock private UserService userService;
    @Mock private UserDtoMapper dtoMapper;
    @InjectMocks private UserController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPaths {
        @Test
        @DisplayName("Returns an empty collection when there are no user accounts")
        void getAllUsers_Empty() throws Exception {
            when(userService.getAllUsers()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/users").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()).andExpect(content().json("[]"));
            verifyNoInteractions(dtoMapper);
        }

        @Test
        @DisplayName("Looks up a user by username")
        void getUserByUsername_Success() throws Exception {
            User domain = User.builder().id(3).username("jdoe").build();
            when(userService.getUserByUsername("jdoe")).thenReturn(Optional.of(domain));
            when(dtoMapper.toResponse(domain)).thenReturn(response(3, "jdoe"));

            mockMvc.perform(get("/api/v1/users/username/jdoe"))
                    .andExpect(status().isOk());
            verify(userService).getUserByUsername("jdoe");
        }

        @Test
        @DisplayName("Creates a user profile")
        void createUser_Success() throws Exception {
            User domain = User.builder().username("janedoe").build();
            when(dtoMapper.toDomain(any(UserCreateRequest.class))).thenReturn(domain);
            when(userService.create(domain)).thenReturn(domain);

            mockMvc.perform(post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userName\":\"janedoe\",\"fullName\":\"Jane Doe\",\"emailAddress\":\"jane@example.com\",\"phoneNumber\":\"5045550123\",\"password\":\"StrongPass123!\",\"roleId\":1,\"statusId\":1}"))
                    .andExpect(status().isCreated());

            verify(userService).create(domain);
        }

        @Test
        @DisplayName("Updates an existing user profile")
        void updateUser_Success() throws Exception {
            User domain = User.builder().id(3).fullName("Updated Name").build();
            when(dtoMapper.toResponse(domain)).thenReturn(response(3, "jdoe"));
            when(dtoMapper.toDomain(eq(3), any(UserUpdateRequest.class))).thenReturn(domain);
            when(userService.updateUser(3, domain)).thenReturn(Optional.of(domain));

            mockMvc.perform(put("/api/v1/users/3")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"fullName\":\"Updated Name\"}"))
                    .andExpect(status().isOk());

            verify(userService).updateUser(3, domain);
        }

        @Test
        @DisplayName("Deletes an existing user")
        void deleteUser_Success() throws Exception {
            when(userService.deleteUser(3)).thenReturn(true);

            mockMvc.perform(delete("/api/v1/users/3"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Exception Case Scenarios")
    class ExceptionCases {
        @Test
        @DisplayName("Maps a user lookup exception to 404")
        void getUserById_NotFoundException() throws Exception {
            when(userService.getUserById(404)).thenThrow(new EntityNotFoundException("missing"));

            mockMvc.perform(get("/api/v1/users/404"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Edge Case Scenarios")
    class EdgeCases {
        @Test
        @DisplayName("Returns 404 when the largest integer identifier is absent")
        void getUserById_MaxIntegerMissing() throws Exception {
            when(userService.getUserById(Integer.MAX_VALUE)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/users/" + Integer.MAX_VALUE))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Returns 404 when deleting an absent user")
        void deleteUser_Missing() throws Exception {
            when(userService.deleteUser(-1)).thenReturn(false);

            mockMvc.perform(delete("/api/v1/users/-1"))
                    .andExpect(status().isNotFound());
        }
    }

    private UserResponse response(Integer id, String username) {
        return new UserResponse(id, username, null, null, null, null, null, null, null, null);
    }
}