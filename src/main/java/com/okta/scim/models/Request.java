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

package com.okta.scim.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Database schema for {@link Request}
 */
@Entity
@Table(name = "requests")
public class Request extends BaseModel {
    /**
     * The unique identifier of the transaction
     */
    @Column(length = 36)
    @Id
    public String id;

    /**
     * Sets the {@link Request} ID
     * @param id The ID
     * @return The {@link Request} instance
     */
    public Request setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Generate a new UUID for the transaction
     * @return The {@link Request} instance
     */
    public Request generateId() {
        this.id = UUID.randomUUID().toString();
        return this;
    }

    /**
     * The timestamp of the transaction, in UTC
     * Non-nullable
     */
    @Column(nullable = false)
    public String timeStamp;

    /**
     * Sets the timestamp of the transaction to the current date-time, UTC
     * @return The {@link Request} instance
     */
    public Request setTimestamp() {
        this.timeStamp = LocalDateTime.now(Clock.systemUTC()).toString().substring(0, 23) + "Z";
        return this;
    }

    /**
     * Set the timestamp of the transaction
     * @param timeStamp The timestamp
     * @return The {@link Request} instance
     */
    public Request setTimestamp(String timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    /**
     * The method of the request
     * Max length: 20
     */
    @Column(length = 20)
    public String method;

    /**
     * Set the request method
     * @param method The method
     * @return The {@link Request} instance
     */
    public Request setMethod(String method) {
        this.method = method;
        return this;
    }

    /**
     * The endpoint of the request
     * Max length: 250
     */
    @Column(length = 250)
    public String endpoint;

    /**
     * Set the endpoint of the request
     * @param endpoint The endpoint
     * @return The {@link Request} instance
     */
    public Request setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public Request() {}
}
