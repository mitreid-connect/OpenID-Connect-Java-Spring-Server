package org.mitre.openid.connect.repository;

import java.util.Collection;

import org.mitre.oauth2.model.ClientDetailsEntity;
import org.mitre.openid.connect.model.ApprovedSite;

/**
 * Repository that aggregates queries used to compute stats
 * in a more efficient way.
 *
 * @author zanitete
 *
 */
public interface StatsRepository {

	/**
	 * Return the subset of ApprovedSite entity fields needed to compute stats.
	 *
	 * @return
	 */
	Collection<ApprovedSiteId> getAllApprovedSitesClientIdAndUserId();

	/**
	 * Counts the number of ApprovedSites per Client and return the count.
	 *
	 * @return
	 */
	Collection<ApprovedSitePerClientCount> getAllApprovedSitesClientIdCount();

	/**
	 * Return the subset of ClientDetailsEntity fields needed to compute stats.
	 *
	 * @return
	 */
	Collection<ClientDetailsEntityId> getAllClientIds();

	/**
	 * Result of a group by query with elements count.
	 */
	class ApprovedSitePerClientCount {

		private final String clientId;
		private final Long count;

		public ApprovedSitePerClientCount(String clientId, Long count) {
			this.clientId = clientId;
			this.count = count;
		}

		public String getClientId() {
			return clientId;
		}
		public Long getCount() {
			return count;
		}

	}

	/**
	 * Subset of {@link ApprovedSite} entity fields:
	 * <ul>
	 *   <li>id</li>
	 *   <li>clientId</li>
	 *   <li>userId</li>
	 *</ul>
	 */
	class ApprovedSiteId {
		private final Long id;
		private final String clientId;
		private final String userId;

		public ApprovedSiteId(Long id, String clientId, String userId) {
			this.id = id;
			this.clientId = clientId;
			this.userId = userId;
		}

		public Long getId() {
			return id;
		}
		public String getClientId() {
			return clientId;
		}
		public String getUserId() {
			return userId;
		}

	}

	/**
	 * Subset of {@link ClientDetailsEntity} entity fields:
	 * <ul>
	 *   <li>id</li>
	 *   <li>clientId</li>
	 *</ul>
	 */
	class ClientDetailsEntityId {
		private final Long id;
		private final String clientId;

		public ClientDetailsEntityId(Long id, String clientId) {
			this.id = id;
			this.clientId = clientId;
		}

		public Long getId() {
			return id;
		}
		public String getClientId() {
			return clientId;
		}

	}
}
