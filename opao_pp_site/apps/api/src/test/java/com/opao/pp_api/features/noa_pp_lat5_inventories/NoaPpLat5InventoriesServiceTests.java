package com.opao.pp_api.features.noa_pp_lat5_inventories;

import com.opao.pp_api.features.noa_pp_lat5_inventories.mapper.NoaPpLat5InventoriesMapper;
import com.opao.pp_api.features.noa_pp_lat5_inventories.model.NoaPpLat5Inventories;
import com.opao.pp_api.features.noa_pp_lat5_inventories.model.NoaPpLat5InventoriesEntity;
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
@DisplayName("NoaPpLat5InventoriesService Unit Tests")
class NoaPpLat5InventoriesServiceTests {
    @Mock private NoaPpLat5InventoriesRepository repository;
    @Mock private NoaPpLat5InventoriesMapper mapper;
    @InjectMocks private NoaPpLat5InventoriesService service;

    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPaths {
        @Test
        @DisplayName("Creates and maps an inventory record")
        void create_Success() {
            NoaPpLat5Inventories domain = NoaPpLat5Inventories.builder().id(7).inventoryType("EQUIPMENT").build();
            NoaPpLat5InventoriesEntity entity = new NoaPpLat5InventoriesEntity();
            when(mapper.toEntity(domain)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(domain);

            assertSame(domain, service.create(domain));
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("Maps results from an inventory type search")
        void findByInventoryType_Success() {
            NoaPpLat5InventoriesEntity entity = new NoaPpLat5InventoriesEntity();
            NoaPpLat5Inventories domain = NoaPpLat5Inventories.builder().inventoryType("EQUIPMENT").build();
            when(repository.findByInventoryType("EQUIPMENT")).thenReturn(List.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            assertEquals(List.of(domain), service.findByInventoryType("EQUIPMENT"));
        }

        @Test
        @DisplayName("Updates an existing inventory record")
        void update_Success() {
            NoaPpLat5InventoriesEntity existing = new NoaPpLat5InventoriesEntity(7);
            NoaPpLat5Inventories input = NoaPpLat5Inventories.builder().inventoryType("UPDATED").build();
            NoaPpLat5Inventories updated = NoaPpLat5Inventories.builder().id(7).inventoryType("UPDATED").build();
            when(repository.findByNoaPpLat5InventoriesId(7)).thenReturn(Optional.of(existing));
            when(repository.save(existing)).thenReturn(existing);
            when(mapper.toDomain(existing)).thenReturn(updated);

            assertEquals("UPDATED", service.update(7, input).orElseThrow().getInventoryType());
            verify(mapper).updateEntityFromDomain(input, existing);
        }
    }

    @Nested
    @DisplayName("Exception Case Scenarios")
    class ExceptionCases {
        @Test
        @DisplayName("Propagates persistence errors when creating an inventory record")
        void create_RepositoryFailure() {
            NoaPpLat5Inventories domain = NoaPpLat5Inventories.builder().inventoryType("EQUIPMENT").build();
            NoaPpLat5InventoriesEntity entity = new NoaPpLat5InventoriesEntity();
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
        @DisplayName("Returns an empty list without invoking the mapper")
        void findAll_Empty() {
            when(repository.findAll()).thenReturn(List.of());

            assertTrue(service.findAll().isEmpty());
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("Returns empty when an inventory identifier is absent")
        void findById_Missing() {
            when(repository.findByNoaPpLat5InventoriesId(Integer.MAX_VALUE)).thenReturn(Optional.empty());

            assertTrue(service.findById(Integer.MAX_VALUE).isEmpty());
            verifyNoInteractions(mapper);
        }
    }
}