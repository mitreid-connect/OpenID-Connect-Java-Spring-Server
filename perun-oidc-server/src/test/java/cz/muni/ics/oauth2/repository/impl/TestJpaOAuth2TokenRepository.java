package cz.muni.ics.oauth2.repository.impl;

import static org.junit.Assert.assertEquals;

import cz.muni.ics.oauth2.model.AuthenticationHolderEntity;
import cz.muni.ics.oauth2.model.OAuth2AccessTokenEntity;
import cz.muni.ics.oauth2.model.OAuth2RefreshTokenEntity;
import cz.muni.ics.oauth2.model.SavedUserAuthentication;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { TestDatabaseConfiguration.class })
@Transactional
public class TestJpaOAuth2TokenRepository {

	@Autowired
	private JpaOAuth2TokenRepository repository;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Before
	public void setUp(){
		createAccessToken("user1");
		createAccessToken("user1");
		createAccessToken("user2");
		createAccessToken("user2");
		
		createRefreshToken("user1");
		createRefreshToken("user1");
		createRefreshToken("user2");
		createRefreshToken("user2");
		createRefreshToken("user2");
	}
	
	@Test
	public void testGetAccessTokensByUserName() {
		Set<OAuth2AccessTokenEntity> tokens = repository.getAccessTokensByUserName("user1");
		assertEquals(2, tokens.size());
		assertEquals("user1", tokens.iterator().next().getAuthenticationHolder().getUserAuth().getName());
	}
	
	@Test
	public void testGetRefreshTokensByUserName() {
		Set<OAuth2RefreshTokenEntity> tokens = repository.getRefreshTokensByUserName("user2");
		assertEquals(3, tokens.size());
		assertEquals("user2", tokens.iterator().next().getAuthenticationHolder().getUserAuth().getName());
	}
	
	@Test
	public void testGetAllAccessTokens(){
		Set<OAuth2AccessTokenEntity> tokens = repository.getAllAccessTokens();
		assertEquals(4, tokens.size());
	}
	
	@Test
	public void testGetAllRefreshTokens(){
		Set<OAuth2RefreshTokenEntity> tokens = repository.getAllRefreshTokens();
		assertEquals(5, tokens.size());
	}
	
	private OAuth2AccessTokenEntity createAccessToken(String name) {
		SavedUserAuthentication userAuth = new SavedUserAuthentication();
		userAuth.setName(name);
		userAuth = entityManager.merge(userAuth);
		
		AuthenticationHolderEntity authHolder = new AuthenticationHolderEntity();
		authHolder.setUserAuth(userAuth);
		authHolder = entityManager.merge(authHolder);
	
		OAuth2AccessTokenEntity accessToken = new OAuth2AccessTokenEntity();
		accessToken.setAuthenticationHolder(authHolder);
		
		accessToken = entityManager.merge(accessToken);
		
		return accessToken;
	}
	
	private OAuth2RefreshTokenEntity createRefreshToken(String name) {
		SavedUserAuthentication userAuth = new SavedUserAuthentication();
		userAuth.setName(name);
		userAuth = entityManager.merge(userAuth);
		
		AuthenticationHolderEntity authHolder = new AuthenticationHolderEntity();
		authHolder.setUserAuth(userAuth);
		authHolder = entityManager.merge(authHolder);
	
		OAuth2RefreshTokenEntity refreshToken = new OAuth2RefreshTokenEntity();
		refreshToken.setAuthenticationHolder(authHolder);
		
		refreshToken = entityManager.merge(refreshToken);
		
		return refreshToken;
	}

}
