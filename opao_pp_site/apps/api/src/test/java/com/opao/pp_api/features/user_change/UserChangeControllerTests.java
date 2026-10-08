package com.opao.pp_api.features.user_change;

import com.opao.pp_api.common.exceptions.GlobalExceptionHandler;
import com.opao.pp_api.features.user_change.dto.request.UserChangeCreateRequest;
import com.opao.pp_api.features.user_change.mapper.UserChangeDtoMapper;
import com.opao.pp_api.features.user_change.model.UserChange;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserChangeController Unit Tests")
class UserChangeControllerTests {
    @Mock private UserChangeService service;
    @Mock private UserChangeDtoMapper dtoMapper;
    @InjectMocks private UserChangeController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPaths {
        @Test
        @DisplayName("Returns the user-change count")
        void getCount_Success() throws Exception {
            when(service.getUserChangeCount()).thenReturn(11L);

            mockMvc.perform(get("/api/v1/user-changes/count"))
                    .andExpect(status().isOk()).andExpect(content().string("11"));
        }

        @Test
        @DisplayName("Returns an empty collection when there are no user-change records")
        void getAll_Empty() throws Exception {
            when(service.findUserChangeEntities()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/user-changes").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()).andExpect(content().json("[]"));
            verifyNoInteractions(dtoMapper);
        }

        @Test
        @DisplayName("Creates a user-change record")
        void createUserChange_Success() throws Exception {
            UserChange domain = UserChange.builder().verificationCode("VERIFY-001").userId(1).userChangeTypeId(1).build();
            when(dtoMapper.toDomain(any(UserChangeCreateRequest.class))).thenReturn(domain);
            when(service.create(domain)).thenReturn(domain);

            mockMvc.perform(post("/api/v1/user-changes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"verificationCode\":\"VERIFY-001\",\"userChangeTypeId\":1,\"userId\":1}"))
                    .andExpect(status().isCreated());

            verify(service).create(domain);
        }

        @Test
        @DisplayName("Deletes an existing user-change record")
        void deleteUserChange_Success() throws Exception {
            doNothing().when(service).destroy(3);

            mockMvc.perform(delete("/api/v1/user-changes/3"))
                    .andExpect(status().isNoContent());

            verify(service).destroy(3);
        }
    }

    @Nested
    @DisplayName("Exception Case Scenarios")
    class ExceptionCases {
        @Test
        @DisplayName("Maps a missing user-change exception to 404")
        void getById_MissingException() throws Exception {
            when(service.findUserChange(404)).thenThrow(new EntityNotFoundException("missing"));

            mockMvc.perform(get("/api/v1/user-changes/404"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Edge Case Scenarios")
    class EdgeCases {
        @Test
        @DisplayName("Returns an empty page for a valid page request")
        void getPaged_Empty() throws Exception {
            var pageable = PageRequest.of(2, 5);
            when(service.findUserChangeEntities(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

            mockMvc.perform(get("/api/v1/user-changes/pageable").param("page", "2").param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Returns 404 for an unknown verification code")
        void getByVerificationCode_Missing() throws Exception {
            when(service.findUserChangeByVerificationCode("unknown")).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/user-changes/verification/unknown"))
                    .andExpect(status().isNotFound());
        }
    }
}