package com.okta.scim.database;

import com.okta.scim.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Interface for the {@link User} {@link Database}
 */
@Repository
public interface UserDatabase extends Database<User> {
    /**
     * Searches and returns all instances of {@link User} that match a given username
     * @param name The username to search
     * @param pagable A pageable object, usually a {@link org.springframework.data.domain.PageRequest}
     * @return A {@link Page} object with the found {@link User} instances
     */
    @Query("SELECT u FROM User u WHERE u.userName = :name")
    Page<User> findByUsername(@Param("name") String name, Pageable pagable);

    /**
     * Searches and returns all instances of {@link User} that are active
     * @param value True for active, False for inactive
     * @param pagable A pageable object, usually a {@link org.springframework.data.domain.PageRequest}
     * @return A {@link Page} object with the found {@link User} instances
     */
    @Query("SELECT u FROM User u WHERE u.active = :value")
    Page<User> findByActive(@Param("value") Boolean value, Pageable pagable);

    /**
     * Searches and returns all instances of {@link User} that match a given last name
     * @param name The last name to search
     * @param pagable A pageable object, usually a {@link org.springframework.data.domain.PageRequest}
     * @return A {@link Page} object with the found {@link User} instances
     */
    @Query("SELECT u FROM User u WHERE u.familyName = :name")
    Page<User> findByFamilyName(@Param("name") String name, Pageable pagable);

    /**
     * Searches and returns all instances of {@link User} that match a given first name
     * @param name The first name to search
     * @param pagable A pageable object, usually a {@link org.springframework.data.domain.PageRequest}
     * @return A {@link Page} object with the found {@link User} instances
     */
    @Query("SELECT u FROM User u WHERE u.givenName = :name")
    Page<User> findByGivenName(@Param("name") String name, Pageable pagable);
}
