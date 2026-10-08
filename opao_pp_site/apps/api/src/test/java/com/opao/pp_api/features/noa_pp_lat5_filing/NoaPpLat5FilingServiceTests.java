package com.opao.pp_api.features.noa_pp_lat5_filing;
import com.opao.pp_api.features.noa_pp_lat5_filing.mapper.NoaPpLat5FilingMapper;
import com.opao.pp_api.features.noa_pp_lat5_filing.model.NoaPpLat5Filing;
import com.opao.pp_api.features.noa_pp_lat5_filing.model.NoaPpLat5FilingEntity;
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
@DisplayName("NoaPpLat5FilingService Unit Tests")
class NoaPpLat5FilingServiceTests {
    @Mock private NoaPpLat5FilingRepository repository;
    @Mock private NoaPpLat5FilingMapper mapper;
    @InjectMocks private NoaPpLat5FilingService service;

    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPaths {
        @Test
        @DisplayName("Creates and maps a LAT5 filing detail")
        void create_Success() {
            NoaPpLat5Filing domain = NoaPpLat5Filing.builder().id(5).category("EQUIPMENT").build();
            NoaPpLat5FilingEntity entity = new NoaPpLat5FilingEntity();
            when(mapper.toEntity(domain)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(domain);

            assertSame(domain, service.create(domain));
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("Maps results from a category search")
        void findByCategory_Success() {
            NoaPpLat5FilingEntity entity = new NoaPpLat5FilingEntity();
            NoaPpLat5Filing domain = NoaPpLat5Filing.builder().category("EQUIPMENT").build();
            when(repository.findByCategory("EQUIPMENT")).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            assertEquals(List.of(domain), service.findByCategory("EQUIPMENT"));
        }

        @Test
        @DisplayName("Updates an existing filing detail")
        void update_Success() {
            NoaPpLat5FilingEntity existing = new NoaPpLat5FilingEntity(5);
            NoaPpLat5Filing input = NoaPpLat5Filing.builder().category("UPDATED").build();
            NoaPpLat5Filing updated = NoaPpLat5Filing.builder().id(5).category("UPDATED").build();
            when(repository.findByNoaPpLat5FilingId(5)).thenReturn(Optional.of(existing));
            when(repository.save(existing)).thenReturn(existing);
            when(mapper.toDomain(existing)).thenReturn(updated);

            assertEquals("UPDATED", service.update(5, input).orElseThrow().getCategory());
            verify(mapper).updateEntityFromDomain(input, existing);
        }
    }

    @Nested
    @DisplayName("Exception Case Scenarios")
    class ExceptionCases {
        @Test
        @DisplayName("Propagates persistence errors when creating a filing detail")
        void create_RepositoryFailure() {
            NoaPpLat5Filing domain = NoaPpLat5Filing.builder().category("EQUIPMENT").build();
            NoaPpLat5FilingEntity entity = new NoaPpLat5FilingEntity();
            IllegalStateException failure = new IllegalStateException("database unavailable");
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
        @DisplayName("Returns an empty collection without mapping")
        void findAll_Empty() {
            when(repository.findAll()).thenReturn(List.of());

            assertTrue(service.findAll().isEmpty());
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("Returns empty for an absent filing identifier")
        void findById_Missing() {
            when(repository.findByNoaPpLat5FilingId(Integer.MAX_VALUE)).thenReturn(Optional.empty());

            assertTrue(service.findById(Integer.MAX_VALUE).isEmpty());
            verifyNoInteractions(mapper);
        }
    }
}