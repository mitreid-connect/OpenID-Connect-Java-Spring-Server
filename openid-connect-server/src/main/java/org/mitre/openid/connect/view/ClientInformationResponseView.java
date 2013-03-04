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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.AbstractView;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

/**
 * 
 * Provides a minimal representation of a client's registration information, to be shown from the dynamic registration endpoint
 * on the client_register and rotate_secret operations.
 * 
 * @author jricher
 *
 */
@Component("clientInformationResponse")
public class ClientInformationResponseView extends AbstractView {

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		response.setContentType("application/json");
		
		// note that this won't serialize nulls by default
		Gson gson = new Gson();
		
		ClientDetailsEntity client = (ClientDetailsEntity) model.get("client");
		OAuth2AccessTokenEntity token = (OAuth2AccessTokenEntity) model.get("token");
		HttpStatus code = (HttpStatus) model.get("code");
		if (code == null) {
			code = HttpStatus.OK;
		}
		
		JsonObject obj = new JsonObject();
		
		obj.addProperty("client_id", client.getClientId());
		if (client.getClientSecret() != null) {
			obj.addProperty("client_secret", client.getClientSecret());
			obj.addProperty("expires_at", 0); // TODO: do we want to let secrets expire?
		}
		obj.addProperty("issued_at", client.getCreatedAt().getTime());

		obj.addProperty("registration_access_token", token.getValue());
		
		// TODO: urlencode the client id for safety?
		String uri = request.getRequestURL() + "/" + client.getClientId();		
		obj.addProperty("registration_client_uri", uri);
		
		
		// add in all other client properties
		
		
		
		try {
	        Writer out = response.getWriter();
	        gson.toJson(obj, out);
        } catch (JsonIOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }		
			
	}

}
