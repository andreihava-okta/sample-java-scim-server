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

package com.okta.scim.controllers;

import com.okta.scim.database.UserDatabase;
import com.okta.scim.models.User;
import com.okta.scim.utils.ListResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  URL route (root)/scim/v2/Users
 */
@Controller
@RequestMapping("/scim/v2/Users")
public class UsersController {
    UserDatabase db;

    @Autowired
    public UsersController(UserDatabase db) {
      this.db = db;
    }

    /**
     * Support pagination and filtering by username
     * @param params Payload from HTTP request
     * @return JSON {@link Map} {@link ListResponse}
     */
    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Map usersGet(@RequestParam Map<String, String> params) {
        Page<User> users;

        // If not given count, default to 100
        int count = (params.get("count") != null) ? Integer.parseInt(params.get("count")) : 100;

        // If not given startIndex, default to 1
        int startIndex = (params.get("startIndex") != null) ? Integer.parseInt(params.get("startIndex")) : 1;

        if(startIndex < 1){
            startIndex = 1;
        }
        startIndex -=1;

        PageRequest pageRequest = new PageRequest(startIndex, count);

        String filter = params.get("filter");
        if (filter != null && filter.contains("eq")) {
            String regex = "(\\w+) eq \"([^\"]*)\"";
            Pattern response = Pattern.compile(regex);

            Matcher match = response.matcher(filter);
            Boolean found = match.find();
            if (found) {
                String searchKeyName = match.group(1);
                String searchValue = match.group(2);
                switch (searchKeyName) {
                    case "active":
                        users = db.findByActive(Boolean.valueOf(searchValue), pageRequest);
                        break;
                    case "faimlyName":
                        users = db.findByFamilyName(searchValue, pageRequest);
                        break;
                    case "givenName":
                        users = db.findByGivenName(searchValue, pageRequest);
                        break;
                    default:
                        // Defaults to username lookup
                        users = db.findByUsername(searchValue, pageRequest);
                        break;
                }
            } else {
                users = db.findAll(pageRequest);
            }
        } else {
            users = db.findAll(pageRequest);
        }

        List<User> foundUsers = users.getContent();
        int totalResults = foundUsers.size();

        // Convert optional values into Optionals for ListResponse Constructor
        ListResponse returnValue = new ListResponse(foundUsers, Optional.of(startIndex),
                                        Optional.of(count), Optional.of(totalResults));
        return returnValue.toScimResource();
    }

    /**
     * Creates new {@link User} with given attributes
     * @param params JSON {@link Map} of {@link User} attributes
     * @param response HTTP response
     * @return JSON {@link Map} of {@link User}
     */
    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Map usersPost(@RequestBody Map<String, Object> params, HttpServletResponse response){

        User newUser = new User(params);
        newUser.id = UUID.randomUUID().toString();
        db.save(newUser);
        response.setStatus(201);
        return newUser.toScimResource();
    }
}
