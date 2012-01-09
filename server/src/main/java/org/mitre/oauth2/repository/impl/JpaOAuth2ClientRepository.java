package org.mitre.oauth2.repository.impl;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.oauth2.repository.OAuth2ClientRepository;
import org.mitre.util.jpa.JpaUtil;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author jricher
 *
 */
@Repository
@Transactional
public class JpaOAuth2ClientRepository implements OAuth2ClientRepository {

	@PersistenceContext
	private EntityManager manager;
	
	public JpaOAuth2ClientRepository() {
		
	}
	
	public JpaOAuth2ClientRepository(EntityManager manager) {
		this.manager = manager;
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.OAuth2ClientRepository#getClientById(java.lang.String)
	 */
	@Override
	public ClientDetailsEntity getClientById(String clientId) {
		return manager.find(ClientDetailsEntity.class, clientId);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.OAuth2ClientRepository#saveClient(org.mitre.oauth2.model.ClientDetailsEntity)
	 */
	@Override
	public ClientDetailsEntity saveClient(ClientDetailsEntity client) {
		return JpaUtil.saveOrUpdate(client.getClientId(), manager, client);
	}

	/* (non-Javadoc)
	 * @see org.mitre.oauth2.repository.OAuth2ClientRepository#deleteClient(org.mitre.oauth2.model.ClientDetailsEntity)
	 */
	@Override
	public void deleteClient(ClientDetailsEntity client) {
		ClientDetailsEntity found = getClientById(client.getClientId());
		if (found != null) {
			manager.remove(found);
		} else {
			throw new IllegalArgumentException("Client not found: " + client);
		}
	}

	@Override
    public ClientDetailsEntity updateClient(String clientId, ClientDetailsEntity client) {
	    return JpaUtil.saveOrUpdate(clientId, manager, client);
    }

	@Override
    public Collection<ClientDetailsEntity> getAllClients() {
		TypedQuery<ClientDetailsEntity> query = manager.createNamedQuery("ClientDetailsEntity.findAll", ClientDetailsEntity.class);
		return query.getResultList();
    }

}
