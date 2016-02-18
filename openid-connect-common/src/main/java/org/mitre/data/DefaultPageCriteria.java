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
