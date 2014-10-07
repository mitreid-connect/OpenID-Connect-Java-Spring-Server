/*
 * Copyright 2014 arielak.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mitre.openid.connect.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author arielak
 */
public class DateUtil {
    private static final Logger log = LoggerFactory.getLogger(DateUtil.class);
    private static final String ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    private static final SimpleDateFormat sdf = new SimpleDateFormat(ISO_FORMAT);
    private static final TimeZone utc = TimeZone.getTimeZone("UTC");    
    
    public static String toUTCString(Date date) {
        if (date == null) {
            return null;
        }
        sdf.setTimeZone(utc);
        return sdf.format(date);
    }

    public static Date utcToDate(String s) {
        if (s == null) {
            return null;
        }
        Date d = null;
        try {
            d = sdf.parse(s);
        } catch(ParseException ex) {
            log.error("Unable to parse date string {}", s, ex);
        }
        return d;
    }
}
