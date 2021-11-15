/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
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
package cz.muni.ics.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract class for performing an operation on a potentially large
 * number of items by paging through the items in discreet chunks.
 *
 * @param <T>  the type parameter
 * @author Colm Smyth.
 */
@Slf4j
public abstract class AbstractPageOperationTemplate<T> {

	private static final int DEFAULT_MAX_PAGES = 1000;
	private static final long DEFAULT_MAX_TIME_MILLIS = 600000L; //10 Minutes

	private int maxPages;
	private long maxTime;
	private boolean swallowExceptions = true;
	private String operationName;

	public AbstractPageOperationTemplate(String operationName){
		this(DEFAULT_MAX_PAGES, DEFAULT_MAX_TIME_MILLIS, operationName);
	}
	public AbstractPageOperationTemplate(int maxPages, long maxTime, String operationName){
		this.maxPages = maxPages;
		this.maxTime = maxTime;
		this.operationName = operationName;
	}

	public int getMaxPages() {
		return maxPages;
	}

	public void setMaxPages(int maxPages) {
		this.maxPages = maxPages;
	}

	public long getMaxTime() {
		return maxTime;
	}

	public void setMaxTime(long maxTime) {
		this.maxTime = maxTime;
	}

	public boolean isSwallowExceptions() {
		return swallowExceptions;
	}

	public void setSwallowExceptions(boolean swallowExceptions) {
		this.swallowExceptions = swallowExceptions;
	}

	public String getOperationName() {
		return operationName;
	}

	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

	/**
	 * Execute the operation on each member of a page of results
	 * retrieved through the fetch method. the method will execute
	 * until either the maxPages or maxTime limit is reached or until
	 * the fetch method returns no more results. Exceptions thrown
	 * performing the operation on the item will be swallowed if the
	 * swallowException (default true) field is set true.
	 */
	public void execute(){
		log.debug("[{}] Starting execution of paged operation. max time: {}, max pages: {}", getOperationName(), maxTime, maxPages);

		long startTime = System.currentTimeMillis();
		long executionTime = 0;
		int i = 0;

		int exceptionsSwallowedCount = 0;
		int operationsCompleted = 0;
		Set<String> exceptionsSwallowedClasses = new HashSet<>();

		while (i < maxPages && executionTime < maxTime){
			Collection<T> page = fetchPage();
			if (page == null || page.size() == 0){
				break;
			}

			for (T item : page) {
				try {
					doOperation(item);
					operationsCompleted++;
				} catch (Exception e){
					if(swallowExceptions){
						exceptionsSwallowedCount++;
						exceptionsSwallowedClasses.add(e.getClass().getName());
						log.debug("Swallowing exception " + e.getMessage(), e);
					} else {
						log.debug("Rethrowing exception " + e.getMessage());
						throw e;
					}
				}
			}

			i++;
			executionTime = System.currentTimeMillis() - startTime;
		}

		finalReport(operationsCompleted, exceptionsSwallowedCount, exceptionsSwallowedClasses);
	}

	/**
	 * Fetch a page of items.
	 *
	 * @return the collection of items
	 */
	public abstract Collection<T> fetchPage();

	/**
	 * Perform operation of fetched page of items.
	 *
	 * @param item the item
	 */
	protected abstract void doOperation(T item);

	/**
	 * Method responsible for final report of progress.
	 */
	protected void finalReport(int operationsCompleted, int exceptionsSwallowedCount, Set<String> exceptionsSwallowedClasses) {
		if (operationsCompleted > 0 || exceptionsSwallowedCount > 0) {
			log.info("[{}] Paged operation run: completed {}; swallowed {} exceptions",
				getOperationName(), operationsCompleted, exceptionsSwallowedCount);
		}
		for(String className:  exceptionsSwallowedClasses) {
			log.warn("[{}] Paged operation swallowed at least one exception of type {}", getOperationName(), className);
		}
	}
}
