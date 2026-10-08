package com.opao.pp_api.features.form;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opao.pp_api.common.exceptions.GlobalExceptionHandler;
import com.opao.pp_api.features.form.dto.request.FormCreateRequest;
import com.opao.pp_api.features.form.dto.request.FormUpdateRequest;
import com.opao.pp_api.features.form.dto.response.FormResponse;
import com.opao.pp_api.features.form.mapper.FormDtoMapper;
import com.opao.pp_api.features.form.model.Form;
import com.opao.pp_api.types.PagedResponse;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
@DisplayName("FormController Standalone Unit Tests")
class FormControllerTests {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private FormService formService;

    @Mock
    private FormDtoMapper dtoMapper;

    @InjectMocks
    private FormController controller;

    @BeforeEach
    void setUp() {
        this.objectMapper.registerModule(new JavaTimeModule());
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        
        // Ensure Page serialization patterns map smoothly inside standalone mock instances
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setViewResolvers((viewName, locale) -> new org.springframework.web.servlet.view.json.MappingJackson2JsonView(objectMapper))
                .build();
    }


    // ─────────────────────────────────────────────────────────────────────────
    // 🟢 1. HAPPY PATH SCENARIOS
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Happy Path API Scenarios")
    class HappyPaths {

        @Test
        @DisplayName("POST /api/v1/forms - Should return 201 Created with mapped response object attributes")
        void createForm_Success() throws Exception {
            // Arrange
            FormCreateRequest request = FormCreateRequest.builder()
                    .title("Tax Form A")
                    .filingYear(2026)
                    .billNumber("BILL123")
                    .pin("PIN99")
                    .formTypeId(10)
                    .userId(500)
                    .build();

            Form domainInput = Form.builder().title("Tax Form A").filingYear(2026).billNumber("BILL123").pin("PIN99").build();
            Form domainOutput = Form.builder().id(1).title("Tax Form A").filingYear(2026).billNumber("BILL123").pin("PIN99").build();
            
            FormResponse response = new FormResponse(
                    1, "Tax Form A", 2026, LocalDateTime.now(), "BILL123", "PIN99", 10, "DRAFT", 500, false
            );

            when(dtoMapper.toDomain(any(FormCreateRequest.class))).thenReturn(domainInput);
            when(formService.create(domainInput)).thenReturn(domainOutput);
            when(dtoMapper.toResponse(domainOutput)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/v1/forms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Tax Form A"))
                    .andExpect(jsonPath("$.filingYear").value(2026))
                    .andExpect(jsonPath("$.formTypeId").value(10))
                    .andExpect(jsonPath("$.userId").value(500))
                    .andExpect(jsonPath("$.statusName").value("DRAFT"));
        }

        @Test
        @DisplayName("PUT /api/v1/forms/{id} - Should return 200 OK after running transactional modifications")
        void updateForm_Success() throws Exception {
            // Arrange
            FormUpdateRequest request = FormUpdateRequest.builder()
                    .id(1)
                    .title("Updated Form Title")
                    .filingYear(2026)
                    .billNumber("BILL123")
                    .pin("PIN99")
                    .formTypeId(10)
                    .build();

            Form domainInput = Form.builder().id(1).title("Updated Form Title").filingYear(2026).build();
            Form domainOutput = Form.builder().id(1).title("Updated Form Title").filingYear(2026).build();
            
            FormResponse response = new FormResponse(
                    1, "Updated Form Title", 2026, LocalDateTime.now(), "BILL123", "PIN99", 10, "SUBMITTED", 500, true
            );

            when(dtoMapper.toDomain(eq(1), any(FormUpdateRequest.class))).thenReturn(domainInput);
            when(formService.edit(eq(1), any(Form.class))).thenReturn(domainOutput);
            when(dtoMapper.toResponse(domainOutput)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/v1/forms/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Updated Form Title"))
                    .andExpect(jsonPath("$.statusName").value("SUBMITTED"))
                    .andExpect(jsonPath("$.hasLineItems").value(true));
        }

        @Test
        @DisplayName("GET /api/v1/forms/{id} - Should return 200 OK when record exists")
        void getFormById_Success() throws Exception {
            // Arrange
            Form domain = Form.builder().id(1).title("Tax Form A").build();
            FormResponse response = new FormResponse(
                    1, "Tax Form A", 2026, LocalDateTime.now(), "BILL123", "PIN99", 10, "DRAFT", 500, false
            );

            when(formService.findForm(1)).thenReturn(Optional.of(domain));
            when(dtoMapper.toResponse(domain)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/v1/forms/1")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.title").value("Tax Form A"));
        }

        @Test
        @DisplayName("GET /api/v1/forms/search - Multi-variable search execution strategy path parsing")
        void searchForms_Success() throws Exception {
            // Arrange
            Form domain = Form.builder().id(1).title("Form A").build();
            FormResponse response = new FormResponse(
                    1, "Form A", 2026, LocalDateTime.now(), "BILL123", "PIN99", 10, "DRAFT", 500, false
            );

            when(formService.findByFilingYearAndBillNumberAndPin(2026, "BILL123", "PIN99"))
                    .thenReturn(List.of(domain));
            when(dtoMapper.toResponse(domain)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(get("/api/v1/forms/search")
                    .param("filingYear", "2026")
                    .param("billNumber", "BILL123")
                    .param("pin", "PIN99")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].billNumber").value("BILL123"));
        }

        @Test
        @DisplayName("GET /api/v1/forms/pageable - Should return 200 OK with pageable envelope structure metrics")
        void getFormsPaged_Success() throws Exception {
            // Arrange
            Form domainForm = Form.builder()
                    .id(1)
                    .title("Form A")
                    .filingYear(2026)
                    .billNumber("BILL123")
                    .pin("PIN99")
                    .statusName("DRAFT")
                    .build();

            FormResponse responseFixture = new FormResponse(
                    1, "Form A", 2026, LocalDateTime.now(), "BILL123", "PIN99", 10, "DRAFT", 500, false
            );

            // 💡 FIX: Use a real PageImpl array structure populated with your domain object
            // This prevents Jackson 3 from crashing on empty or unmodifiable type structures
            java.util.List<Form> domainList = java.util.List.of(domainForm);
            org.springframework.data.domain.Page<Form> realDomainPage = 
                    new org.springframework.data.domain.PageImpl<>(domainList, org.springframework.data.domain.PageRequest.of(0, 10), 1L);

            // Stub the service layer to return this real domain page
            when(formService.findFormEntities(any(org.springframework.data.domain.Pageable.class)))
                    .thenReturn(realDomainPage);

            // 💡 FIX: Stub the dtoMapper with a lenient, type-safe fallback matcher 
            // to catch any object instance passed through the Page stream mapping loop
            org.mockito.Mockito.lenient()
                    .when(dtoMapper.toResponse(any(com.opao.pp_api.features.form.model.Form.class)))
                    .thenReturn(responseFixture);

            // Act & Assert
            mockMvc.perform(get("/api/v1/forms/pageable")
                    .param("page", "0")
                    .param("size", "10")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    // Target items inside your content array payload natively
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].title").value("Form A"))
                    .andExpect(jsonPath("$.content[0].filingYear").value(2026))
                    .andExpect(jsonPath("$.content[0].statusName").value("DRAFT"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }



        @Test
        @DisplayName("DELETE /api/v1/forms/{id} - Should return 204 No Content post database clean deletion")
        void deleteForm_Success() throws Exception {
            // Arrange
            doNothing().when(formService).destroy(1);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/forms/1"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("GET /api/v1/forms/search?title=... - Routes title-only searches")
        void searchForms_ByTitle() throws Exception {
            Form domain = Form.builder().id(5).title("Annual Filing").build();
            FormResponse response = new FormResponse(
                    5, "Annual Filing", 2026, LocalDateTime.now(), "BILL5", "PIN5", 10, "DRAFT", 500, false
            );
            when(formService.findByTitle("Annual Filing")).thenReturn(List.of(domain));
            when(dtoMapper.toResponse(domain)).thenReturn(response);

            mockMvc.perform(get("/api/v1/forms/search")
                    .param("title", "Annual Filing")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(5))
                    .andExpect(jsonPath("$[0].title").value("Annual Filing"));
        }

        @Test
        @DisplayName("GET /api/v1/forms/search - Uses the default collection query when no filters are supplied")
        void searchForms_NoFilters() throws Exception {
            when(formService.findFormEntities()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/forms/search").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));

            verify(formService).findFormEntities();
            verifyNoInteractions(dtoMapper);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 🔴 2. EXCEPTION AND ERROR SCENARIOS
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Exception and Error Contract Diagnostics")
    class ErrorPaths {

        @Test
        @DisplayName("GET /api/v1/forms/{id} - Should return 404 Not Found when schema resource is missing")
        void getFormById_NotFound() throws Exception {
            // Arrange
            when(formService.findForm(99)).thenReturn(Optional.empty());

            // Act & Assert
            mockMvc.perform(get("/api/v1/forms/99")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/v1/forms - Rejects payloads missing required filing relationships")
        void createForm_MissingRequiredFields_ReturnsBadRequest() throws Exception {
            mockMvc.perform(post("/api/v1/forms")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"title\":\"Tax Form\",\"billNumber\":\"BILL123\",\"securityPin\":\"PIN99\"}")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("VALIDATION_FAILURE"))
                    .andExpect(jsonPath("$.status").value(400));

            verifyNoInteractions(formService);
        }

        @Test
        @DisplayName("PUT /api/v1/forms/{id} - Maps a missing form exception to 404")
        void updateForm_MissingForm_ReturnsNotFound() throws Exception {
            FormUpdateRequest request = FormUpdateRequest.builder().id(404).title("Updated").build();
            Form update = Form.builder().id(404).title("Updated").build();
            when(dtoMapper.toDomain(eq(404), any(FormUpdateRequest.class))).thenReturn(update);
            when(formService.edit(eq(404), any(Form.class)))
                    .thenThrow(new jakarta.persistence.EntityNotFoundException("Form record with id 404 no longer exists."));

            mockMvc.perform(put("/api/v1/forms/404")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("Boundary and Empty-Result Scenarios")
    class EdgeCases {

        @Test
        @DisplayName("GET /api/v1/forms/search - Returns an empty array for a valid filter with no matches")
        void searchForms_EmptyResult() throws Exception {
            when(formService.findByFilingYear(1900)).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/forms/search")
                    .param("filingYear", "1900")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));

            verify(formService).findByFilingYear(1900);
            verifyNoInteractions(dtoMapper);
        }

        @Test
        @DisplayName("GET /api/v1/forms/pageable - Preserves a valid empty page response")
        void getFormsPaged_EmptyPage() throws Exception {
            Page<Form> emptyPage = new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(0, 10), 0);
            when(formService.findFormEntities(any(Pageable.class))).thenReturn(emptyPage);

            mockMvc.perform(get("/api/v1/forms/pageable")
                    .param("page", "0")
                    .param("size", "10")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verifyNoInteractions(dtoMapper);
        }
    }
}
