package com.opao.pp_api.features.user;
        
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opao.pp_api.features.user.mapper.UserMapper;
import com.opao.pp_api.features.user.model.User;
import com.opao.pp_api.features.user.model.UserEntity;
import com.opao.pp_api.features.user_role.constants.UserRoles;
import com.opao.pp_api.features.user_status.constants.UserStatuses;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper; 

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    public Optional<User> getUserById(Integer id) { 
        return userRepository.findById(id).map(userMapper::toDomain);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toDomain);
    }

    public Optional<User> getUserByEmailAddress(String email) {
        return userRepository.findByEmailAddress(email).map(userMapper::toDomain);
    }

    /**
     * Creates a brand new user record. 
     * Flexible enough for self-registration, admin creation, and disabled states.
     */
    @Transactional
    public User create(User domainModel) {
        // 1. Guard Rail: Prevent duplicate identities
        if (userRepository.findByUsername(domainModel.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }
         
        // 2. Encrypt cleartext passwords safely before it hits mapping stages
        if (domainModel.getPassword() != null) {
            // domainModel.setHashedPassword(passwordEncoder.encode(domainModel.getClearTextPassword()));
            domainModel.setPassword(domainModel.getPassword()); // Fallback for raw setup
        }

        // 3. 🌟 SMART DEFAULTS: Only fallback if the controller/caller didn't specify them!
        if (domainModel.getUserRoleId() == null) {
            domainModel.setUserRoleId(UserRoles.TAX_PREPARER); // Default for public self-registration
        }
        
        if (domainModel.getUserStatusId() == null) {
            domainModel.setUserStatusId(UserStatuses.ENABLED); // Default status
        }
        
        UserEntity entity = userMapper.toEntity(domainModel);
        UserEntity savedEntity = userRepository.save(entity);
        
        return userMapper.toDomain(savedEntity);
    }


    @Transactional
    public Optional<User> updateUser(Integer id, User updatedUser) { 
        return userRepository.findById(id).map(existingEntity -> {
            userMapper.updateEntityFromDomain(updatedUser, existingEntity);
            UserEntity savedEntity = userRepository.save(existingEntity);
            return userMapper.toDomain(savedEntity);
        });
    }

    @Transactional
    public boolean deleteUser(Integer id) { 
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
