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

import com.okta.scim.models.GroupMembership;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface for the {@link GroupMembership} database
 */
@Repository
public interface GroupMembershipDatabase extends JpaRepository<GroupMembership, Long> {
    /**
     * Gets a single resource from the database, matching the given ID
     * @param id The ID to search for
     * @return The instance of {@link GroupMembership} found
     */
    List<GroupMembership> findById(String id);

    /**
     * Searches and returns all instances of {@link GroupMembership} that match a given group ID
     * @param groupId The group ID to search
     * @param pagable A pageable object, usually a {@link org.springframework.data.domain.PageRequest}
     * @return A {@link Page} object with the found {@link GroupMembership} instances
     */
    @Query("SELECT gm FROM GroupMembership gm WHERE gm.groupId = :groupId")
    Page<GroupMembership> findByGroupId(@Param("groupId") String groupId, Pageable pagable);

    @Query("SELECT gm FROM GroupMembership gm WHERe gm.userId = :userId")
    Page<GroupMembership> findByUserId(@Param("userId") String userId, Pageable pageable);

    /**
     * Searches and returns all instances of {@link GroupMembership} that match a given group ID and userId
     * @param groupId The group ID to search
     * @param userId The userId to search
     * @param pagable A pageable object, usually a {@link org.springframework.data.domain.PageRequest}
     * @return A {@link Page} object with the found {@link GroupMembership} instances
     */
    @Query("SELECT gm FROM GroupMembership gm WHERE gm.groupId = :groupId AND gm.userId = :userId")
    Page<GroupMembership> findByGroupIdAndUserId(@Param("groupId") String groupId, @Param("userId") String userId, Pageable pagable);
}
