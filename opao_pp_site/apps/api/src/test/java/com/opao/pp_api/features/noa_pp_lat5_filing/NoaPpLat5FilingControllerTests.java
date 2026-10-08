package com.opao.pp_api.features.noa_pp_lat5_filing;

import com.opao.pp_api.common.exceptions.GlobalExceptionHandler;
import com.opao.pp_api.features.noa_pp_lat5_filing.dto.request.NoaPpLat5FilingCreateRequest;
import com.opao.pp_api.features.noa_pp_lat5_filing.dto.request.NoaPpLat5FilingUpdateRequest;
import com.opao.pp_api.features.noa_pp_lat5_filing.dto.response.NoaPpLat5FilingResponse;
import com.opao.pp_api.features.noa_pp_lat5_filing.model.NoaPpLat5Filing;
import com.opao.pp_api.features.noa_pp_lat5_filing.mapper.NoaPpLat5FilingDtoMapper;
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
@DisplayName("NoaPpLat5FilingController Unit Tests")
class NoaPpLat5FilingControllerTests {
    @Mock private NoaPpLat5FilingService service;
    @Mock private NoaPpLat5FilingDtoMapper dtoMapper;
    @InjectMocks private NoaPpLat5FilingController controller;
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
        @DisplayName("Returns an empty collection when there are no filing details")
        void getAllFilings_Empty() throws Exception {
            when(service.findAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/noa-pp-lat5-filings").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()).andExpect(content().json("[]"));
            verifyNoInteractions(dtoMapper);
        }

        @Test
        @DisplayName("Routes category searches to the filing service")
        void searchFilings_ByCategory() throws Exception {
            when(service.findByCategory("EQUIPMENT")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/noa-pp-lat5-filings/search").param("category", "EQUIPMENT"))
                    .andExpect(status().isOk()).andExpect(content().json("[]"));
            verify(service).findByCategory("EQUIPMENT");
        }

        @Test
        @DisplayName("Creates a LAT5 filing detail")
        void createFiling_Success() throws Exception {
            NoaPpLat5Filing domain = NoaPpLat5Filing.builder().category("EQUIPMENT").build();
            when(dtoMapper.toDomain(any(NoaPpLat5FilingCreateRequest.class))).thenReturn(domain);
            when(service.create(domain)).thenReturn(domain);

            mockMvc.perform(post("/api/v1/noa-pp-lat5-filings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"jurisdiction\":\"ORL\",\"parcelId\":\"P-100\",\"taxYear\":2026,\"category\":\"EQUIPMENT\",\"propertyType\":\"MACHINERY\",\"filingYear\":2026,\"yearAcquired\":2020,\"noaPpLat5Id\":1,\"consignerTelNo\":\"5045550123\"}"))
                    .andExpect(status().isCreated());

            verify(service).create(domain);
        }

        @Test
        @DisplayName("Updates an existing filing detail")
        void updateFiling_Success() throws Exception {
            NoaPpLat5Filing domain = NoaPpLat5Filing.builder().id(1).category("UPDATED").build();
            when(dtoMapper.toResponse(domain)).thenReturn(response(1));
            when(dtoMapper.toDomain(eq(1), any(NoaPpLat5FilingUpdateRequest.class))).thenReturn(domain);
            when(service.update(1, domain)).thenReturn(Optional.of(domain));

            mockMvc.perform(put("/api/v1/noa-pp-lat5-filings/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"id\":1,\"category\":\"UPDATED\"}"))
                    .andExpect(status().isOk());

            verify(service).update(1, domain);
        }

        @Test
        @DisplayName("Returns a filing detail when its identifier exists")
        void getFilingById_Success() throws Exception {
            NoaPpLat5Filing domain = NoaPpLat5Filing.builder().id(1).build();
            when(service.findById(1)).thenReturn(Optional.of(domain));
            when(dtoMapper.toResponse(domain)).thenReturn(response(1));

            mockMvc.perform(get("/api/v1/noa-pp-lat5-filings/1"))
                    .andExpect(status().isOk());
            verify(dtoMapper).toResponse(domain);
        }

        @Test
        @DisplayName("Deletes an existing filing detail")
        void deleteFiling_Success() throws Exception {
            when(service.deleteById(1)).thenReturn(true);

            mockMvc.perform(delete("/api/v1/noa-pp-lat5-filings/1"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Exception Case Scenarios")
    class ExceptionCases {
        @Test
        @DisplayName("Maps a missing filing exception to 404")
        void getFilingById_MissingException() throws Exception {
            when(service.findById(404)).thenThrow(new jakarta.persistence.EntityNotFoundException("missing"));

            mockMvc.perform(get("/api/v1/noa-pp-lat5-filings/404"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Edge Case Scenarios")
    class EdgeCases {
        @Test
        @DisplayName("Returns 404 for an absent filing identifier")
        void getFilingById_Missing() throws Exception {
            when(service.findById(-1)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/noa-pp-lat5-filings/-1"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Returns 404 when deleting an absent filing")
        void deleteFiling_Missing() throws Exception {
            when(service.deleteById(Integer.MAX_VALUE)).thenReturn(false);

            mockMvc.perform(delete("/api/v1/noa-pp-lat5-filings/" + Integer.MAX_VALUE))
                    .andExpect(status().isNotFound());
        }
    }

    private NoaPpLat5FilingResponse response(Integer id) {
        return new NoaPpLat5FilingResponse(id, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null);
    }
}