/**
 * 
 */
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.openid.connect.ClientDetailsEntityJsonProcessor;
import org.springframework.http.HttpStatus;
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
@Component("clientInformationResponseView")
public class ClientInformationResponseView extends AbstractView {

	// note that this won't serialize nulls by default
	private Gson gson = new Gson();

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		response.setContentType("application/json");

		ClientDetailsEntity c = (ClientDetailsEntity) model.get("client");
		OAuth2AccessTokenEntity token = (OAuth2AccessTokenEntity) model.get("token");
		HttpStatus code = (HttpStatus) model.get("code");
		if (code == null) {
			code = HttpStatus.OK;
		}

		// TODO: urlencode the client id for safety?
		String uri = request.getRequestURL() + "/" + c.getClientId();
		JsonObject o = ClientDetailsEntityJsonProcessor.serialize(c, token, uri);

		try {
			Writer out = response.getWriter();
			gson.toJson(o, out);
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
