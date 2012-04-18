package org.mitre.openid.connect.repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mitre.openid.connect.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;

/**
 * EventRepository unit test
 * 
 * @author Michael Joseph Walsh
 * 
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/spring-servlet.xml", "classpath:test-context.xml"})
public class EventRepositoryTest {
	
    @Autowired
    private EventRepository repository;
    
    @PersistenceContext
    private EntityManager sharedManager;  
    
    private Event event1;
    private Event event2;
    
    @Before
    public void setup() {
    	//Use existing test-data.sql
    	event1 = new Event();
    	event1.setId(1L);
    	event1.setType(Event.EventType.LOGIN);
    	event1.setTimestamp(new Date(86400000*5)); // 1 day = 86 400 000 milliseconds
 	
    	event2 = new Event();
    	event2.setId(2L);
    	event2.setType(Event.EventType.AUTHORIZATION);
    	event2.setTimestamp(new Date(86400000*10));
    }
    
    @Test
    public void getById_valid() {
            Event retrieved = repository.getById(1L);
            assertThat(retrieved, is(not(nullValue())));
            assertThat(retrieved.getId(), equalTo(event1.getId()));
    }
    
    @Test
    public void getById_invalid() {
            Event nullAddress = repository.getById(42L);
            assertThat(nullAddress, is(nullValue()));
    }
    
    @Test
    public void getEventsDuringPeriod() {
    	List<Event> allEvents = Lists.newArrayList(event1, event2);
    	
    	List<Event> retrieved = (List<Event>) repository.getEventsDuringPeriod(new Date(0L), new Date(86400000*11), 0, 10);
    	
    	if (allEvents.size() != retrieved.size()) {
            fail("Retrieved and expected are not of equal size!");
    	}
    }
    
    @Test
    @Rollback
    public void save_validNew() {

        Event newEvent = new Event();
        newEvent.setType(Event.EventType.LOGIN);
        newEvent.setTimestamp(new Date());
        
        Event saved = repository.save(newEvent);
        sharedManager.flush();

        assertThat(saved, is(sameInstance(newEvent)));
        assertThat(saved.getId(), not(nullValue()));
    }

    @Test
    @Rollback
    public void save_validExisting() {
            event1.setType(Event.EventType.ACCESS);
            
            Event saved = repository.save(event1);
            
            assertThat(saved, not(nullValue()));
            assertThat(saved.getId(), equalTo(event1.getId()));
            assertThat(saved.getType(), equalTo(event1.getType()));
    }
    
    @Test
    @Rollback
    public void remove_valid() {
            
            Event managed = repository.getById((event1.getId()));
            
            repository.remove(managed);
            
            Event nullAddress = repository.getById(event1.getId());
            
            assertThat(nullAddress, is(nullValue()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void remove_invalid() {
            Event doesNotExist = new Event();
            doesNotExist.setId(42L);
            
            repository.remove(doesNotExist);
    }
    
    @Test
    @Rollback
    public void removeById_valid() {
            repository.removeById(event1.getId());
            
            Event nullagg = repository.getById(event1.getId());
            
            assertThat(nullagg, is(nullValue()));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void removeById_invalid() {
            
            repository.removeById(42L);
    }    

}