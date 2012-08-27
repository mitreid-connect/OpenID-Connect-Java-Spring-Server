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
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * @author jricher
 *
 */
@Component("clientUpdate")
public class ClientUpdateView extends AbstractView {

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {

		response.setContentType("application/json");
		
		try {
			
			Gson gson = new GsonBuilder().create();
			
			ClientDetailsEntity client = (ClientDetailsEntity) model.get("client");
			
			JsonObject obj = new JsonObject();
			obj.addProperty("client_id", client.getClientId());
			
			Writer out = response.getWriter();
		    gson.toJson(obj, out);
		    
		} catch (IOException e) {
			
			logger.error("IOException " + e.getStackTrace());
			
		}

	}

}
