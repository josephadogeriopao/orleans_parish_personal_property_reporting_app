package com.opao.pp_api.features.noa_pp_lat5_inventories;

import com.opao.pp_api.common.exceptions.GlobalExceptionHandler;
import com.opao.pp_api.features.noa_pp_lat5_inventories.dto.request.NoaPpLat5InventoriesCreateRequest;
import com.opao.pp_api.features.noa_pp_lat5_inventories.dto.request.NoaPpLat5InventoriesUpdateRequest;
import com.opao.pp_api.features.noa_pp_lat5_inventories.dto.response.NoaPpLat5InventoriesResponse;
import com.opao.pp_api.features.noa_pp_lat5_inventories.model.NoaPpLat5Inventories;
import com.opao.pp_api.features.noa_pp_lat5_inventories.mapper.NoaPpLat5InventoriesDtoMapper;
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
@DisplayName("NoaPpLat5InventoriesController Unit Tests")
class NoaPpLat5InventoriesControllerTests {
    @Mock private NoaPpLat5InventoriesService service;
    @Mock private NoaPpLat5InventoriesDtoMapper dtoMapper;
    @InjectMocks private NoaPpLat5InventoriesController controller;
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
        @DisplayName("Returns an empty inventory collection")
        void getAllInventories_Empty() throws Exception {
            when(service.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/noa-pp-lat5-inventories").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()).andExpect(content().json("[]"));
            verifyNoInteractions(dtoMapper);
        }

        @Test
        @DisplayName("Routes inventory-type searches to the service")
        void searchInventories_ByType() throws Exception {
            when(service.findByInventoryType("EQUIPMENT")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/noa-pp-lat5-inventories/search").param("inventoryType", "EQUIPMENT"))
                    .andExpect(status().isOk()).andExpect(content().json("[]"));
            verify(service).findByInventoryType("EQUIPMENT");
        }

        @Test
        @DisplayName("Creates an inventory record")
        void createInventory_Success() throws Exception {
            NoaPpLat5Inventories domain = NoaPpLat5Inventories.builder().inventoryType("EQUIPMENT").build();
            when(dtoMapper.toDomain(any(NoaPpLat5InventoriesCreateRequest.class))).thenReturn(domain);
            when(service.create(domain)).thenReturn(domain);

            mockMvc.perform(post("/api/v1/noa-pp-lat5-inventories")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"jurisdiction\":\"ORL\",\"parcelAddress\":\"P-100\",\"taxYear\":2026,\"filingYear\":2026,\"inventoryType\":\"EQUIPMENT\",\"inventoryAmount\":0}"))
                    .andExpect(status().isCreated());

            verify(service).create(domain);
        }

        @Test
        @DisplayName("Updates an existing inventory record")
        void updateInventory_Success() throws Exception {
            NoaPpLat5Inventories domain = NoaPpLat5Inventories.builder().id(1).inventoryType("UPDATED").build();
            when(dtoMapper.toResponse(domain)).thenReturn(response(1));
            when(dtoMapper.toDomain(eq(1), any(NoaPpLat5InventoriesUpdateRequest.class))).thenReturn(domain);
            when(service.update(1, domain)).thenReturn(Optional.of(domain));

            mockMvc.perform(put("/api/v1/noa-pp-lat5-inventories/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"id\":1,\"inventoryType\":\"UPDATED\"}"))
                    .andExpect(status().isOk());

            verify(service).update(1, domain);
        }

        @Test
        @DisplayName("Returns an inventory when its identifier exists")
        void getInventoryById_Success() throws Exception {
            NoaPpLat5Inventories domain = NoaPpLat5Inventories.builder().id(1).build();
            when(service.findById(1)).thenReturn(Optional.of(domain));
            when(dtoMapper.toResponse(domain)).thenReturn(response(1));

            mockMvc.perform(get("/api/v1/noa-pp-lat5-inventories/1"))
                    .andExpect(status().isOk());
            verify(dtoMapper).toResponse(domain);
        }

        @Test
        @DisplayName("Deletes an existing inventory record")
        void deleteInventory_Success() throws Exception {
            when(service.deleteById(1)).thenReturn(true);

            mockMvc.perform(delete("/api/v1/noa-pp-lat5-inventories/1"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Exception Case Scenarios")
    class ExceptionCases {
        @Test
        @DisplayName("Maps missing inventory exceptions to 404")
        void getInventoryById_MissingException() throws Exception {
            when(service.findById(404)).thenThrow(new jakarta.persistence.EntityNotFoundException("missing"));

            mockMvc.perform(get("/api/v1/noa-pp-lat5-inventories/404"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Edge Case Scenarios")
    class EdgeCases {
        @Test
        @DisplayName("Returns 404 for an absent inventory identifier")
        void getInventoryById_Missing() throws Exception {
            when(service.findById(-1)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/noa-pp-lat5-inventories/-1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Returns 404 when deleting an absent inventory")
        void deleteInventory_Missing() throws Exception {
            when(service.deleteById(Integer.MAX_VALUE)).thenReturn(false);

            mockMvc.perform(delete("/api/v1/noa-pp-lat5-inventories/" + Integer.MAX_VALUE))
                    .andExpect(status().isNotFound());
        }
    }

    private NoaPpLat5InventoriesResponse response(Integer id) {
        return new NoaPpLat5InventoriesResponse(id, null, null, null, null, null, null, null);
    }
}