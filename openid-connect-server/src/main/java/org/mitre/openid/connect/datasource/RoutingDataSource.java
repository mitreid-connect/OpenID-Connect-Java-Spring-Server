package org.mitre.openid.connect.datasource;


import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * Created by Vishwanathan.D on 6/7/17.
 */
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DbContextHolder.getDbType();
    }
}
