package org.mitre.data;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Colm Smyth
 */
public class AbstractPageOperationTemplateTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void execute_zeropages() {
        CountingPageOperation op = new CountingPageOperation(0,Long.MAX_VALUE);
        op.execute();

        assertEquals(0L, op.counter);
    }

    @Test
    public void execute_singlepage() {
        CountingPageOperation op = new CountingPageOperation(1,Long.MAX_VALUE);
        op.execute();

        assertEquals(10L, op.counter);
    }

    @Test
    public void execute_negpage() {
        CountingPageOperation op = new CountingPageOperation(-1,Long.MAX_VALUE);
        op.execute();

        assertEquals(0L, op.counter);
    }

    @Test
    public void execute_npage(){
        int n = 7;
        CountingPageOperation op = new CountingPageOperation(n,Long.MAX_VALUE);
        op.execute();

        assertEquals(n*10L, op.counter);
    }

    @Test
    public void execute_nullpage(){
        CountingPageOperation op = new NullPageCountingPageOperation(Integer.MAX_VALUE, Long.MAX_VALUE);
        op.execute();

        assertEquals(0L, op.getCounter());
    }

    @Test
    public void execute_emptypage(){
        CountingPageOperation op = new EmptyPageCountingPageOperation(Integer.MAX_VALUE, Long.MAX_VALUE);
        op.execute();

        assertEquals(0L, op.getCounter());
    }

    @Test
    public void execute_zerotime(){
        CountingPageOperation op = new CountingPageOperation(Integer.MAX_VALUE,0L);
        op.execute();

        assertEquals(0L, op.getCounter());
        assertEquals(0L, op.getLastFetchTime());
    }

    @Test
    public void execute_nonzerotime(){
        Long timeMillis = 100L;
        CountingPageOperation op = new CountingPageOperation(Integer.MAX_VALUE,timeMillis);
        op.execute();

        assertTrue("start time " + op.getStartTime() + "" +
                " to last fetch time " + op.getLastFetchTime() +
                " exceeds max time" + timeMillis, op.getLastFetchTime() - op.getStartTime() <= timeMillis);
    }

    @Test
    public void execute_negtime(){
        Long timeMillis = -100L;
        CountingPageOperation op = new CountingPageOperation(Integer.MAX_VALUE,timeMillis);
        op.execute();

        assertEquals(0L, op.getCounter());
    }

    @Test
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
        private int pageSize = 10;
        private long counter = 0L;
        private long startTime;
        private long lastFetchTime;

        private CountingPageOperation(int maxPages, long maxTime) {
            super(maxPages, maxTime);
            startTime = System.currentTimeMillis();
        }

        @Override
        public Collection<String> fetchPage() {
            lastFetchTime = System.currentTimeMillis();
            List<String> page = new ArrayList<>(pageSize);
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

        public long getLastFetchTime() {
            return lastFetchTime;
        }

        public long getStartTime(){
            return startTime;
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
