--
-- Turn off autocommit and start a transaction so that we can use the temp tables
--

--SET AUTOCOMMIT = OFF;

START TRANSACTION;

--
-- Insert scope information into the temporary tables.
-- 

INSERT INTO host_info (uuid, owner_uuid, host, config) VALUES
  ('0629d968-4eb4-467d-b45f-f4b1a1d3e7f0', '6e4bf681-2ac2-4341-9fff-b32802f1a72c', 'localhost', '{"databases":{"hsql-mem":{"driverClassName":"org.hsqldb.jdbcDriver","jdbcUrl":"jdbc:hsqldb:mem:oic;sql.syntax_mys=true","username":"oic","password":"oic","initScripts":["classpath:/db/hsql/hsql_database_tables.sql","classpath:/db/hsql/security-schema.sql","classpath:/db/hsql/users.sql","classpath:/db/hsql/clients.sql","classpath:/db/hsql/scopes.sql"]},"mysql-localhost":{"driverClassName":"com.mysql.jdbc.Driver","jdbcUrl":"jdbc:mysql://127.0.0.1:3306/kopenid","username":"kerp","password":"12345678"}},"restApiHosts":{"dev":{"host":"http://localhost:8320"},"qa":{"host":"https://kopenid.api-np.systeminventors.com"},"prod":{"host":"https://kopenid.api.systeminventors.com"}},"repositories":{"userInfoRepository":{"type":"DB","database":"hsql-mem"}}}');
  
-- 
-- Close the transaction and turn autocommit back on
-- 
    
COMMIT;

--SET AUTOCOMMIT = ON;