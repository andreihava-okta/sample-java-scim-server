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
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface Database extends JpaRepository<User, Long> {
    List<User> findById(String id);

    // Query by username
    @Query("SELECT u FROM User u WHERE u.userName = :name")
    Page<User> findByUsername(@Param("name") String name, Pageable pagable);

    // Query by active
    @Query("SELECT u FROM User u WHERE u.active = :value")
    Page<User> findByActive(@Param("value") Boolean value, Pageable pagable);

    // Query by familyName
    @Query("SELECT u FROM User u WHERE u.familyName = :name")
    Page<User> findByFamilyName(@Param("name") String name, Pageable pagable);

    // Query by givenName
    @Query("SELECT u FROM User u WHERE u.givenName = :name")
    Page<User> findByGivenName(@Param("name") String name, Pageable pagable);

}

@EnableJpaRepositories
class Config {}
