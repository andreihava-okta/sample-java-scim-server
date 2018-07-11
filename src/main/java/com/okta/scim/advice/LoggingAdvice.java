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

package com.okta.scim.advice;

import com.google.gson.Gson;
import com.okta.scim.database.RequestDatabase;
import com.okta.scim.interceptors.LoggingInterceptor;
import com.okta.scim.models.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@ControllerAdvice
public class LoggingAdvice implements ResponseBodyAdvice<HashMap> {
    RequestDatabase db;

    @Autowired
    public LoggingAdvice(RequestDatabase db) {
        this.db = db;
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public HashMap beforeBodyWrite(HashMap body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest request, ServerHttpResponse response) {
        Request req = new Request();
        req.id = UUID.randomUUID().toString();
        req.timeStamp = LocalDateTime.now(Clock.systemUTC()).toString().substring(0, 23) + "Z";
        req.response = true;
        req.body = new Gson().toJson(body);

        LoggingInterceptor.requests.add(req);

        return body;
    }
}