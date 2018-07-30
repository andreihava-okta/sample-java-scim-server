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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Utility class for {@link String} operations
 */
public class StringUtil {
    private static Logger logger = LoggerFactory.getLogger(StringUtil.class);

    /**
     * Converts a {@link BufferedReader} to a {@link String}
     * @param reader The {@link BufferedReader} to convert
     * @return The contents of the reader
     */
    public static String readerToString(BufferedReader reader) {
        StringBuilder sb = new StringBuilder();

        try {
            char[] charBuffer = new char[128];
            int bytesRead;

            while ((bytesRead = reader.read(charBuffer)) != -1) {
                sb.append(charBuffer, 0, bytesRead);
            }
        } catch(IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch(IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        return sb.toString();
    }
}
