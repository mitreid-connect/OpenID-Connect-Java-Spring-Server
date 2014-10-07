/**
 * *****************************************************************************
 * Copyright 2014 The MITRE Corporation and the MIT Kerberos and Internet Trust
 * Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * ****************************************************************************
 */
package org.mitre.openid.connect.service.impl;

import com.google.common.io.BaseEncoding;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.mitre.openid.connect.service.MITREidDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author arielak
 */
public abstract class MITREidDataService_1_X implements MITREidDataService {
	private static Logger logger = LoggerFactory.getLogger(MITREidDataService_1_X.class);
    
    protected static <T> T base64UrlDecodeObject(String encoded, Class<T> type) {
    	if (encoded == null) {
    		return null;
    	} else {
	        T deserialized = null;
	        try {
	            byte[] decoded = BaseEncoding.base64Url().decode(encoded);
	            ByteArrayInputStream bais = new ByteArrayInputStream(decoded);
	            ObjectInputStream ois = new ObjectInputStream(bais);
	            deserialized = type.cast(ois.readObject());
	            ois.close();
	            bais.close();
	        } catch (Exception ex) {
	            logger.error("Unable to decode object", ex);
	        }
	        return deserialized;
    	}
    }
    
    protected static String base64UrlEncodeObject(Serializable obj) {
    	if (obj == null) {
    		return null;
    	} else {
	        String encoded = null;
	        try {
	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
	            ObjectOutputStream oos = new ObjectOutputStream(baos);
	            oos.writeObject(obj);
	            encoded = BaseEncoding.base64Url().encode(baos.toByteArray());
	            oos.close();
	            baos.close();
	        } catch (IOException ex) {
	            logger.error("Unable to encode object", ex);
	        }
	        return encoded;
    	}
    }
    protected static Set readSet(JsonReader reader) throws IOException {
        Set arraySet = null;
        reader.beginArray();
        switch (reader.peek()) {
            case STRING:
                arraySet = new HashSet<String>();
                while (reader.hasNext()) {
                    arraySet.add(reader.nextString());
                }
                break;
            case NUMBER:
                arraySet = new HashSet<Long>();
                while (reader.hasNext()) {
                    arraySet.add(reader.nextLong());
                }
                break;
            default:
                arraySet = new HashSet();
                break;
        }
        reader.endArray();
        return arraySet;
    }
    
    protected static Map readMap(JsonReader reader) throws IOException {
        Map map = new HashMap<String, Object>();
        reader.beginObject();
        while(reader.hasNext()) {
            String name = reader.nextName();
            Object value = null;
            switch(reader.peek()) {
                case STRING:
                    value = reader.nextString();
                    break;
                case BOOLEAN:
                    value = reader.nextBoolean();
                    break;
                case NUMBER:
                    value = reader.nextLong();
                    break;
            }
            map.put(name, value);
        }
        reader.endObject();
        return map;
    }
    
	protected void writeNullSafeArray(JsonWriter writer, Set<String> items)
			throws IOException {
		if (items != null) {
			writer.beginArray();
		    for (String s : items) {
		        writer.value(s);
		    }
		    writer.endArray();
		} else {
			writer.nullValue();
		}
	}
}
