/**
 * 
 */
package org.mitre.openid.connect.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * @author jricher
 *
 */
@Entity
@Table(name="blacklisted_site")
@NamedQueries({
	@NamedQuery(name = "BlacklistedSite.getAll", query = "select b from BlacklistedSite b"),
})
public class BlacklistedSite {

    // unique id
    private Long id;
    
    // URI pattern to black list
    private String uri;
    
    public BlacklistedSite() {
    	
    }
    
	/**
     * @return the id
     */
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
    	return id;
    }

	/**
     * @param id the id to set
     */
    public void setId(Long id) {
    	this.id = id;
    }

    @Basic
    @Column(name="uri")
    public String getUri() {
    	return uri;
    }

    public void setUri(String uri) {
    	this.uri = uri;
    }

    
}
