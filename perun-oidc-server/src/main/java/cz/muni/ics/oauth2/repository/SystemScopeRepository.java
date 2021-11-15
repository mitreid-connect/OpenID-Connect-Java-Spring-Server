/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.oauth2.repository;

import cz.muni.ics.oauth2.model.SystemScope;
import java.util.Set;

/**
 * @author jricher
 */
public interface SystemScopeRepository {

	Set<SystemScope> getAll();

	SystemScope getById(Long id);

	SystemScope getByValue(String value);

	void remove(SystemScope scope);

	SystemScope save(SystemScope scope);

}
