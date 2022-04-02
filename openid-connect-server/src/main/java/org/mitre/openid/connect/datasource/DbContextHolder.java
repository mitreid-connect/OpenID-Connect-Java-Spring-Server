package org.mitre.openid.connect.datasource;

/**
 * Created by Vishwanathan.D on 6/7/17.
 */
public class DbContextHolder {

    private final static ThreadLocal<DbType> contextHolder = new ThreadLocal<>();

    public static void setDbType(DbType dbType) {
        if(dbType == null){
            throw new NullPointerException();
        }
        contextHolder.set(dbType);
    }

    public static DbType getDbType() {
        return contextHolder.get();
    }

    public static void clearDbType() {
        contextHolder.remove();
    }
}
