/** Copyright © 2016, Okta, Inc.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.okta.scim;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Entity;
import javax.persistence.Table;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Database schema for {@link User} object
 */
@Entity
@Table(name="users")
public class User {
    // Unique ID as a UUID4 following the RFC 7643 requirement
    @Column(length=36)
    @Id
    public String id;

    @Column(columnDefinition="boolean default false")
    public Boolean active = false;

    @Column(unique=true, nullable=false, length=250)
    public String userName;

    @Column(length=250)
    public String familyName;

    @Column(length=250)
    public String middleName;

    @Column(length=250)
    public String givenName;

    User() {}

    User(Map<String, Object> resource){
        this.update(resource);
    }

    /**
     * Updates {@link User} object from JSON {@link Map}
     * @param resource JSON {@link Map} of {@link User}
     */
    void update(Map<String, Object> resource) {
        try{
            Map<String, Object> names = (Map<String, Object>)resource.get("name");
            for(String subName : names.keySet()){
                switch (subName) {
                    case "givenName":
                        this.givenName = names.get(subName).toString();
                        break;
                    case "familyName":
                        this.familyName = names.get(subName).toString();
                        break;
                    case "middleName":
                        this.middleName = names.get(subName).toString();
                        break;
                    default:
                        break;
                }
            }
          this.userName = resource.get("userName").toString();
          this.active = (Boolean)resource.get("active");
        } catch(Exception e) {
             System.out.println(e);
        }
    }

    /**
     * Formats JSON {@link Map} response with {@link User} attributes.
     *
     * @return JSON {@link Map} of {@link User}
     */
    Map toScimResource(){
        Map<String, Object> returnValue = new HashMap<>();
        List<String> schemas = new ArrayList<>();
        schemas.add("urn:ietf:params:scim:schemas:core:2.0:User");
        returnValue.put("schemas", schemas);
        returnValue.put("id", this.id);
        returnValue.put("active", this.active);
        returnValue.put("userName", this.userName);

        // Names
        Map<String, Object> names = new HashMap<>();
        names.put("familyName", this.familyName);
        names.put("givenName", this.givenName);
        names.put("middleName", this.middleName);
        returnValue.put("name", names);

        // Meta information
        Map<String, Object> meta = new HashMap<>();
        meta.put("resourceType", "User");
        meta.put("meta", ("/scim/v2/Users/" + this.userName));
        returnValue.put("meta", meta);

        return returnValue;
    }
}
