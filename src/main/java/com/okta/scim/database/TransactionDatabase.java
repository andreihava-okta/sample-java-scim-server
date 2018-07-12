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

import com.okta.scim.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Interface for the {@link Transaction} database
 */
@Repository
public interface TransactionDatabase extends JpaRepository<Transaction, Long> {
    /**
     * Gets a single resource from the database, matching the given ID
     * @param id The ID to search for
     * @return The instance of {@link Transaction} found
     */
    List<Transaction> findById(String id);
}
