package cz.muni.ics.oidc.server;

import cz.muni.ics.oauth2.model.AuthorizationCodeEntity;
import cz.muni.ics.oauth2.model.DeviceCode;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity;
import cz.muni.ics.openid.connect.models.Acr;
import cz.muni.ics.openid.connect.models.DeviceCodeAcr;
import java.time.Instant;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.QueryTimeoutException;
import org.springframework.stereotype.Repository;

@Repository
public class CustomClearTasks {

    @PersistenceContext(unitName = "defaultPersistenceUnit")
    private EntityManager manager;

    public int clearExpiredTokens(long timeout) {
        int count = 0;
        count += this.clearExpiredAccessTokens(timeout);
        count += this.clearExpiredRefreshTokens(timeout);
        count += this.clearOrphanedAuthHolders(timeout);
        return count;
    }

    int clearExpiredAccessTokens(long timeout) {
        manager.flush();
        manager.clear();
        int count = 0;
        Query query1 = manager.createQuery("DELETE FROM OAuth2AccessTokenEntity a " +
                "WHERE a.expiration <= :" + OAuth2AccessTokenEntity.PARAM_DATE);
        query1.setParameter(OAuth2AccessTokenEntity.PARAM_DATE, new Date());
        if (timeout > 0) {
            query1.setHint("javax.persistence.query.timeout", timeout);
        }
        try {
            count += query1.executeUpdate();
        } catch (QueryTimeoutException e) {
            // this is OK
        }
        return count;
    }

    int clearExpiredRefreshTokens(long timeout) {
        manager.flush();
        manager.clear();
        int count = 0;
        Query query2 = manager.createQuery("DELETE FROM OAuth2RefreshTokenEntity r " +
                "WHERE r.expiration <= :" + OAuth2RefreshTokenEntity.PARAM_DATE);
        query2.setParameter(OAuth2RefreshTokenEntity.PARAM_DATE, new Date());
        if (timeout > 0) {
            query2.setHint("javax.persistence.query.timeout", timeout);
        }
        try {
            count += query2.executeUpdate();
        } catch (QueryTimeoutException e) {
            // this is OK
        }
        return count;
    }

    int clearOrphanedAuthHolders(long timeout) {
        manager.flush();
        manager.clear();
        int count = 0;
        Query query3 = manager.createQuery("DELETE FROM AuthenticationHolderEntity a " +
                "WHERE a.id NOT IN (SELECT t.authenticationHolder.id FROM OAuth2AccessTokenEntity t) AND " +
                "a.id NOT IN (SELECT r.authenticationHolder.id FROM OAuth2RefreshTokenEntity r) AND " +
                "a.id NOT IN (SELECT c.authenticationHolder.id FROM AuthorizationCodeEntity c)");
        if (timeout > 0) {
            query3.setHint("javax.persistence.query.timeout", timeout);
        }
        try {
            count += query3.executeUpdate();
        } catch (QueryTimeoutException e) {
            // this is OK
        }
        return count;
    }

    public int clearExpiredSites(long timeout) {
        manager.flush();
        manager.clear();
        int count = 0;
        Query query = manager.createQuery("DELETE FROM ApprovedSite a WHERE a.timeoutDate <= :date");
        query.setParameter("date", new Date());
        if (timeout > 0) {
            query.setHint("javax.persistence.query.timeout", timeout);
        }
        try {
            count += query.executeUpdate();
        } catch (QueryTimeoutException e) {
            // this is OK
        }
        return count;
    }

    public int clearExpiredAuthorizationCodes(long timeout) {
        manager.flush();
        manager.clear();
        int count = 0;
        Query query = manager.createQuery("DELETE FROM AuthorizationCodeEntity a " +
                "WHERE a.expiration <= :" + AuthorizationCodeEntity.PARAM_DATE);
        query.setParameter(AuthorizationCodeEntity.PARAM_DATE, new Date());
        if (timeout > 0) {
            query.setHint("javax.persistence.query.timeout", timeout);
        }
        try {
            count += query.executeUpdate();
        } catch (QueryTimeoutException e) {
            // this is OK
        }
        return count;
    }

    public int clearExpiredDeviceCodes(long timeout) {
        manager.flush();
        manager.clear();
        int count = 0;
        Query query = manager.createQuery("DELETE FROM DeviceCode d WHERE d.expiration <= :" + DeviceCode.PARAM_DATE);
        query.setParameter(DeviceCode.PARAM_DATE, new Date());
        if (timeout > 0) {
            query.setHint("javax.persistence.query.timeout", timeout);
        }
        try {
            count += query.executeUpdate();
        } catch (QueryTimeoutException e) {
            // this is OK
        }
        return count;
    }

    public int clearExpiredAcrs(long timeout) {
        manager.flush();
        manager.clear();
        int count = 0;
        Query query = manager.createNamedQuery(Acr.DELETE_EXPIRED);
        query.setParameter(Acr.PARAM_EXPIRES_AT, Instant.now().toEpochMilli());
        if (timeout > 0) {
            query.setHint("javax.persistence.query.timeout", timeout);
        }
        try {
            count += query.executeUpdate();
        } catch (QueryTimeoutException e) {
            // this is OK
        }
        return count;
    }

    public int clearExpiredDeviceCodeAcrs(long timeout) {
        manager.flush();
        manager.clear();
        int count = 0;
        Query query = manager.createNamedQuery(DeviceCodeAcr.DELETE_EXPIRED);
        query.setParameter(DeviceCodeAcr.PARAM_EXPIRES_AT, Instant.now().toEpochMilli());
        if (timeout > 0) {
            query.setHint("javax.persistence.query.timeout", timeout);
        }
        try {
            count += query.executeUpdate();
        } catch (QueryTimeoutException e) {
            // this is OK
        }
        return count;
    }

}
