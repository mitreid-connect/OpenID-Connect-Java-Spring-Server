/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
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
package org.mitre.util.jpa;

import java.util.List;

import javax.persistence.EntityManager;

/**
 * @author mfranklin
 *         Date: 4/28/11
 *         Time: 2:13 PM
 */
public class JpaUtil {
	public static <T> T getSingleResult(List<T> list) {
		switch(list.size()) {
			case 0:
				return null;
			case 1:
				return list.get(0);
			default:
				throw new IllegalStateException("Expected single result, got " + list.size());
		}
	}

	public static <T, I> T saveOrUpdate(I id, EntityManager entityManager, T entity) {
		if (id == null) {
			entityManager.persist(entity);
			entityManager.flush();
			return entity;
		} else {
			T tmp = entityManager.merge(entity);
			entityManager.flush();
			return tmp;
		}
	}
}
