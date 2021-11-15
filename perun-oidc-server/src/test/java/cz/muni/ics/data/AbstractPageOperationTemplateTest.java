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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Colm Smyth
 */
public class AbstractPageOperationTemplateTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test(timeout = 1000L)
	public void execute_zeropages() {
		CountingPageOperation op = new CountingPageOperation(0,Long.MAX_VALUE);
		op.execute();

		assertEquals(0L, op.counter);
	}

	@Test(timeout = 1000L)
	public void execute_singlepage() {
		CountingPageOperation op = new CountingPageOperation(1,Long.MAX_VALUE);
		op.execute();

		assertEquals(10L, op.counter);
	}

	@Test(timeout = 1000L)
	public void execute_negpage() {
		CountingPageOperation op = new CountingPageOperation(-1,Long.MAX_VALUE);
		op.execute();

		assertEquals(0L, op.counter);
	}

	@Test(timeout = 1000L)
	public void execute_npage(){
		int n = 7;
		CountingPageOperation op = new CountingPageOperation(n,Long.MAX_VALUE);
		op.execute();

		assertEquals(n*10L, op.counter);
	}

	@Test(timeout = 1000L)
	public void execute_nullpage(){
		CountingPageOperation op = new NullPageCountingPageOperation(Integer.MAX_VALUE, Long.MAX_VALUE);
		op.execute();

		assertEquals(0L, op.getCounter());
	}

	@Test(timeout = 1000L)
	public void execute_emptypage(){
		CountingPageOperation op = new EmptyPageCountingPageOperation(Integer.MAX_VALUE, Long.MAX_VALUE);
		op.execute();

		assertEquals(0L, op.getCounter());
	}

	@Test(timeout = 1000L)
	public void execute_zerotime(){
		CountingPageOperation op = new CountingPageOperation(Integer.MAX_VALUE,0L);
		op.execute();

		assertEquals(0L, op.getCounter());
		assertEquals(0L, op.getTimeToLastFetch());
	}

	/*
	 * This is a valid test however it is vulnerable to a race condition
	 * as such it is being ignored.
	 */
	@Test(timeout = 1000L)
	@Ignore
	public void execute_nonzerotime(){
		Long timeMillis = 200L;
		CountingPageOperation op = new CountingPageOperation(Integer.MAX_VALUE,timeMillis);
		op.execute();

		assertFalse("last fetch time " + op.getTimeToLastFetch() + "" +
				" and previous fetch time  " + op.getTimeToPreviousFetch() +
				" exceed max time" + timeMillis,
				op.getTimeToLastFetch() > timeMillis
				&& op.getTimeToPreviousFetch() > timeMillis);
	}

	@Test(timeout = 1000L)
	public void execute_negtime(){
		Long timeMillis = -100L;
		CountingPageOperation op = new CountingPageOperation(Integer.MAX_VALUE,timeMillis);
		op.execute();

		assertEquals(0L, op.getCounter());
	}

	@Test(timeout = 1000L)
	public void execute_swallowException(){
		CountingPageOperation op = new EvenExceptionCountingPageOperation(1, 1000L);
		op.execute();

		assertTrue(op.isSwallowExceptions());
		assertEquals(5L, op.getCounter());
	}

	@Test(expected = IllegalStateException.class)
	public void execute_noSwallowException(){
		CountingPageOperation op = new EvenExceptionCountingPageOperation(1, 1000L);
		op.setSwallowExceptions(false);

		try {
			op.execute();
		}finally {
			assertEquals(1L, op.getCounter());
		}
	}


	private static class CountingPageOperation extends AbstractPageOperationTemplate<String>{

		private int currentPageFetch;
		private final int pageSize = 10;
		private long counter = 0L;
		private final long startTime;
		private long timeToLastFetch;
		private long timeToPreviousFetch;

		private CountingPageOperation(int maxPages, long maxTime) {
			super(maxPages, maxTime, "CountingPageOperation");
			startTime = System.currentTimeMillis();
		}

		@Override
		public Collection<String> fetchPage() {
			timeToPreviousFetch = timeToLastFetch > 0 ? timeToLastFetch : 0;
			timeToLastFetch = System.currentTimeMillis() - startTime;

			List<String> page = new ArrayList<String>(pageSize);
			for(int i = 0; i < pageSize; i++ ) {
				page.add("item " + currentPageFetch * pageSize + i);
			}
			currentPageFetch++;
			return page;
		}

		@Override
		protected void doOperation(String item) {
			counter++;
		}

		public long getCounter() {
			return counter;
		}

		public long getTimeToLastFetch() {
			return timeToLastFetch;
		}

		public long getTimeToPreviousFetch() {
			return timeToPreviousFetch;
		}
	}

	private static class NullPageCountingPageOperation extends CountingPageOperation {
		private NullPageCountingPageOperation(int maxPages, long maxTime) {
			super(maxPages, maxTime);
		}

		@Override
		public Collection<String> fetchPage() {
			return null;
		}
	}

	private static class EmptyPageCountingPageOperation extends CountingPageOperation {
		private EmptyPageCountingPageOperation(int maxPages, long maxTime) {
			super(maxPages, maxTime);
		}

		@Override
		public Collection<String> fetchPage() {
			return new ArrayList<>(0);
		}
	}

	private static class EvenExceptionCountingPageOperation extends CountingPageOperation {

		private int callCounter;
		private EvenExceptionCountingPageOperation(int maxPages, long maxTime) {
			super(maxPages, maxTime);
		}

		@Override
		protected void doOperation(String item) {
			callCounter++;
			if(callCounter%2 == 0){
				throw new IllegalStateException("even number items cannot be processed");
			}

			super.doOperation(item);

		}
	}
}
