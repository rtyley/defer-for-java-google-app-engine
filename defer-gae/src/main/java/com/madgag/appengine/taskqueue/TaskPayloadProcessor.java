package com.madgag.appengine.taskqueue;

import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;

public class TaskPayloadProcessor {

	private static final Logger log = Logger
			.getLogger(TaskPayloadProcessor.class.getName());

	private final TaskStore taskStore;

	@Inject
	public TaskPayloadProcessor(TaskStore taskStore) {
		this.taskStore = taskStore;
	}

	void processPayload(Object payload) {
		if (payload instanceof Key) {
			Key key = (Key) payload;
			Deferrable task = taskStore.getTask(key);
			if (task!=null) {
				run(task);
				taskStore.deleteEntity(key);
			}
		} else if (payload instanceof Deferrable) {
			run((Deferrable) payload);
		} else {
			log.severe("invalid payload type: " + payload.getClass().getName());
			// don't retry task
		}
	}

	private void run(Deferrable task) {
		try {
			task.run();
		} catch (PermanentTaskFailure e) {}
	}

}
