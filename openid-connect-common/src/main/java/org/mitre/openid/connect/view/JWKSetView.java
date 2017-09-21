/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
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
/**
 *
 */
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

/**
 * @author jricher
 *
 */
@Component(JWKSetView.VIEWNAME)
public class JWKSetView extends AbstractView {

	public static final String VIEWNAME = "jwkSet";
	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(JWKSetView.class);

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);


		//BiMap<String, PublicKey> keyMap = (BiMap<String, PublicKey>) model.get("keys");
		Map<String, JWK> keys = (Map<String, JWK>) model.get("keys");

		JWKSet jwkSet = new JWKSet(new ArrayList<>(keys.values()));

		try {

			Writer out = response.getWriter();
			out.write(jwkSet.toString());

		} catch (IOException e) {

			logger.error("IOException in JWKSetView.java: ", e);

		}

	}

}
