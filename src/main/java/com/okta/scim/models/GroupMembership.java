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
import java.util.HashMap;
import java.util.Map;

/**
 * Database schema for {@link GroupMembership}
 */
@Entity
@Table(name = "groupmemberships")
public class GroupMembership extends BaseModel {
    /**
     * The unique identifier of the object
     * UUID4 following the RFC 7643 requirement
     */
    @Column(length = 36)
    @Id
    public String id;

    @Column(nullable = false, length = 36)
    public String groupId;

    @Column(nullable = false, length = 36)
    public String userId;

    @Column
    public String groupDisplay;

    @Column
    public String userDisplay;

    public GroupMembership() {}

    public GroupMembership(Map<String, Object> resource){
        this.update(resource);
    }

    /**
     * Updates {@link GroupMembership} object from JSON {@link Map}
     * @param resource JSON {@link Map} of {@link GroupMembership}
     */
    public void update(Map<String, Object> resource) {
        try{
            this.userId = resource.get("value").toString();
            this.userDisplay = resource.get("display").toString();
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    /**
     * Formats JSON {@link Map} response with {@link Group} attributes.
     * @return JSON {@link Map} of {@link Group}
     */
    @Override
    public Map toScimResource(){
        Map<String, Object> returnValue = new HashMap<>();

        returnValue.put("value", this.userId);
        returnValue.put("display", this.userDisplay);

        return returnValue;
    }

    public Map toUserScimResource() {
        Map<String, Object> returnValue = new HashMap<>();

        returnValue.put("value", this.groupId);
        returnValue.put("display", this.groupDisplay);

        return returnValue;
    }
}
