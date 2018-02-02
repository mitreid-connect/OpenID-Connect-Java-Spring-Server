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
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.openid.connect.ClientDetailsEntityJsonProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

/**
 *
 * Provides representation of a client's registration metadata, to be shown from the dynamic registration endpoint
 * on the client_register and rotate_secret operations.
 *
 * @author jricher
 *
 */
@Component(ClientInformationResponseView.VIEWNAME)
public class ClientInformationResponseView extends AbstractView {

	/**
	 * Logger for this class
	 */
	private static final Logger logger = LoggerFactory.getLogger(ClientInformationResponseView.class);

	public static final String VIEWNAME = "clientInformationResponseView";

	// note that this won't serialize nulls by default
	private Gson gson = new Gson();

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		RegisteredClient c = (RegisteredClient) model.get("client");
		//OAuth2AccessTokenEntity token = (OAuth2AccessTokenEntity) model.get("token");
		//String uri = (String)model.get("uri"); //request.getRequestURL() + "/" + c.getClientId();

		HttpStatus code = (HttpStatus) model.get(HttpCodeView.CODE);
		if (code == null) {
			code = HttpStatus.OK;
		}
		response.setStatus(code.value());

		JsonObject o = ClientDetailsEntityJsonProcessor.serialize(c);

		try {
			Writer out = response.getWriter();
			gson.toJson(o, out);
		} catch (JsonIOException e) {

			logger.error("JsonIOException in ClientInformationResponseView.java: ", e);

		} catch (IOException e) {

			logger.error("IOException in ClientInformationResponseView.java: ", e);

		}

	}

}
