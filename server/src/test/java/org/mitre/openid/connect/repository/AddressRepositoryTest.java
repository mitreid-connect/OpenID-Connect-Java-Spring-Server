package org.mitre.openid.connect.repository;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.model.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/spring/application-context.xml", "classpath:test-context.xml"})
public class AddressRepositoryTest {
	
    @Autowired
    private AddressRepository repository;
    
    @PersistenceContext
    private EntityManager sharedManager;  
    
    @Before
    public void setup() {
    	
    }
    
    @Test
    @Rollback
    public void save_validNew() {
    	// See: http://openid.net/specs/openid-connect-basic-1_0.html#address_claim
        Address newAddress = new Address();
        newAddress.setStreetAddress("P.O. Box 517, 8158 Elementum Rd.");
        newAddress.setLocality("Whittier");
        newAddress.setRegion("YT");
        newAddress.setPostalCode("U6Q 3F2");
        newAddress.setCountry("Cyprus");

        Address saved = repository.save(newAddress);
        sharedManager.flush();

        assertThat(saved, is(sameInstance(newAddress)));
        assertThat(saved.getId(), not(nullValue()));
    }

}
