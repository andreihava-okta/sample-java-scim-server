/** Copyright © 2018, Okta, Inc.
 *
 *  Licensed under the MIT license, the "License";
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     https://opensource.org/licenses/MIT
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.okta.scim.database;

import com.okta.scim.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface for the {@link User} database
 */
@Repository
public interface UserDatabase extends JpaRepository<User, Long> {
    /**
     * Gets a single resource from the database, matching the given ID
     * @param id The ID to search for
     * @return The instance of {@link User} found
     */
    List<User> findById(String id);

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
