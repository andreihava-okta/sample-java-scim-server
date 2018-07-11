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

import com.okta.scim.models.Request;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface for the {@link Request} database
 */
@Repository
public interface RequestDatabase extends JpaRepository<Request, Long> {
    /**
     * Gets a single resource from the database, matching the given ID
     * @param id The ID to search for
     * @return The instance of {@link Request} found
     */
    List<Request> findById(String id);

    /**
     * Searches and returns all instances of {@link Request} that match the value condition of is response
     * @param value The value of the condition
     * @param pagable A pageable object, usually a {@link org.springframework.data.domain.PageRequest}
     * @return A {@link Page} object with the found {@link Request} instances
     */
    @Query("SELECT r FROM Request r WHERE r.response = :value")
    Page<Request> findByResponse(@Param("value") Boolean value, Pageable pagable);
}
