/*******************************************************************************
 * Copyright 2012 The MITRE Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.mitre.openid.connect.repository;

import static org.hamcrest.CoreMatchers.equalTo;
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

/**
 * AddressRepository unit test
 * 
 * @author Michael Joseph Walsh
 * 
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/spring-servlet.xml", "classpath:test-context.xml"})
public class AddressRepositoryTest {
	
    @Autowired
    private AddressRepository repository;
    
    @PersistenceContext
    private EntityManager sharedManager;  
    
    private Address address1;
    private Address address2;
    
    @Before
    public void setup() {
    	//Use existing test-data.sql
    	address1 = new Address();
    	address1.setId(1L);
    	// too lazy to create formatted...
    	address1.setStreetAddress("7443 Et Road");
    	address1.setLocality("Pass Christian");
    	address1.setRegion("ID");
    	address1.setPostalCode("16183");
    	address1.setCountry("Jordan");
    	
    	address2 = new Address();
    	address2.setId(2L);    	
    	address2.setStreetAddress("P.O. Box 893, 2523 Felis Rd.");
    	address2.setLocality("New Kensington");
    	address2.setRegion("NT");
    	address2.setPostalCode("I5V 3Z7");
    	address2.setCountry("Israel");    	
    }
    
    @Test
    public void getById_valid() {
            Address retrieved = repository.getById(1L);
            assertThat(retrieved, is(not(nullValue())));
            assertThat(retrieved.getId(), equalTo(address1.getId()));
    }
    
    @Test
    public void getById_invalid() {
            Address nullAddress = repository.getById(42L);
            assertThat(nullAddress, is(nullValue()));
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

    @Test
    @Rollback
    public void save_validExisting() {
            address1.setStreetAddress("A New address");
            
            Address saved = repository.save(address1);
            
            assertThat(saved, not(nullValue()));
            assertThat(saved.getId(), equalTo(address1.getId()));
            assertThat(saved.getStreetAddress(), equalTo(address1.getStreetAddress()));
    }
    
    @Test
    @Rollback
    public void remove_valid() {
            
            Address managed = repository.getById((address1.getId()));
            
            repository.remove(managed);
            
            Address nullAddress = repository.getById(address1.getId());
            
            assertThat(nullAddress, is(nullValue()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void remove_invalid() {
            Address doesNotExist = new Address();
            doesNotExist.setId(42L);
            
            repository.remove(doesNotExist);
    }
    
    @Test
    @Rollback
    public void removeById_valid() {
            repository.removeById(address1.getId());
            
            Address nullagg = repository.getById(address1.getId());
            
            assertThat(nullagg, is(nullValue()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void removeById_invalid() {
            
            repository.removeById(42L);
    }    
}
