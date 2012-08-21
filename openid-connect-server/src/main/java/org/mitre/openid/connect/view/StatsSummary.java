/**
 * 
 */
package org.mitre.openid.connect.view;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.view.AbstractView;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author jricher
 *
 */
@Component("statsSummaryJson")
public class StatsSummary extends AbstractView {

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) {
        Gson gson = new GsonBuilder()
        .setExclusionStrategies(new ExclusionStrategy() {

            public boolean shouldSkipField(FieldAttributes f) {

                return false;
            }

            public boolean shouldSkipClass(Class<?> clazz) {
                // skip the JPA binding wrapper
                if (clazz.equals(BeanPropertyBindingResult.class)) {
                    return true;
                }
                return false;
            }

        }).create();

        response.setContentType("application/json");


        try {
	
        	Writer out = response.getWriter();
        	Object obj = model.get("entity");
        	if (obj == null) {
        		obj = model;
        	}

        	gson.toJson(obj, out);
    
        } catch (IOException e) {
	
        	logger.error("IOException in JSONClientView.java: " + e.getStackTrace());
	
        }

	}

}
