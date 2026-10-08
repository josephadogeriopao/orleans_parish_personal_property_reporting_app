package com.opao.pp_api.features.user_change;

import com.opao.pp_api.features.user_change.mapper.UserChangeMapper;
import com.opao.pp_api.features.user_change.model.UserChange;
import com.opao.pp_api.features.user_change.model.UserChangeEntity;
import jakarta.persistence.EntityManager;
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
@DisplayName("UserChangeService Unit Tests")
class UserChangeServiceTests {
    @Mock private UserChangeRepository repository;
    @Mock private UserChangeMapper mapper;
    @Mock private EntityManager entityManager;
    @InjectMocks private UserChangeService service;

    @Nested
    @DisplayName("Happy Path Scenarios")
    class HappyPaths {
        @Test
        @DisplayName("Creates and maps a user-change record without optional relationships")
        void create_Success() {
            UserChange domain = UserChange.builder().id(3).verificationCode("verify-3").build();
            UserChangeEntity entity = new UserChangeEntity();
            when(mapper.toEntity(domain)).thenReturn(entity);
            when(repository.save(entity)).thenReturn(entity);
            when(mapper.toDomain(entity)).thenReturn(domain);

            assertSame(domain, service.create(domain));
            verify(repository).save(entity);
            verifyNoInteractions(entityManager);
        }

        @Test
        @DisplayName("Returns the total user-change count")
        void getUserChangeCount_Success() {
            when(repository.count()).thenReturn(9L);

            assertEquals(9L, service.getUserChangeCount());
        }

        @Test
        @DisplayName("Updates an existing user-change record")
        void edit_Success() {
            UserChangeEntity existing = new UserChangeEntity();
            UserChange input = UserChange.builder().verificationCode("updated-code").build();
            UserChange updated = UserChange.builder().id(3).verificationCode("updated-code").build();
            when(repository.findById(3L)).thenReturn(Optional.of(existing));
            when(repository.save(existing)).thenReturn(existing);
            when(mapper.toDomain(existing)).thenReturn(updated);

            assertEquals("updated-code", service.edit(3, input).getVerificationCode());
            verify(mapper).updateEntityFromDomain(input, existing);
            verifyNoInteractions(entityManager);
        }
    }

    @Nested
    @DisplayName("Exception Case Scenarios")
    class ExceptionCases {
        @Test
        @DisplayName("Throws when deleting an absent user-change record")
        void destroy_Missing() {
            when(repository.existsById(404L)).thenReturn(false);

            assertThrows(EntityNotFoundException.class, () -> service.destroy(404));
            verify(repository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Propagates persistence failures during creation")
        void create_RepositoryFailure() {
            UserChange domain = UserChange.builder().verificationCode("verify").build();
            UserChangeEntity entity = new UserChangeEntity();
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
        @DisplayName("Returns an empty user-change list without mapping")
        void findAll_Empty() {
            when(repository.findAll()).thenReturn(List.of());

            assertTrue(service.findUserChangeEntities().isEmpty());
            verifyNoInteractions(mapper);
        }

        @Test
        @DisplayName("Preserves metadata for an empty paged result")
        void findPaged_Empty() {
            var pageable = PageRequest.of(1, 10);
            when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

            var page = service.findUserChangeEntities(pageable);

            assertTrue(page.isEmpty());
            assertEquals(1, page.getNumber());
            verifyNoInteractions(mapper);
        }
    }
}