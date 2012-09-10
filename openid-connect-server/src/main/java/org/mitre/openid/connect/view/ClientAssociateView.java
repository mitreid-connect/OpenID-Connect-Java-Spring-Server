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
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * @author jricher
 *
 */
@Component("clientAssociate")
public class ClientAssociateView extends AbstractView {

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {

		response.setContentType("application/json");
		
		try {
			
			Gson gson = new GsonBuilder().create();
			
			ClientDetailsEntity client = (ClientDetailsEntity) model.get("client");
			
			JsonObject obj = new JsonObject();
			obj.addProperty("client_id", client.getClientId());
			obj.addProperty("client_secret", client.getClientSecret());
			obj.addProperty("expires_at", 0); // TODO: configure expiring client secrets. For now, they don't expire
			
			Writer out = response.getWriter();
		    gson.toJson(obj, out);
		    
		} catch (IOException e) {
			
			logger.error("IOException ", e);
			
		}

	}

}
