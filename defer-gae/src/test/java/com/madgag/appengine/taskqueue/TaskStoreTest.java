package com.madgag.appengine.taskqueue;

import static com.madgag.appengine.taskqueue.TaskStore.ENTITY_KIND;
import static com.madgag.appengine.taskqueue.TaskStore.TASK_PROPERTY;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

@RunWith(MockitoJUnitRunner.class)
public class TaskStoreTest {
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

	@Mock ObjectSerialisation objectSerialisation;
	@Mock DatastoreService datastoreService;
	Entity entity;
	Key key;
	TaskStore taskStore;

	
    @Before
    public void setUp() throws Exception {
        helper.setUp();
        key=KeyFactory.createKey("MyKey", "MyKeyPath");
        entity = new Entity( ENTITY_KIND );
        taskStore = new TaskStore(datastoreService, objectSerialisation);
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }
	
	@Test
	public void shouldDeleteKeyIfDeserialisationFails() throws Exception {
		when(datastoreService.get(key)).thenReturn(entity);
		entity.setProperty(TASK_PROPERTY, new Blob(new byte[0]));
		when(objectSerialisation.deserialize(any(byte[].class))).thenReturn(null);
		
		taskStore.getTask(key);
		
		verify(datastoreService).delete(key);
	}
}
