/*******************************************************************************
 * Copyright 2017 The MIT Internet Trust Consortium
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
package org.mitre.data;

/**
 * Default implementation of PageCriteria which specifies
 * both page to be retrieved and page size in the constructor.
 *
 * @author Colm Smyth
 */
public class DefaultPageCriteria implements PageCriteria {

	private static final int DEFAULT_PAGE_NUMBER = 0;
	private static final int DEFAULT_PAGE_SIZE = 100;

	private int pageNumber;
	private int pageSize;

	public DefaultPageCriteria(){
		this(DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
	}

	public DefaultPageCriteria(int pageNumber, int pageSize) {
		this.pageNumber = pageNumber;
		this.pageSize = pageSize;
	}

	@Override
	public int getPageNumber() {
		return pageNumber;
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}
}
