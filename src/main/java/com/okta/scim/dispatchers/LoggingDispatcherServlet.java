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

package com.okta.scim.dispatchers;

import com.okta.scim.database.RequestDatabase;
import com.okta.scim.models.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class LoggingDispatcherServlet extends DispatcherServlet {
    @Autowired
    RequestDatabase db;

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (!(request instanceof ContentCachingRequestWrapper)) {
            request = new ContentCachingRequestWrapper(request);
        }

        if (!(response instanceof ContentCachingResponseWrapper)) {
            response = new ContentCachingResponseWrapper(response);
        }

        HandlerExecutionChain handler = getHandler(request);

        try {
            super.doDispatch(request, response);
        } finally {
            if (request.getRequestURI().startsWith("/scim/v2/")) {
                log(request, response, handler);
            }
            updateResponse(response);
        }
    }

    private void log(HttpServletRequest requestToCache, HttpServletResponse responseToCache, HandlerExecutionChain handler) {
        Request req = new Request()
                .generateId()
                .setTimestamp()
                .setMethod(requestToCache.getMethod())
                .setHttpCode(responseToCache.getStatus())
                .setEndpoint(requestToCache.getRequestURI())
                .setClientIp(requestToCache.getRemoteAddr())
                .setJavaMethod(handler.toString())
                .setRequestBody(getRequestPayload(requestToCache))
                .setResponseBody(getResponsePayload(responseToCache));

        db.save(req);
    }

    @SuppressWarnings("Duplicates")
    private String getRequestPayload(HttpServletRequest request) {
        ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);

        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();

            if (buf.length > 0) {
                int length = Math.min(buf.length, 5120);

                try {
                    return new String(buf, 0, length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    System.out.println(e);
                }
            }
        }

        return "[unknown]";
    }

    @SuppressWarnings("Duplicates")
    private String getResponsePayload(HttpServletResponse response) {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);

        if (wrapper != null) {
            byte[] buf = wrapper.getContentAsByteArray();

            if (buf.length > 0) {
                int length = Math.min(buf.length, 5120);

                try {
                    return new String(buf, 0, length, wrapper.getCharacterEncoding());
                } catch (UnsupportedEncodingException e) {
                    System.out.println(e);
                }
            }
        }

        return "[unknown]";
    }

    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        wrapper.copyBodyToResponse();
    }
}
