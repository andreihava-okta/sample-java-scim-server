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

package com.okta.scim.interceptors;

import com.okta.scim.database.RequestDatabase;
import com.okta.scim.models.Request;
import com.okta.scim.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.SynchronousQueue;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
    public static Queue<Request> requests = new LinkedList<>();

    RequestDatabase db;

    @Autowired
    public LoggingInterceptor(RequestDatabase db) {
        this.db = db;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (request.getRequestURI().startsWith("/scim/v2/")) {
            Request req = new Request();
            req.id = UUID.randomUUID().toString();
            req.timeStamp = LocalDateTime.now(Clock.systemUTC()).toString().substring(0, 23) + "Z";
            req.method = request.getMethod();
            req.endpoint = request.getRequestURI();

            if (!req.method.equals("GET")) {
                try {
                    req.body = StringUtil.readerToString(request.getReader());
                } catch (IOException e) {
                    System.out.println(e);
                }
            }

            db.save(req);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            Request req = requests.remove();
            req.method = request.getMethod();
            req.endpoint = request.getRequestURI();
            req.httpCode = response.getStatus();

            db.save(req);
        } catch(Exception e) {
            // System.out.println(e);
        }
    }
}