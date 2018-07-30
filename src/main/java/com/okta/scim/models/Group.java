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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database schema for {@link Group}
 */
@Entity
@Table(name = "groups")
public class Group extends BaseModel {
    /**
     * The unique identifier of the object
     * UUID4 following the RFC 7643 requirement
     */
    @Column(length = 36)
    @Id
    public String id;

    /**
     * The display name of the group
     * Non-nullable
     * Max length: 250
     */
    @Column(nullable = false, length = 250)
    public String displayName;

    public Group() {}

    public Group(Map<String, Object> resource){
        this.update(resource);
    }

    /**
     * Updates {@link Group} object from JSON {@link Map}
     * @param resource JSON {@link Map} of {@link Group}
     */
    public void update(Map<String, Object> resource) {
        try{
            this.displayName = resource.get("displayName").toString();
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
        List<String> schemas = new ArrayList<>();
        schemas.add("urn:ietf:params:scim:schemas:core:2.0:Group");
        returnValue.put("schemas", schemas);
        returnValue.put("id", this.id);
        returnValue.put("displayName", this.displayName);

        // Meta information
        Map<String, Object> meta = new HashMap<>();
        meta.put("resourceType", "Group");
        meta.put("meta", ("/scim/v2/Groups/" + this.id));
        returnValue.put("meta", meta);

        return returnValue;
    }
}
