package com.madgag.appengine.taskqueue;

import java.util.logging.Logger;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;

public class TaskPayloadProcessor {
	
	private static final Logger log = Logger.getLogger( TaskPayloadProcessor.class.getName() );
	
	private final TaskStore taskStore;
	
	@Inject
	public TaskPayloadProcessor(TaskStore taskStore) {
		this.taskStore = taskStore;
	}
	
	void processPayload(Object payload) {
		try {
			if ( payload instanceof Key ) {
				Key key = (Key) payload;
                Deferrable task = taskStore.getTask(key);
				task.run();
				taskStore.deleteEntity(key);
            } else if ( payload instanceof Deferrable ) {
                ((Deferrable)payload).run();
            } else {
                log.severe( "invalid payload type: " + payload.getClass().getName() );
                // don't retry task
            }
        } catch ( EntityNotFoundException e ) {
            log.severe( e.toString() ); // don't retry task
        } catch ( PermanentTaskFailure e ) {
            log.severe( e.toString() ); // don't retry task
        }
	}
}
