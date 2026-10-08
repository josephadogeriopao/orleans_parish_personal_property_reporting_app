package com.opao.pp_api.features.business_type;

/**
 * @author Joseph Adogeri
 * @since 06-OCT-2026
 * @version 1.0.6
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opao.pp_api.common.exceptions.GlobalExceptionHandler;
import com.opao.pp_api.features.business_type.dto.request.BusinessTypeCreateRequest;
import com.opao.pp_api.features.business_type.dto.request.BusinessTypeUpdateRequest;
import com.opao.pp_api.features.business_type.dto.response.BusinessTypeResponse;
import com.opao.pp_api.features.business_type.mapper.BusinessTypeDtoMapper;
import com.opao.pp_api.features.business_type.model.BusinessType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessTypeController Standalone Unit Tests")
class BusinessTypeControllerTests {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private BusinessTypeService service;

    @Mock
    private BusinessTypeDtoMapper dtoMapper;

    @InjectMocks
    private BusinessTypeController controller;

    @BeforeEach
    void setUp() {
        // 💡 Manually build MockMvc and explicitly wire up your secure GlobalExceptionHandler advice
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 🟢 1. HAPPY PATH SCENARIOS
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Happy Path API Scenarios")
    class HappyPaths {

        @Test
        @DisplayName("GET /api/v1/business-types - Should return 200 OK with populated array matrix")
        void getAll_Success() throws Exception {
            // Arrange
            BusinessType domain = BusinessType.builder().id(1).code(10).description("Retail").build();
            BusinessTypeResponse response = new BusinessTypeResponse(1, 10, "Retail");

            when(service.getAllBusinessTypes()).thenReturn(List.of(domain));
            when(dtoMapper.toResponse(domain)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/v1/business-types")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].code").value(10))
                    .andExpect(jsonPath("$[0].description").value("Retail"));
        }

        @Test
        @DisplayName("GET /api/v1/business-types/{id} - Should return 200 OK when record exists")
        void getById_Success() throws Exception {
            // Arrange
            BusinessType domain = BusinessType.builder().id(1).code(10).description("Retail").build();
            BusinessTypeResponse response = new BusinessTypeResponse(1, 10, "Retail");

            when(service.getBusinessTypeById(1)).thenReturn(Optional.of(domain));
            when(dtoMapper.toResponse(domain)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/v1/business-types/1")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.code").value(10))
                    .andExpect(jsonPath("$.description").value("Retail"));
        }

        @Test
        @DisplayName("DELETE /api/v1/business-types/{id} - Should return 204 No Content upon total delete success")
        void delete_Success() throws Exception {
            // Arrange
            doNothing().when(service).deleteBusinessType(1);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/business-types/1"))
                    .andExpect(status().isNoContent());

            verify(service, times(1)).deleteBusinessType(1);
        }

        @Test
        @DisplayName("POST /api/v1/business-types - Creates a valid business type")
        void create_Success() throws Exception {
            BusinessTypeCreateRequest request = new BusinessTypeCreateRequest(42, "Consulting");
            BusinessType input = BusinessType.builder().code(42).description("Consulting").build();
            BusinessType created = BusinessType.builder().id(7).code(42).description("Consulting").build();
            BusinessTypeResponse response = new BusinessTypeResponse(7, 42, "Consulting");

            when(dtoMapper.toDomain(request)).thenReturn(input);
            when(service.createBusinessType(input)).thenReturn(created);
            when(dtoMapper.toResponse(created)).thenReturn(response);

            mockMvc.perform(post("/api/v1/business-types")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(7))
                    .andExpect(jsonPath("$.code").value(42))
                    .andExpect(jsonPath("$.description").value("Consulting"));
        }

        @Test
        @DisplayName("PUT /api/v1/business-types/{id} - Updates a matching business type")
        void update_Success() throws Exception {
            BusinessTypeUpdateRequest request = new BusinessTypeUpdateRequest(null, "Professional Services");
            BusinessType update = BusinessType.builder().description("Professional Services").build();
            BusinessType updated = BusinessType.builder().id(7).code(42).description("Professional Services").build();
            BusinessTypeResponse response = new BusinessTypeResponse(7, 42, "Professional Services");

            when(dtoMapper.toDomain(7, request)).thenReturn(update);
            when(service.updateBusinessType(7, update)).thenReturn(Optional.of(updated));
            when(dtoMapper.toResponse(updated)).thenReturn(response);

            mockMvc.perform(put("/api/v1/business-types/7")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(7))
                    .andExpect(jsonPath("$.description").value("Professional Services"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 🔴 2. EXCEPTION SCENARIOS
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Exception and Error Contract Diagnostics")
    class ExceptionPaths {

        @Test
        @DisplayName("GET /api/v1/business-types/{id} - Should emit secure generic contract when ID drops on 404")
        void getById_NotFound_ReturnsSecureContract() throws Exception {
            // Arrange
            when(service.getBusinessTypeById(999)).thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(get("/api/v1/business-types/999")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.trackingId").exists())
                    .andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.method").doesNotExist()) // ➔ Shielder validation check passing
                    .andExpect(jsonPath("$.url").doesNotExist());
        }

        @Test
        @DisplayName("POST /api/v1/business-types - Maps duplicate code rejection to 400")
        void create_DuplicateCode_ReturnsBadRequest() throws Exception {
            BusinessTypeCreateRequest request = new BusinessTypeCreateRequest(42, "Consulting");
            BusinessType input = BusinessType.builder().code(42).description("Consulting").build();
            when(dtoMapper.toDomain(request)).thenReturn(input);
            when(service.createBusinessType(input)).thenThrow(new IllegalArgumentException("Business code is already registered"));

            mockMvc.perform(post("/api/v1/business-types")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("INVALID_ARGUMENT"))
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @DisplayName("PUT /api/v1/business-types/{id} - Returns 404 when the target does not exist")
        void update_NotFound() throws Exception {
            BusinessTypeUpdateRequest request = new BusinessTypeUpdateRequest(null, "Unused");
            BusinessType update = BusinessType.builder().description("Unused").build();
            when(dtoMapper.toDomain(999, request)).thenReturn(update);
            when(service.updateBusinessType(999, update)).thenReturn(Optional.empty());

            mockMvc.perform(put("/api/v1/business-types/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Boundary and Empty-Result Scenarios")
    class EdgeCases {

        @Test
        @DisplayName("GET /api/v1/business-types - Returns an empty JSON array when no types exist")
        void getAll_EmptyResult() throws Exception {
            when(service.getAllBusinessTypes()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/business-types").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));

            verifyNoInteractions(dtoMapper);
        }

        @Test
        @DisplayName("GET /api/v1/business-types/{id} - Accepts the largest positive integer path ID")
        void getById_MaxIntegerId() throws Exception {
            when(service.getBusinessTypeById(Integer.MAX_VALUE)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/business-types/{id}", Integer.MAX_VALUE)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            verify(service).getBusinessTypeById(Integer.MAX_VALUE);
        }
    }
}
