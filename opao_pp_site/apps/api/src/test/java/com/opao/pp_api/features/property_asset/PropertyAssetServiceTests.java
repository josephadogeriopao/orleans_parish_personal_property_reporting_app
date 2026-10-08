package com.opao.pp_api.features.property_asset;

import com.opao.pp_api.features.property_asset.mapper.PropertyAssetMapper;
import com.opao.pp_api.features.property_asset.model.PropertyAsset;
import com.opao.pp_api.features.property_asset.model.PropertyAssetEntity;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PropertyAssetService Unit Tests")
class PropertyAssetServiceTests {
    @Mock private PropertyAssetRepository repository;
    @Mock private PropertyAssetMapper mapper;
    @InjectMocks private PropertyAssetService service;

    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPaths {
        @Test
        @DisplayName("Creates and maps a property asset")
        void create_Success() {
            PropertyAsset domain = PropertyAsset.builder().id(2).category("MACHINERY").build();
            PropertyAssetEntity entity = new PropertyAssetEntity();
            when(mapper.toEntity(domain)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(domain);

            assertSame(domain, service.create(domain));
            verify(repository).save(entity);
        }

        @Test
        @DisplayName("Returns the repository count")
        void getPropertyAssetCount_Success() {
            when(repository.count()).thenReturn(12L);

            assertEquals(12L, service.getPropertyAssetCount());
        }

        @Test
        @DisplayName("Updates an existing property asset")
        void edit_Success() {
            PropertyAssetEntity existing = new PropertyAssetEntity();
            PropertyAsset input = PropertyAsset.builder().category("UPDATED").build();
            PropertyAsset updated = PropertyAsset.builder().id(2).category("UPDATED").build();
            when(repository.findById(2)).thenReturn(Optional.of(existing));
            when(repository.save(existing)).thenReturn(existing);
            when(mapper.toDomain(existing)).thenReturn(updated);

            assertSame(updated, service.edit(2, input));
            verify(mapper).updateEntityFromDomain(input, existing);
        }
    }

    @Nested
    @DisplayName("Exception Case Scenarios")
    class ExceptionCases {
        @Test
        @DisplayName("Throws when updating an asset that does not exist")
        void edit_MissingAsset() {
            when(repository.findById(404)).thenReturn(Optional.empty());

            EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                    () -> service.edit(404, PropertyAsset.builder().category("Updated").build()));
            assertEquals("PropertyAsset with id 404 no longer exists.", exception.getMessage());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Propagates persistence failures when creating an asset")
        void create_RepositoryFailure() {
            PropertyAsset domain = PropertyAsset.builder().category("MACHINERY").build();
            PropertyAssetEntity entity = new PropertyAssetEntity();
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
        @DisplayName("Preserves metadata for an empty page")
        void findPaged_Empty() {
            var pageable = PageRequest.of(2, 20);
            when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

            var page = service.findPropertyAssetEntities(pageable);

            assertTrue(page.isEmpty());
            assertEquals(2, page.getNumber());
            assertEquals(20, page.getSize());
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("Returns an empty result for an absent identifier")
        void findById_Missing() {
            when(repository.findById(Integer.MAX_VALUE)).thenReturn(Optional.empty());

            assertTrue(service.findPropertyAsset(Integer.MAX_VALUE).isEmpty());
            verifyNoInteractions(mapper);
        }
    }
}