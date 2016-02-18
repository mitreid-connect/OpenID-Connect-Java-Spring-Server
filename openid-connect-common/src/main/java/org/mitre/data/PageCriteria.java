package org.mitre.data;

/**
 * Interface which defines page criteria for use in
 * a repository operation.
 *
 * @author Colm Smyth
 */
public interface PageCriteria {

    public int getPageNumber();
    public int getPageSize();
}
