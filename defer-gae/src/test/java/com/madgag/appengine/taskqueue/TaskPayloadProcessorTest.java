package com.madgag.appengine.taskqueue;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;


@RunWith(MockitoJUnitRunner.class)
public class TaskPayloadProcessorTest {

	
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @Before
    public void setUp() {
        helper.setUp();
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }
	
	@Mock TaskStore taskStore;
	@Mock ObjectSerialisation objectSerialisation;
	Key key=KeyFactory.createKey("MyKey", "MyKeyPath");
	@Mock Deferrable deferrableExplosion;

	@Test
	public void shouldNotDeleteKeyIfTaskFailsAndShouldBeRepeated() throws Exception {
		doThrow(new RuntimeException()).when(deferrableExplosion).run();
		when(taskStore.getTask(key)).thenReturn(deferrableExplosion);
		
		TaskPayloadProcessor processor = new TaskPayloadProcessor(taskStore);
		
		processor.processPayload(key);
		
		verify(taskStore,never()).deleteEntity(key);
		
	}
}
