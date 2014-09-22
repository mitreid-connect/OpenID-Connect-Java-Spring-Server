/*******************************************************************************
 * Copyright 2014 The MITRE Corporation
 *   and the MIT Kerberos and Internet Trust Consortium
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
 ******************************************************************************/

package org.mitre.oauth2.model;

/**
 * @author jricher
 *
 */
public interface SystemScope {

	/**
	 * @return the id
	 */
	Long getId();

	/**
	 * @param id the id to set
	 */
	void setId(Long id);

	/**
	 * @return the value
	 */
	String getValue();

	/**
	 * @param value the value to set
	 */
	void setValue(String value);

	/**
	 * @return the description
	 */
	String getDescription();

	/**
	 * @param description the description to set
	 */
	void setDescription(String description);

	/**
	 * @return the icon
	 */
	String getIcon();

	/**
	 * @param icon the icon to set
	 */
	void setIcon(String icon);

	/**
	 * @return the allowDynReg
	 */
	boolean isAllowDynReg();

	/**
	 * @param allowDynReg the allowDynReg to set
	 */
	void setAllowDynReg(boolean allowDynReg);

	/**
	 * @return the defaultScope
	 */
	boolean isDefaultScope();

	/**
	 * @param defaultScope the defaultScope to set
	 */
	void setDefaultScope(boolean defaultScope);

	/**
	 * @return the isStructured status
	 */
	boolean isStructured();

	/**
	 * @param structured the structured to set
	 */
	void setStructured(boolean structured);

	String getStructuredParamDescription();

	/**
	 * @param isStructured the isStructured to set
	 */
	void setStructuredParamDescription(String d);

	/**
	 * @return the structuredValue
	 */
	String getStructuredValue();

	/**
	 * @param structuredValue the structuredValue to set
	 */
	void setStructuredValue(String structuredValue);

}
