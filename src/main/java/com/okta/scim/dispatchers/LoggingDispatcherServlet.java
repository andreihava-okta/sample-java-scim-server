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

import com.okta.scim.database.TransactionDatabase;
import com.okta.scim.models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Dispatch Servlet to log all transactions
 */
public class LoggingDispatcherServlet extends DispatcherServlet {
    @Autowired
    TransactionDatabase db;

    private Logger logger = LoggerFactory.getLogger(LoggingDispatcherServlet.class);

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

    /**
     * Creates a {@link Transaction} for a transaction and saves it to the {@link TransactionDatabase}
     * @param requestToCache The transaction {@link HttpServletRequest}
     * @param responseToCache The transaction {@link HttpServletResponse}
     * @param handler The transaction {@link HandlerExecutionChain}
     */
    private void log(HttpServletRequest requestToCache, HttpServletResponse responseToCache, HandlerExecutionChain handler) throws UnsupportedEncodingException {
        Transaction req = new Transaction()
                .generateId()
                .setTimestamp()
                .setMethod(requestToCache.getMethod())
                .setHttpCode(responseToCache.getStatus())
                .setEndpoint(requestToCache.getRequestURI() + (requestToCache.getQueryString() != null ? "?" + URLDecoder.decode(requestToCache.getQueryString(), "UTF-8") : ""))
                .setJavaMethod(handler.toString())
                .setRequestBody(getRequestPayload(requestToCache))
                .setResponseBody(getResponsePayload(responseToCache))
                .setRequestId(requestToCache.getAttribute("rid").toString());

        db.save(req);
    }

    /**
     * Extracts the body from a request
     * @param request The {@link HttpServletRequest}
     * @return A {@link String} containing the body, or '[unknown]'
     */
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
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return "[unknown]";
    }

    /**
     * Extracts the body from a response
     * @param response The {@link HttpServletResponse}
     * @return A {@link String} containing the body, or '[unknown]'
     */
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
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return "[unknown]";
    }

    /**
     * Updates the response, copying the contents back
     * @param response The {@link HttpServletResponse}
     * @throws IOException
     */
    private void updateResponse(HttpServletResponse response) throws IOException {
        ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
        try {
            wrapper.copyBodyToResponse();
        } catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
