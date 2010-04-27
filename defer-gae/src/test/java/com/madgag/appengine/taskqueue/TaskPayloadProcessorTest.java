package com.madgag.appengine.taskqueue;

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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

	@Mock TaskStore taskStore;
	@Mock ObjectSerialisation objectSerialisation;
	Key key;
	@Mock Deferrable datastorePersistedDeferrable;

	private TaskPayloadProcessor processor;
	
    @Before
    public void setUp() throws Exception {
        helper.setUp();
        key=KeyFactory.createKey("MyKey", "MyKeyPath");
        when(taskStore.getTask(key)).thenReturn(datastorePersistedDeferrable);
        processor = new TaskPayloadProcessor(taskStore);
    }

    @After
    public void tearDown() {
        helper.tearDown();
    }
	
	@Test
	public void shouldDeleteKeyAfterSuccesfulExecutionOfTaskWithPersistedLoad() throws Exception {
		processor.processPayload(key);
		
		verify(taskStore).deleteEntity(key);
	}
    
	@Test
	public void shouldAllowExceptionToBubbleUpAndNotDeleteKeyIfTaskFailsAndShouldBeRepeated() throws Exception {
		RuntimeException toBeThrown = new RuntimeException();
		doThrow(toBeThrown).when(datastorePersistedDeferrable).run();
		
		try {
			processor.processPayload(key);
			fail("Should throw an exception");
		} catch (RuntimeException e) {
			assertThat(e, equalTo(toBeThrown));
			verify(taskStore,never()).deleteEntity(key);
		}
	}
	
	@Test
	public void shouldDeleteKeyAndNotDieIfTaskFailsWithAPermanentTaskFailure() throws Exception {
		doThrow(new PermanentTaskFailure("Boo")).when(datastorePersistedDeferrable).run();
		
		processor.processPayload(key);
		
		verify(taskStore).deleteEntity(key);
	}
}
