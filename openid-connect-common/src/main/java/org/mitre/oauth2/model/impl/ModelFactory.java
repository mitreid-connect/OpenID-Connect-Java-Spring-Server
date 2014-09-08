package org.mitre.oauth2.model.impl;

import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.AuthorizationCodeEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.model.OAuth2AccessTokenEntity;
import org.mitre.oauth2.model.OAuth2RefreshTokenEntity;
import org.mitre.oauth2.model.RegisteredClient;
import org.mitre.oauth2.model.SystemScope;

public class ModelFactory {
	
	private static ModelFactory factory = new ModelFactory();
	
	private Class<? extends AuthenticationHolderEntity> authHolderType = DefaultAuthenticationHolderEntity.class;
	private Class<? extends AuthorizationCodeEntity> authCodeType = DefaultAuthorizationCodeEntity.class;
	private Class<? extends ClientDetailsEntity> clientDetailsType = DefaultClientDetailsEntity.class;
	private Class<? extends OAuth2AccessTokenEntity> accessTokenType = DefaultOAuth2AccessTokenEntity.class;
	private Class<? extends OAuth2RefreshTokenEntity> refreshTokenType = DefaultOAuth2RefreshTokenEntity.class;
	private Class<? extends RegisteredClient> regClientType = DefaultRegisteredClient.class;
	private Class<? extends SystemScope> sysScopeType = DefaultSystemScope.class;
	
	private ModelFactory() {
		
	}
	
	public static ModelFactory instance() {
		return factory;
	}
	
	@SuppressWarnings("unchecked")
	public void setClientDetailsType(String type) {
		try {
			Class<?> localType = Class.forName(type);
			setClientDetailsType((Class<? extends ClientDetailsEntity>)localType);
		} catch (Throwable ex) {
			throw new RuntimeException("failed while setting class", ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setAccessTokenType(String type) {
		try {
			Class<?> localType = Class.forName(type);
			setAccessTokenType((Class<? extends OAuth2AccessTokenEntity>)localType);
		} catch (Throwable ex) {
			throw new RuntimeException("failed while setting class", ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setRefreshTokenType(String type) {
		try {
			Class<?> localType = Class.forName(type);
			setRefreshTokenType((Class<? extends OAuth2RefreshTokenEntity>)localType);
		} catch (Throwable ex) {
			throw new RuntimeException("failed while setting class", ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setAuthCodeType(String type) {
		try {
			Class<?> localType = Class.forName(type);
			setAuthCodeType((Class<? extends AuthorizationCodeEntity>)localType);
		} catch (Throwable ex) {
			throw new RuntimeException("failed while setting class", ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setAuthHolderType(String type) {
		try {
			Class<?> localType = Class.forName(type);
			setAuthHolderType((Class<? extends AuthenticationHolderEntity>)localType);
		} catch (Throwable ex) {
			throw new RuntimeException("failed while setting class", ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setRegisteredClientType(String type) {
		try {
			Class<?> localType = Class.forName(type);
			setRegisteredClientType((Class<? extends RegisteredClient>)localType);
		} catch (Throwable ex) {
			throw new RuntimeException("failed while setting class", ex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setSystemScopeType(String type) {
		try {
			Class<?> localType = Class.forName(type);
			setSystemScopeType((Class<? extends SystemScope>)localType);
		} catch (Throwable ex) {
			throw new RuntimeException("failed while setting class", ex);
		}
	}
	
	public void setClientDetailsType(Class<? extends ClientDetailsEntity> type) {
		this.clientDetailsType = type;
	}
	
	public void setAccessTokenType(Class<? extends OAuth2AccessTokenEntity> type) {
		this.accessTokenType = type;
	}
	
	public void setRefreshTokenType(Class<? extends OAuth2RefreshTokenEntity> type) {
		this.refreshTokenType = type;
	}
	
	public void setAuthCodeType(Class<? extends AuthorizationCodeEntity> type) {
		this.authCodeType = type;
	}
	
	public void setAuthHolderType(Class<? extends AuthenticationHolderEntity> type) {
		this.authHolderType = type;
	}
	
	public void setRegisteredClientType(Class<? extends RegisteredClient> type) {
		this.regClientType = type;
	}
	
	public void setSystemScopeType(Class<? extends SystemScope> type) {
		this.sysScopeType = type;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends ClientDetailsEntity> T getClientDetailsInstance() {
		T instance = null;
		try {
			instance = (T)this.clientDetailsType.newInstance();
		} catch (Throwable ex) {
			throw new RuntimeException("failed to instanciate client details", ex);
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends OAuth2AccessTokenEntity> T getAccessTokenInstance() {
		T instance = null;
		try {
			instance = (T)this.accessTokenType.newInstance();
		} catch (Throwable ex) {
			throw new RuntimeException("failed to instanciate access token", ex);
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends OAuth2RefreshTokenEntity> T getRefreshTokenInstance() {
		T instance = null;
		try {
			instance = (T)this.refreshTokenType.newInstance();
		} catch (Throwable ex) {
			throw new RuntimeException("failed to instanciate refresh token", ex);
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AuthorizationCodeEntity> T getAuthCodeInstance() {
		T instance = null;
		try {
			instance = (T)this.authCodeType.newInstance();
		} catch (Throwable ex) {
			throw new RuntimeException("failed to instanciate authorization code", ex);
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends AuthenticationHolderEntity> T getAuthHolderInstance() {
		T instance = null;
		try {
			instance = (T)this.authHolderType.newInstance();
		} catch (Throwable ex) {
			throw new RuntimeException("failed to instanciate authentication holder", ex);
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends RegisteredClient> T getRegisteredClientInstance() {
		T instance = null;
		try {
			instance = (T)this.regClientType.newInstance();
		} catch (Throwable ex) {
			throw new RuntimeException("failed to instanciate registered client", ex);
		}
		return instance;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends SystemScope> T getSystemScopeInstance() {
		T instance = null;
		try {
			instance = (T)this.sysScopeType.newInstance();
		} catch (Throwable ex) {
			throw new RuntimeException("failed to instanciate system scope", ex);
		}
		return instance;
	}
	
}
