package org.mitre.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Abstract class for performing an operation on a potentially large
 * number of items by paging through the items in discreet chunks.
 *
 * @param <T>  the type parameter
 * @author Colm Smyth.
 */
public abstract class AbstractPageOperationTemplate<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPageOperationTemplate.class);

    private static int DEFAULT_MAX_PAGES = 1000;
    private static long DEFAULT_MAX_TIME_MILLIS = 600000L; //10 Minutes

    /**
     * int specifying the maximum number of
     * pages which should be fetched before
     * execution should terminate
     */
    private int maxPages;

    /**
     * long specifying the maximum execution time
     * in milliseconds
     */
    private long maxTime;

    /**
     * boolean specifying whether or not Exceptions
     * incurred performing the operation should be
     * swallowed during execution default true.
     */
    private boolean swallowExceptions = true;


    /**
     * default constructor which sets the value of
     * maxPages and maxTime to DEFAULT_MAX_PAGES and
     * DEFAULT_MAX_TIME_MILLIS respectively
     */
    public AbstractPageOperationTemplate(){
        this(DEFAULT_MAX_PAGES, DEFAULT_MAX_TIME_MILLIS);
    }


    /**
     * Instantiates a new AbstractPageOperationTemplate with the
     * given maxPages and maxTime
     *
     * @param maxPages the maximum number of pages to fetch.
     * @param maxTime the maximum execution time.
     */
    public AbstractPageOperationTemplate(int maxPages, long maxTime){
        this.maxPages = maxPages;
        this.maxTime = maxTime;
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
        logger.info("Starting execution of paged operation. maximum time: " + maxTime
                + " maximum pages: " + maxPages);

        long startTime = System.currentTimeMillis();
        long executionTime = 0;
        int i = 0;

        int exceptionsSwallowedCount = 0;
        int operationsCompleted = 0;
        Set<String> exceptionsSwallowedClasses = new HashSet<String>();


        while (i< maxPages && executionTime < maxTime){
            Collection<T> page = fetchPage();
            if(page == null || page.size() == 0){
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
                        logger.debug("Swallowing exception " + e.getMessage(), e);
                    } else {
                        logger.debug("Rethrowing exception " + e.getMessage());
                        throw e;
                    }
                }
            }

            i++;
            executionTime = System.currentTimeMillis() - startTime;
        }

        logger.info("Paged operation run completed " + operationsCompleted + " swallowed " + exceptionsSwallowedCount + " exceptions");
        for(String className:  exceptionsSwallowedClasses) {
            logger.warn("Paged operation swallowed at least one exception of type " + className);
        }
    }

    /**
     * method responsible for fetching
     * a page of items.
     *
     * @return the collection of items
     */
    public abstract Collection<T> fetchPage();

    /**
     * method responsible for performing desired
     * operation on a fetched page item.
     *
     * @param item the item
     */
    protected abstract void doOperation(T item);

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
}
