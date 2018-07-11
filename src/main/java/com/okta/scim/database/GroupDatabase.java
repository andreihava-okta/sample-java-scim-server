package com.okta.scim.database;

import com.okta.scim.models.Group;
import com.okta.scim.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Interface for the {@link Group} {@link Database}
 */
@Repository
public interface GroupDatabase extends Database<Group> {
    /**
     * Searches and returns all instances of {@link Group} that match a given display name
     * @param name The display name to search
     * @param pagable A pageable object, usually a {@link org.springframework.data.domain.PageRequest}
     * @return A {@link Page} object with the found {@link Group} instances
     */
    @Query("SELECT g FROM Group g WHERE g.displayName = :name")
    Page<User> findByDisplayname(@Param("name") String name, Pageable pagable);
}
