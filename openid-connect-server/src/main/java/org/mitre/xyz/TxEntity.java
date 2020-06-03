package org.mitre.xyz;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.mitre.oauth2.model.AuthenticationHolderEntity;
import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.xyz.Hash.Method;
import org.mitre.xyz.TxEndpoint.Status;

/**
 * @author jricher
 *
 */
@Entity
@Table(name = "transaction")
@NamedQueries({
	@NamedQuery(name = TxEntity.QUERY_BY_HANDLE, query = "SELECT t FROM TxEntity t WHERE t.handle = :" + TxEntity.PARAM_HANDLE),
	@NamedQuery(name = TxEntity.QUERY_BY_INTERACTION, query = "SELECT t FROM TxEntity t WHERE t.interaction = :" + TxEntity.PARAM_INTERACTION)
})
public class TxEntity {

	public static final String QUERY_BY_HANDLE = "TxEntity.getByHandle";
	public static final String QUERY_BY_INTERACTION = "TxEntity.getByInteraction";

	public static final String PARAM_HANDLE = "handle";
	public static final String PARAM_INTERACTION = "interaction";

	private Long id;

	private String handle;

	private String interaction;

	private ClientDetailsEntity client;

	private Set<String> scope = new HashSet<>();

	private Status status;

	private String callbackUri;

	private String clientNonce;

	private String serverNonce;

	private String interactionRef;

	private Method hashMethod;

	private AuthenticationHolderEntity authenticationHolder;

	/**
	 * @return the id
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the scope
	 */
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(
			name="transaction_scope",
			joinColumns=@JoinColumn(name="owner_id")
			)
	@Column(name="scope")
	public Set<String> getScope() {
		return scope;
	}

	/**
	 * @param scope the set of scopes allowed to be issued to this client
	 */
	public void setScope(Set<String> scope) {
		this.scope = scope;
	}

	/**
	 * @return the clientId
	 */
	@ManyToOne
	@JoinColumn(name = "client_id")
	public ClientDetailsEntity getClient() {
		return client;
	}

	/**
	 */
	public void setClient(ClientDetailsEntity client) {
		this.client = client;
	}

	/**
	 * @return the handle
	 */
	@Basic
	@Column(name = "handle")
	public String getHandle() {
		return handle;
	}

	/**
	 * @param handle the handle to set
	 */
	public void setHandle(String handle) {
		this.handle = handle;
	}

	/**
	 * @return the interaction
	 */
	@Basic
	@Column(name = "interaction")
	public String getInteraction() {
		return interaction;
	}

	/**
	 * @param interaction the interaction to set
	 */
	public void setInteraction(String interaction) {
		this.interaction = interaction;
	}

	/**
	 * @return the status
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return the callbackUri
	 */
	@Basic
	@Column(name = "callback_uri")
	public String getCallbackUri() {
		return callbackUri;
	}

	/**
	 * @param callbackUri the callbackUri to set
	 */
	public void setCallbackUri(String callbackUri) {
		this.callbackUri = callbackUri;
	}

	/**
	 * @return the clientNonce
	 */
	@Basic
	@Column(name = "client_nonce")
	public String getClientNonce() {
		return clientNonce;
	}

	/**
	 * @param clientNonce the clientNonce to set
	 */
	public void setClientNonce(String clientNonce) {
		this.clientNonce = clientNonce;
	}

	/**
	 * @return the serverNonce
	 */
	@Basic
	@Column(name = "server_nonce")
	public String getServerNonce() {
		return serverNonce;
	}

	/**
	 * @param serverNonce the serverNonce to set
	 */
	public void setServerNonce(String serverNonce) {
		this.serverNonce = serverNonce;
	}

	/**
	 * @return the interactionRef
	 */
	@Basic
	@Column(name = "interaction_ref")
	public String getInteractionRef() {
		return interactionRef;
	}

	/**
	 * @param interactionRef the interactionRef to set
	 */
	public void setInteractionRef(String interactionRef) {
		this.interactionRef = interactionRef;
	}

	/**
	 * @return the hashMethod
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "hash_method")
	public Method getHashMethod() {
		return hashMethod;
	}

	/**
	 * @param hashMethod the hashMethod to set
	 */
	public void setHashMethod(Method hashMethod) {
		this.hashMethod = hashMethod;
	}

	/**
	 * The authentication in place when this token was created.
	 * @return the authentication
	 */
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "auth_holder_id")
	public AuthenticationHolderEntity getAuthenticationHolder() {
		return authenticationHolder;
	}

	/**
	 * @param authentication the authentication to set
	 */
	public void setAuthenticationHolder(AuthenticationHolderEntity authenticationHolder) {
		this.authenticationHolder = authenticationHolder;
	}



}
