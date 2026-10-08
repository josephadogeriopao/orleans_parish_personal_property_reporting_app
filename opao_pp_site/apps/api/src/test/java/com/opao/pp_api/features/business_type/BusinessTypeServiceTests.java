package com.opao.pp_api.features.business_type;

import com.opao.pp_api.features.business_type.mapper.BusinessTypeMapper;
import com.opao.pp_api.features.business_type.model.BusinessType;
import com.opao.pp_api.features.business_type.model.BusinessTypeEntity;
import com.opao.pp_api.common.exceptions.ResourceNotFoundException; // Ensure this import matches your exception package location
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessTypeService Unit Tests")
class BusinessTypeServiceTests {

    @Mock
    private BusinessTypeRepository repository;

    @Mock
    private BusinessTypeMapper mapper;

    @InjectMocks
    private BusinessTypeService service;

    // ─────────────────────────────────────────────────────────────────────────
    // 🟢 1. HAPPY PATHS (Expected Successful Operations)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPaths {

        @Test
        @DisplayName("Should successfully persist a new business type when code is unique")
        void createBusinessType_Success() {
            BusinessType inputDomain = BusinessType.builder().code(101).description("Retail").build();
            BusinessTypeEntity mockEntity = new BusinessTypeEntity();
            
            when(repository.findByBusinessCode(101)).thenReturn(Optional.empty());
            when(mapper.toEntity(inputDomain)).thenReturn(mockEntity);
            when(repository.save(mockEntity)).thenReturn(mockEntity);
            when(mapper.toDomain(mockEntity)).thenReturn(inputDomain);

            BusinessType result = service.createBusinessType(inputDomain);

            assertNotNull(result);
            assertEquals(101, result.getCode());
            verify(repository, times(1)).save(mockEntity);
        }

        @Test
        @DisplayName("Should successfully delete a business type if it exists in the database")
        void deleteBusinessType_Success() {
            // Arrange
            when(repository.existsById(1)).thenReturn(true);

            // Act & Assert
            // 💡 FIX: Assert that a void method executes cleanly without throwing any errors
            assertDoesNotThrow(() -> service.deleteBusinessType(1));
            
            verify(repository, times(1)).deleteById(1);
        }

        @Test
        @DisplayName("Should map a persisted business type when looking up an existing identifier")
        void getBusinessTypeById_Success() {
            BusinessTypeEntity entity = new BusinessTypeEntity();
            BusinessType domain = BusinessType.builder().id(8).code(108).description("Manufacturing").build();
            when(repository.findById(8)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(domain);

            Optional<BusinessType> result = service.getBusinessTypeById(8);

            assertTrue(result.isPresent());
            assertEquals(108, result.get().getCode());
            verify(mapper).toDomain(entity);
        }

        @Test
        @DisplayName("Should persist and map changes to an existing business type")
        void updateBusinessType_Success() {
            BusinessTypeEntity existingEntity = new BusinessTypeEntity();
            BusinessType input = BusinessType.builder().description("Updated description").build();
            BusinessType updated = BusinessType.builder().id(3).code(103).description("Updated description").build();
            when(repository.findById(3)).thenReturn(Optional.of(existingEntity));
            when(repository.save(existingEntity)).thenReturn(existingEntity);
            when(mapper.toDomain(existingEntity)).thenReturn(updated);

            Optional<BusinessType> result = service.updateBusinessType(3, input);

            assertTrue(result.isPresent());
            assertEquals("Updated description", result.get().getDescription());
            verify(mapper).updateEntityFromDomain(input, existingEntity);
            verify(repository).save(existingEntity);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 🟡 2. EDGE CASES (Boundary Conditions and Unexpected Inputs)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Edge Case Scenarios")
    class EdgeCases {

        @Test
        @DisplayName("Should return an empty list gracefully when no records exist in the database")
        void getAllBusinessTypes_ReturnsEmptyList() {
            when(repository.findAll()).thenReturn(Collections.emptyList());

            List<BusinessType> result = service.getAllBusinessTypes();

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(mapper, never()).toDomain(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 🔴 3. EXCEPTION CASES (Business Violations and Error Conditions)
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Exception Case Scenarios")
    class ExceptionCases {

        @Test
        @DisplayName("Should throw IllegalArgumentException when registering a duplicate code")
        void createBusinessType_ThrowsExceptionForDuplicateCode() {
            BusinessType duplicateDomain = BusinessType.builder().code(101).description("Wholesale").build();
            BusinessTypeEntity existingEntity = new BusinessTypeEntity();
            
            when(repository.findByBusinessCode(101)).thenReturn(Optional.of(existingEntity));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                service.createBusinessType(duplicateDomain);
            });

            assertEquals("Business code is already registered", exception.getMessage());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Should return empty Optional when updating a non-existent ID context")
        void updateBusinessType_ReturnsEmptyForMissingId() {
            BusinessType updateInput = BusinessType.builder().code(102).description("Manufacturing").build();
            when(repository.findById(999)).thenReturn(Optional.empty());

            Optional<BusinessType> result = service.updateBusinessType(999, updateInput);

            assertTrue(result.isEmpty());
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when trying to delete an ID that does not exist")
        void deleteBusinessType_ThrowsResourceNotFoundException() {
            // Arrange
            // 💡 FIX: Moved from "EdgeCases" to "ExceptionCases" because missing IDs now throw explicit exceptions
            when(repository.existsById(999)).thenReturn(false);

            // Act & Assert
            ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
                service.deleteBusinessType(999);
            });

            assertEquals("Business type record with ID 999 does not exist.", exception.getMessage());
            verify(repository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Should propagate persistence failures without mapping an unsaved business type")
        void createBusinessType_PropagatesRepositoryFailure() {
            BusinessType input = BusinessType.builder().code(203).description("Retail").build();
            BusinessTypeEntity entity = new BusinessTypeEntity();
            IllegalStateException failure = new IllegalStateException("database unavailable");
            when(repository.findByBusinessCode(203)).thenReturn(Optional.empty());
            when(mapper.toEntity(input)).thenReturn(entity);
            when(repository.save(entity)).thenThrow(failure);

            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                    () -> service.createBusinessType(input));

            assertSame(failure, thrown);
            verify(mapper, never()).toDomain(any());
        }
    }

    @Nested
    @DisplayName("Boundary and Empty-Result Scenarios")
    class BoundaryCases {

        @Test
        @DisplayName("Should return an empty Optional for the largest valid identifier when no row exists")
        void getBusinessTypeById_MaxIntegerMissing() {
            when(repository.findById(Integer.MAX_VALUE)).thenReturn(Optional.empty());

            assertTrue(service.getBusinessTypeById(Integer.MAX_VALUE).isEmpty());
            verify(mapper, never()).toDomain(any());
        }

        @Test
        @DisplayName("Should allow the largest integer business code when it is unique")
        void createBusinessType_MaxIntegerCode() {
            BusinessType input = BusinessType.builder().code(Integer.MAX_VALUE).description("Boundary").build();
            BusinessTypeEntity entity = new BusinessTypeEntity();
            BusinessType saved = BusinessType.builder().id(10).code(Integer.MAX_VALUE).description("Boundary").build();
            when(repository.findByBusinessCode(Integer.MAX_VALUE)).thenReturn(Optional.empty());
            when(mapper.toEntity(input)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(saved);

            BusinessType result = service.createBusinessType(input);

            assertEquals(Integer.MAX_VALUE, result.getCode());
            verify(repository).save(entity);
        }
    }
}
