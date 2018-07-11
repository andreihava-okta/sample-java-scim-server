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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Base interface for all {@link Database} implementations
 * @param <T> The model to use for the database
 */
@Repository
public interface Database<T> extends JpaRepository<T, Long> {
    /**
     * Gets a single resource from the database, matching the given ID
     * @param id The ID to search for
     * @return The instance of {@link T} found
     */
    List<T> findById(String id);
}

@EnableJpaRepositories
class Config {}
