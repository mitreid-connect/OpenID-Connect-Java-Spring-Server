/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.mitre.openid.connect.service.impl;

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.format.datetime.DateFormatter;

public abstract class MITREidDataServiceSupport {
	private final DateFormatter dateFormatter;
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(MITREidDataServiceSupport.class);

	public MITREidDataServiceSupport() {
		dateFormatter = new DateFormatter();
		dateFormatter.setIso(ISO.DATE_TIME);
	}

	protected Date utcToDate(String value) {
		if (value == null) {
			return null;
		}
		try {
			return dateFormatter.parse(value, Locale.ENGLISH);
		} catch (ParseException ex) {
			logger.error("Unable to parse datetime {}", value, ex);
		}
		return null;
	}

	protected String toUTCString(Date value) {
		if (value == null) {
			return null;
		}
		return dateFormatter.print(value, Locale.ENGLISH);
	}

}
