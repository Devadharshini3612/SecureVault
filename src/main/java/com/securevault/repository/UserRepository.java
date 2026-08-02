package com.securevault.repository;

import com.securevault.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * UserRepository
 *
 * @Repository - marks this interface as a Spring-managed repository bean.
 *
 * By extending JpaRepository<User, Long>, Spring Data JPA automatically
 * provides common database operations at runtime without you writing any SQL:
 *
 *   - save(user)       : INSERT or UPDATE a user row
 *   - findById(id)     : SELECT by primary key
 *   - findAll()        : SELECT all rows
 *   - deleteById(id)   : DELETE by primary key
 *   - count()          : SELECT COUNT(*)
 *   ... and many more
 *
 * JpaRepository<User, Long>
 *   - User : the entity type this repository manages
 *   - Long : the type of the primary key (userId)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Check whether a user with the given email already exists in the database.
     *
     * Spring Data JPA reads the method name "existsByEmail" and automatically
     * generates this SQL query at runtime:
     *
     *   SELECT EXISTS (SELECT 1 FROM users WHERE email = ?)
     *
     * No manual SQL or @Query annotation needed.
     *
     * @param email the email address to check
     * @return true if a user with this email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find a user by their email address.
     *
     * Spring Data JPA reads the method name "findByEmail" and automatically
     * generates this SQL query at runtime:
     *
     *   SELECT * FROM users WHERE email = ?
     *
     * Returns Optional<User> which will be empty if no user is found.
     *
     * @param email the email address to search for
     * @return Optional containing the user if found, empty Optional otherwise
     */
    java.util.Optional<User> findByEmail(String email);
}
