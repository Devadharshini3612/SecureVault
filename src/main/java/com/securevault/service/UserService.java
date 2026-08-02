package com.securevault.service;

import com.securevault.dto.RegisterRequest;
import com.securevault.dto.LoginRequest;
import com.securevault.dto.UserResponse;
import com.securevault.entity.User;
import com.securevault.exception.DuplicateEmailException;
import com.securevault.exception.InvalidCredentialsException;
import com.securevault.exception.UserNotFoundException;
import com.securevault.mapper.DtoEntityMapper;
import com.securevault.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserService - Enhanced with DTO Support
 *
 * Updated service layer that works seamlessly with DTOs while maintaining
 * backward compatibility for existing entity-based methods.
 * 
 * Key improvements:
 * - DTO-focused methods using DtoEntityMapper for clean conversions
 * - Custom exceptions instead of string return codes
 * - Transaction management for data consistency
 * - Comprehensive error handling and validation
 * - Separation of concerns between API layer (DTOs) and persistence layer (Entities)
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DtoEntityMapper mapper;

    /**
     * BCryptPasswordEncoder is used to hash passwords securely.
     * 
     * BCrypt features:
     * - Automatically generates a unique salt for each password
     * - Two users with the same password will have different hashes
     * - The hash includes the algorithm version, cost factor, salt, and hash
     * - Example: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
     *   $2a     = BCrypt algorithm version
     *   $10     = Cost factor (2^10 = 1024 rounds)
     *   Next 22 chars = Salt (randomly generated)
     *   Last 31 chars = Actual password hash
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ========================================
    // DTO-FOCUSED METHODS (New Enhanced API)
    // ========================================

    /**
     * Register a new user using DTO pattern
     * 
     * @param request RegisterRequest DTO with validation
     * @return UserResponse DTO with created user information
     * @throws DuplicateEmailException if email already exists
     */
    @Transactional
    public UserResponse registerUserWithDTO(RegisterRequest request) {
        // Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw DuplicateEmailException.emailAlreadyExists(request.getEmail());
        }

        // Convert DTO to Entity using mapper
        User userEntity = mapper.toUserEntity(request);
        
        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(userEntity.getPasswordHash());
        userEntity.setPasswordHash(hashedPassword);

        // Save to database
        User savedUser = userRepository.save(userEntity);

        // Convert Entity back to DTO for response
        return mapper.toUserResponse(savedUser);
    }

    /**
     * Authenticate user using DTO pattern
     * 
     * @param request LoginRequest DTO with validation
     * @return UserResponse DTO with authenticated user information
     * @throws UserNotFoundException if user doesn't exist
     * @throws InvalidCredentialsException if password is incorrect
     */
    public UserResponse authenticateUserWithDTO(LoginRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> UserNotFoundException.withEmail(request.getEmail()));

        // Verify password
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        if (!passwordMatches) {
            throw InvalidCredentialsException.invalidPassword();
        }

        // Convert Entity to DTO for response
        return mapper.toUserResponse(user);
    }

    /**
     * Find user by email and return as DTO
     * 
     * @param email User's email address
     * @return UserResponse DTO
     * @throws UserNotFoundException if user doesn't exist
     */
    public UserResponse findUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.withEmail(email));
        
        return mapper.toUserResponse(user);
    }

    // ========================================
    // LEGACY METHODS (Maintained for backward compatibility)
    // ========================================

    /**
     * Registers a new user with BCrypt password hashing.
     * 
     * @deprecated Use registerUserWithDTO(RegisterRequest) for new implementations
     * @param user the User entity (contains plaintext password)
     * @return "EMAIL_EXISTS" if duplicate, "SUCCESS" if registered successfully
     */
    @Deprecated
    public String registerUser(User user) {
        // Step 1: Check for duplicate email using the custom repository method
        boolean emailAlreadyExists = userRepository.existsByEmail(user.getEmail());

        // Step 2: If email is taken, signal conflict to the controller
        if (emailAlreadyExists) {
            return "EMAIL_EXISTS";
        }

        // Step 3: Hash the plaintext password using BCrypt before saving
        String hashedPassword = passwordEncoder.encode(user.getPasswordHash());
        user.setPasswordHash(hashedPassword);

        // Step 4: Save the user to PostgreSQL with the hashed password
        userRepository.save(user);

        return "SUCCESS";
    }

    /**
     * Authenticates a user by verifying their email and password.
     * 
     * @deprecated Use authenticateUserWithDTO(LoginRequest) for new implementations
     * @param email the user's email address
     * @param plainPassword the plaintext password from the login request
     * @return "SUCCESS", "USER_NOT_FOUND", or "INVALID_PASSWORD"
     */
    @Deprecated
    public String loginUser(String email, String plainPassword) {
        // Step 1: Find the user by email
        var userOptional = userRepository.findByEmail(email);

        // Step 2: Check if user exists
        if (userOptional.isEmpty()) {
            return "USER_NOT_FOUND";
        }

        // Step 3: Get the user and their stored password hash
        User user = userOptional.get();
        String storedHash = user.getPasswordHash();

        // Step 4: Verify the plaintext password against the BCrypt hash
        boolean passwordMatches = passwordEncoder.matches(plainPassword, storedHash);

        // Step 5: Return result based on password verification
        if (passwordMatches) {
            return "SUCCESS";
        } else {
            return "INVALID_PASSWORD";
        }
    }

    /**
     * Finds a user by email address
     * 
     * @deprecated Use findUserByEmail(String) for DTO response, or keep this for internal use
     * @param email the user's email address
     * @return User entity if found
     * @throws UserNotFoundException if user not found
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> UserNotFoundException.withEmail(email));
    }
}
