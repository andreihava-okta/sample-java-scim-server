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
import java.util.Map;

/**
 * Database schema for {@link Request}
 */
@Entity
@Table(name = "requests")
public class Request extends BaseModel {
    /**
     * The unique identifier of the user
     * UUID4 following the RFC 7643 requirement
     */
    @Column(length = 36)
    @Id
    public String id;

    /**
     * The timestamp of the request, in UTC
     * Non-nullable
     */
    @Column(nullable = false)
    public String timeStamp;

    /**
     * True for response FROM SCIM server, False for request TO SCIM server
     * Default: false
     */
    @Column(columnDefinition = "boolean default false")
    public Boolean response = false;

    /**
     * If response is True, shows the returned HTTP Status Code
     * Default: 200
     */
    @Column
    public int httpCode = 200;

    /**
     * The method of the request
     * Max length: 20
     */
    @Column(length = 20)
    public String method;

    /**
     * The endpoint of the request
     * Max length: 250
     */
    @Column(length = 250)
    public String endpoint;

    /**
     * The body of the request
     * Max length: 250
     */
    @Column(length = 1000)
    public String body;

    public Request() {}

    public Request(Map<String, Object> resource) {
        this.update(resource);
    }

    /**
     * Updates {@link Request} object from JSON {@link Map}
     * @param resource JSON {@link Map} of {@link Request}
     */
    public void update(Map<String, Object> resource) {
        try {
            this.id = resource.get("id").toString();
            this.timeStamp = resource.get("timeStamp").toString();
            this.method = resource.get("method").toString();
            this.endpoint = resource.get("endpoint").toString();
            this.body = resource.get("body").toString();
        } catch(Exception e) {
            System.out.println(e);
        }
    }
}
