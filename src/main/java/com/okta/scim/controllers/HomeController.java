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

import com.okta.scim.database.*;
import com.okta.scim.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 *  URL route (root)/ to userDisplay home page
 */
@Controller
@RequestMapping("/")
public class HomeController {
    private UserDatabase uDb;
    private GroupDatabase gDb;
    private TransactionDatabase tDb;
    private RequestDatabase rDb;
    private GroupMembershipDatabase gmDb;

    @Autowired
    public HomeController(
            UserDatabase uDb,
            GroupDatabase gDb,
            TransactionDatabase tDb,
            RequestDatabase rDb,
            GroupMembershipDatabase gmDb) {
        this.uDb = uDb;
        this.gDb = gDb;
        this.tDb = tDb;
        this.rDb = rDb;
        this.gmDb = gmDb;
    }

    /**
     * Outputs all active users, groups and transaction logs to web view
     * @param model UI Model
     * @return HTML page to render by name
     */
    @RequestMapping(method = RequestMethod.GET)
    public String home(ModelMap model) {
        List<User> users = uDb.findAll();
        List<Group> groups = gDb.findAll();
        List<Transaction> transactions = tDb.findAll();
        List<Request> requests = rDb.findAll();
        List<GroupMembership> groupMemberships = gmDb.findAll();
        model.addAttribute("users", users);
        model.addAttribute("groups", groups);
        model.addAttribute("transactions", transactions);
        model.addAttribute("requests", requests);
        model.addAttribute("groupMemberships", groupMemberships);
        return "home";
    }
}
