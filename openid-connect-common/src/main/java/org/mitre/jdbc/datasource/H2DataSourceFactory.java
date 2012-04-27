/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
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
package org.mitre.jdbc.datasource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mitre.jdbc.datasource.util.SqlFileParser;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * {@inheritDoc}
 *
 * @author Matt Franklin
 *         <p/>
 *         Creates a JDBC DataSource that is fully initialized with the schema and data referenced in the
 *         application context.
 *         <p/>
 *         <p/>
 *         Usage:
 *         <code>
 *          <bean id="dataSource" class="org.mitre.jdbc.datasource.H2DataSourceFactory">
 *              <property name="databaseName" value="mymii"/>
 *              <property name="persist" value="true" />
 *              <property name="executeScriptQuery" value="SELECT * FROM gadgets" />
 *              <property name="scriptLocations" >
 *                  <list>
 *                      <value>file:db/sequences/create_all_seq.sql</value>
 *                      <value>file:db/tables/create_all_tables.sql</value>
 *                      <value>classpath:test-data.sql</value>
 *                  </list>
 *              </property>
 *          </bean>
 *         </code>
 *         <p/>
 */
public class H2DataSourceFactory implements FactoryBean {

    private static Log logger = LogFactory.getLog(H2DataSourceFactory.class);

    protected String databaseName;
    protected Boolean persist;
    protected String executeScriptQuery;
    protected List<Resource> scriptLocations;
    protected Map<String, String> dateConversionPatterns;

    /**
     * The DataSource singleton returned by the factory.
     */
    protected DataSource dataSource;

    /**
     * Creates a new factory with no initial values for required properties.
     * <p/>
     * NOTE: If the factory is initialized using the default constructor, the required properties must be set prior to use
     */
    public H2DataSourceFactory() {
    }

    /**
     * Creates a new factory and sets teh properties to the passed in parameters
     *
     * @param databaseName           {@see setDatabaseName}
     * @param scriptLocations        {@see setScriptLocations}
     * @param persist                {@see setPersist}
     * @param executeScriptQuery     {@see setLoadDataQuery}
     * @param dateConversionPatterns {@see setDateConversionPatterns}
     */
    public H2DataSourceFactory(String databaseName, List<Resource> scriptLocations, Boolean persist, String executeScriptQuery, Map<String, String> dateConversionPatterns) {
        setDatabaseName(databaseName);
        setScriptLocations(scriptLocations);
        setPersist(persist);
        setExecuteScriptQuery(executeScriptQuery);
        setDateConversionPatterns(dateConversionPatterns);
    }

    /**
     * Optional property
     * <p/>
     * Sets a map of conversion patterns to register with the Oracle conversion utilities
     *
     * @param dateConversionPatterns map of patterns keyed by Oracle syntax with a value of a {@link java.text.DateFormat}
     */
    public void setDateConversionPatterns(Map<String, String> dateConversionPatterns) {
        this.dateConversionPatterns = dateConversionPatterns;
    }

    /**
     * Optional Property
     * <p/>
     * Sets whether or not to persist the database
     *
     * @param persist boolean value
     */
    public void setPersist(Boolean persist) {
        this.persist = persist;
    }


    /**
     * Optional Property
     * <p/>
     * Set the query used to determine whether or not to execute the scripts on initialization
     *
     * @param executeScriptQuery the query to execute.  If there are no results of the query, the scripts referenced
     *                           in setScriptLocations will be executed.  The statement must be a select statement.
     */
    public void setExecuteScriptQuery(String executeScriptQuery) {
        this.executeScriptQuery = executeScriptQuery;
    }

    /**
     * Required Property
     * <p/>
     * Sets the name of the in memory database/schema
     *
     * @param databaseName the name such as "mymii"
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Required Property
     * <p/>
     * Sets the locations of the files containing DDL to be executed against the database
     * <p/>
     * NOTE: Files are executed in order
     *
     * @param scriptLocations list of {@link Resource} compatible location strings
     */
    public void setScriptLocations(List<Resource> scriptLocations) {
        this.scriptLocations = scriptLocations;
    }

    @PostConstruct
    public void initializeFactory() {
        validateProperties();
    }

    public Object getObject() throws Exception {
        return getDataSource();
    }

    public Class getObjectType() {
        return DataSource.class;
    }

    public boolean isSingleton() {
        return true;
    }

    /**
     * Gets the singleton DataSource if created. Initializes if not already created.
     *
     * @return the DataSource
     */
    public DataSource getDataSource() {
        if (dataSource == null) {
            initializeDataSource();
        }
        return dataSource;
    }

    /*
      Helper methods
    */
    protected void validateProperties() {
        if (databaseName == null) {
            throw new IllegalArgumentException("The name of the test database to create is required");
        }
        if (scriptLocations == null) {
            throw new IllegalArgumentException("The path to the database schema DDL is required");
        }
        if(persist == null) {
            persist = false;
        }
    }

    protected void initializeDataSource() {
        this.dataSource = createDataSource();
        populateDataSourceIfNecessary();
    }

    protected DataSource createDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.h2.Driver");
        ds.setUrl(getConnectionString());
        ds.setUsername("sa");
        ds.setPassword("");
        logger.debug("Created dataSource: " + ds.toString());
        return ds;
    }

    protected String getConnectionString() {
        return persist ?
                "jdbc:h2:file:" + databaseName + ";MODE=MySQL" :
                "jdbc:h2:mem:" + databaseName + ";MODE=MySQL;DB_CLOSE_DELAY=-1";
    }

    protected void populateDataSourceIfNecessary() {
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            if (!persist || testExecuteScriptQuery(conn, executeScriptQuery)) {
                logger.debug("Database is empty.  Loading script files");
                executeScripts(conn, scriptLocations);
            }
        } catch (SQLException e) {
            logger.error("Error querying or populating database", e);
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }
    }

    /*
      Static Helper methods
    */
    protected static void executeScripts(Connection connection, List<Resource> resources) {
        for (Resource script : resources) {
            try {
                String sql = new SqlFileParser(script).getSQL();
                logger.debug("Executing sql:\n" + sql);
                executeSql(sql, connection);
                logger.debug("Successfully executed statement");

            } catch (IOException e) {
                throw new RuntimeException("File IO Exception while loading " + script.getFilename(), e);
            } catch (SQLException e) {
                throw new RuntimeException("SQL exception occurred loading data from " + script.getFilename(), e);
            }
        }
    }

    protected static boolean testExecuteScriptQuery(Connection conn, String executeScriptQuery) {
        boolean result;
        try {
            //If the ResultSet has any rows, the first method will return true
            result = !executeQuery(conn, executeScriptQuery).first();
        } catch (SQLException e) {
            //Only return true if the exception we got is that the table was not found
            result =  e.getMessage().toLowerCase().matches("table \".*\" not found.*\n*.*");
        }
        logger.debug("Executed query " + executeScriptQuery + " with result " + result);
        
        return result;
    }

    protected static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Error closing connection to database", e);
            }
        }
    }

    protected static ResultSet executeQuery(Connection conn, String executeScriptQuery) throws SQLException {
        Statement statement = conn.createStatement();
        return statement.executeQuery(executeScriptQuery);
        
    }


    protected static void executeSql(String sql, Connection connection) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }


}

