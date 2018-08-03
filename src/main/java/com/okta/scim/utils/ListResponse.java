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

package com.okta.scim.utils;

import com.okta.scim.models.BaseModel;
import com.okta.scim.models.User;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Returns an array of SCIM resources into a Query Resource
 */
public class ListResponse<T extends BaseModel> {
    private List<T> list;
    private int startIndex;
    private int count;
    private int totalResults;

    public ListResponse(){
        this.list = new ArrayList<>();
        this.startIndex = 1;
        this.count = 0;
        this.totalResults = 0;
    }

    public ListResponse(List<T> list, Optional<Integer> startIndex,
                        Optional<Integer> count, Optional<Integer> totalResults){
        this.list = list;

        // startIndex.orElse checks for optional values
        this.startIndex = startIndex.orElse(1);
        this.count = count.orElse(0);
        this.totalResults = totalResults.orElse(0);
    }

    /**
     * @return JSON {@link Map} of {@link ListResponse} object
     */
    public HashMap<String, Object> toScimResource(){
        HashMap<String, Object> returnValue = new HashMap<>();

        List<String> schemas = new ArrayList<>();
        schemas.add("urn:ietf:params:scim:api:messages:2.0:ListResponse");
        returnValue.put("schemas", schemas);
        returnValue.put("totalResults", this.totalResults);
        returnValue.put("startIndex", this.startIndex);

        List<Map> resources = this.list.stream().map(T::toScimResource).collect(Collectors.toList());

        if(this.count != 0) {
            returnValue.put("itemsPerPage", this.count);
        }
        returnValue.put("Resources", resources);

        return returnValue;
    }
}
