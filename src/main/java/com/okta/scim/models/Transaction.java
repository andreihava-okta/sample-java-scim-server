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
import java.util.Map;
import java.util.UUID;

/**
 * Database schema for {@link Transaction}
 */
@Entity
@Table(name = "transactions")
public class Transaction extends BaseModel {
    /**
     * The unique identifier of the transaction
     */
    @Column(length = 36)
    @Id
    public String id;

    /**
     * Sets the {@link Transaction} ID
     * @param id The ID
     * @return The {@link Transaction} instance
     */
    public Transaction setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Generate a new UUID for the transaction
     * @return The {@link Transaction} instance
     */
    public Transaction generateId() {
        this.id = UUID.randomUUID().toString();
        return this;
    }

    /**
     * The original {@link Request} ID
     * Max length: 36
     */
    @Column(length = 36)
    public String requestId;

    /**
     * Set the transaction's original {@link Request} ID
     * @param requestId The {@link Request} ID
     * @return The {@link Transaction} instance
     */
    public Transaction setRequestId(String requestId) {
        this.requestId = requestId;
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
     * @return The {@link Transaction} instance
     */
    public Transaction setTimestamp() {
        this.timeStamp = LocalDateTime.now(Clock.systemUTC()).toString().substring(0, 23) + "Z";
        return this;
    }

    /**
     * Set the timestamp of the transaction
     * @param timeStamp The timestamp
     * @return The {@link Transaction} instance
     */
    public Transaction setTimestamp(String timeStamp) {
        this.timeStamp = timeStamp;
        return this;
    }

    /**
     * If response is True, shows the returned HTTP Status Code
     * Default: 200
     */
    @Column
    public int httpCode = 200;

    /**
     * Set the HTTP Status Code of the response
     * @param httpCode The status code
     * @return The {@link Transaction} instance
     */
    public Transaction setHttpCode(int httpCode) {
        this.httpCode = httpCode;
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
     * @return The {@link Transaction} instance
     */
    public Transaction setMethod(String method) {
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
     * @return The {@link Transaction} instance
     */
    public Transaction setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * The body of the request
     * Max length: 1000
     */
    @Column(length = 1000)
    public String requestBody;

    /**
     * Set the request body
     * @param requestBody The request body
     * @return The {@link Transaction} instance
     */
    public Transaction setRequestBody(String requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    /**
     * The body of the response
     * Max length: 1000
     */
    @Column(length = 1000)
    public String responseBody;

    /**
     * Set the response body
     * @param responseBody The response body
     * @return The {@link Transaction} instance
     */
    public Transaction setResponseBody(String responseBody) {
        this.responseBody = responseBody;
        return this;
    }

    /**
     * The Java method handling the request
     * Max length: 300
     */
    @Column(length = 300)
    public String javaMethod;

    /**
     * Set the Java method handling the request
     * @param javaMethod The method
     * @return The {@link Transaction} instance
     */
    public Transaction setJavaMethod(String javaMethod) {
        this.javaMethod = javaMethod;
        return this;
    }

    public Transaction() {}

    @Override
    public Map toScimResource() {
        return null;
    }
}
