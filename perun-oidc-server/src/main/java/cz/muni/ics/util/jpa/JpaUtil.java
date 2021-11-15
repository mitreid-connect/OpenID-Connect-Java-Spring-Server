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
package cz.muni.ics.util.jpa;

import cz.muni.ics.data.PageCriteria;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

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

	/**
	 * Get a page of results from the specified TypedQuery
	 * by using the given PageCriteria to limit the query
	 * results. The PageCriteria will override any size or
	 * offset already specified on the query.
	 *
	 * @param <T>  the type parameter
	 * @param query the query
	 * @param pageCriteria the page criteria
	 * @return the list
	 */
	public static <T> List<T> getResultPage(TypedQuery<T> query, PageCriteria pageCriteria){
		query.setMaxResults(pageCriteria.getPageSize());
		query.setFirstResult(pageCriteria.getPageNumber()*pageCriteria.getPageSize());

		return query.getResultList();
	}

	public static <T, I> T saveOrUpdate(EntityManager entityManager, T entity) {
		T tmp = entityManager.merge(entity);
		entityManager.flush();
		return tmp;
	}

}
