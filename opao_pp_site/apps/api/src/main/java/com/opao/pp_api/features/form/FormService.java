package com.opao.pp_api.features.form;

import com.opao.pp_api.features.form.mapper.FormMapper;
import com.opao.pp_api.features.form.model.Form;
import com.opao.pp_api.features.form.model.FormEntity;
import com.opao.pp_api.features.form_status.model.FormStatusEntity;
import com.opao.pp_api.features.form_status.FormStatusRepository; // Ensure these repositories exist
import com.opao.pp_api.features.form_type.FormTypeRepository;
import com.opao.pp_api.features.user.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FormService {

    private final FormRepository formRepository;
    private final FormMapper formMapper;
    
    // 🆕 Inject standard repositories to handle lightweight proxy lookups
    private final FormTypeRepository formTypeRepository;
    private final FormStatusRepository formStatusRepository;
    private final UserRepository userRepository;

    /**
     * Creates a brand-new Form record.
     * Manages explicit proxy linkages for structural parents using repository references.
     */
    @Transactional
    public Form create(Form domain) {
        FormEntity entity = formMapper.toEntity(domain);
        resolveRelationships(domain, entity);
        
        FormEntity savedEntity = formRepository.save(entity);
        return formMapper.toDomain(savedEntity);
    }

    /**
     * Safely updates incremental updates from your domain into an existing database row context.
     */
    @Transactional
    public Form edit(Integer id, Form domain) {
        FormEntity existingEntity = formRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Form record with id " + id + " no longer exists."));
        
        formMapper.updateEntityFromDomain(domain, existingEntity);
        resolveRelationships(domain, existingEntity);
        
        FormEntity updatedEntity = formRepository.save(existingEntity);
        return formMapper.toDomain(updatedEntity);
    }

    /**
     * Destroys/Removes a target form record from the ecosystem.
     */
    @Transactional
    public void destroy(Integer id) {
        if (!formRepository.existsById(id)) {
            throw new EntityNotFoundException("Form record with id " + id + " no longer exists.");
        }
        formRepository.deleteById(id);
    }

    /**
     * Finds a single clean business domain representation by ID.
     */
    public Optional<Form> findForm(Integer id) {
        return formRepository.findById(id)
            .map(formMapper::toDomain);
    }

    /**
     * Retrieves all form entities transformed to the standalone business model mapping.
     */
    public List<Form> findFormEntities() {
        return formRepository.findAll().stream()
            .map(formMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * Paged variant of finding form models natively over the framework abstractions.
     */
    public Page<Form> findFormEntities(Pageable pageable) {
        return formRepository.findAll(pageable)
            .map(formMapper::toDomain);
    }

    // --- Custom Repository Query Mappings ---

    public List<Form> findByTitle(String title) {
        return formRepository.findByTitle(title).stream()
            .map(formMapper::toDomain)
            .collect(Collectors.toList());
    }

    public List<Form> findByFilingYear(Integer filingYear) {
        return formRepository.findByFilingYear(filingYear).stream()
            .map(formMapper::toDomain)
            .collect(Collectors.toList());
    }

    public List<Form> findByBillNumber(String billNumber) {
        return formRepository.findByBillNumber(billNumber).stream()
            .map(formMapper::toDomain)
            .collect(Collectors.toList());
    }

    public List<Form> findByBillNumberAndPin(String billNumber, String pin) {
        return formRepository.findByBillNumberAndPin(billNumber, pin).stream()
            .map(formMapper::toDomain)
            .collect(Collectors.toList());
    }

    public List<Form> findByBillNumberAndFilingYear(String billNumber, Integer filingYear) {
        return formRepository.findByBillNumberAndFilingYear(billNumber, filingYear).stream()
            .map(formMapper::toDomain)
            .collect(Collectors.toList());
    }

    public List<Form> findByFilingYearAndBillNumberAndPin(Integer filingYear, String billNumber, String pin) {
        return formRepository.findByFilingYearAndBillNumberAndPin(filingYear, billNumber, pin).stream()
            .map(formMapper::toDomain)
            .collect(Collectors.toList());
    }

    public List<Form> findByBillNumberAndPinAndStatusName(String billNumber, String pin, String statusName) {
        return formRepository.findByBillNumberAndPinAndStatusName(billNumber, pin, statusName).stream()
            .map(formMapper::toDomain)
            .collect(Collectors.toList());
    }

    /**
     * Helper technique that safely hydrates parent references from flat domain identifiers without deep fetches.
     */
    private void resolveRelationships(Form domain, FormEntity entity) {
        if (domain.getFormTypeId() != null) {
            // Natively fetches an un-hydrated proxy record reference safely!
            entity.setFormType(formTypeRepository.getReferenceById(domain.getFormTypeId()));
        }
        
        if (domain.getStatusName() != null) {
            FormStatusEntity status = formStatusRepository.findByName(domain.getStatusName())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Form status with name " + domain.getStatusName() + " does not exist."));
            entity.setStatus(status);
        }

        if (domain.getUserId() != null) {
            entity.setUserId(userRepository.getReferenceById(domain.getUserId()));
        }
    }
}
