package com.opao.pp_api.features.property_asset;

import com.opao.pp_api.common.exceptions.GlobalExceptionHandler;
import com.opao.pp_api.features.property_asset.dto.request.PropertyAssetCreateRequest;
import com.opao.pp_api.features.property_asset.dto.request.PropertyAssetUpdateRequest;
import com.opao.pp_api.features.property_asset.dto.response.PropertyAssetResponse;
import com.opao.pp_api.features.property_asset.mapper.PropertyAssetDtoMapper;
import com.opao.pp_api.features.property_asset.model.PropertyAsset;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PropertyAssetController Unit Tests")
class PropertyAssetControllerTests {
    @Mock private PropertyAssetService service;
    @Mock private PropertyAssetDtoMapper dtoMapper;
    @InjectMocks private PropertyAssetController controller;
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
        @DisplayName("Returns the total configured property asset count")
        void getAssetCount_Success() throws Exception {
            when(service.getPropertyAssetCount()).thenReturn(15L);

            mockMvc.perform(get("/api/v1/property-assets/count"))
                    .andExpect(status().isOk()).andExpect(content().string("15"));
        }

        @Test
        @DisplayName("Returns an empty search result for a valid effective-life filter")
        void searchAssets_EmptyResult() throws Exception {
            when(service.findPropertyAssetByEffectiveLife(0)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/property-assets/search").param("effectiveLife", "0"))
                    .andExpect(status().isOk()).andExpect(content().json("[]"));
            verify(service).findPropertyAssetByEffectiveLife(0);
            verifyNoInteractions(dtoMapper);
        }

        @Test
        @DisplayName("Creates a property asset")
        void createAsset_Success() throws Exception {
            PropertyAsset domain = PropertyAsset.builder().category("MACHINERY").build();
            when(dtoMapper.toDomain(any(PropertyAssetCreateRequest.class))).thenReturn(domain);
            when(service.create(domain)).thenReturn(domain);

            mockMvc.perform(post("/api/v1/property-assets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"sectionNumber\":1,\"category\":\"MACH\",\"propertyType\":\"EQUIPMENT\",\"assetDescription\":\"Machine\",\"effectiveLife\":5}"))
                    .andExpect(status().isCreated());

            verify(service).create(domain);
        }

        @Test
        @DisplayName("Updates an existing property asset")
        void updateAsset_Success() throws Exception {
            PropertyAsset domain = PropertyAsset.builder().id(2).category("UPD").build();
            when(dtoMapper.toResponse(domain)).thenReturn(response(2));
            when(dtoMapper.toDomain(eq(2), any(PropertyAssetUpdateRequest.class))).thenReturn(domain);
            when(service.edit(2, domain)).thenReturn(domain);

            mockMvc.perform(put("/api/v1/property-assets/2")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"id\":2,\"category\":\"UPD\"}"))
                    .andExpect(status().isOk());

            verify(service).edit(2, domain);
        }
    }

    @Nested
    @DisplayName("Exception Case Scenarios")
    class ExceptionCases {
        @Test
        @DisplayName("Maps a missing asset exception during deletion to 404")
        void deleteAsset_MissingException() throws Exception {
            doThrow(new EntityNotFoundException("PropertyAsset with id 404 no longer exists."))
                    .when(service).destroy(404);

            mockMvc.perform(delete("/api/v1/property-assets/404"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Edge Case Scenarios")
    class EdgeCases {
        @Test
        @DisplayName("Returns 404 for an absent property asset")
        void getAssetById_Missing() throws Exception {
            when(service.findPropertyAsset(-1)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/property-assets/-1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Returns 204 when deleting a valid asset")
        void deleteAsset_Success() throws Exception {
            doNothing().when(service).destroy(3);

            mockMvc.perform(delete("/api/v1/property-assets/3"))
                    .andExpect(status().isNoContent());
            verify(service).destroy(3);
        }
    }

    private PropertyAssetResponse response(Integer id) {
        return new PropertyAssetResponse(id, null, null, null, null, null);
    }
}