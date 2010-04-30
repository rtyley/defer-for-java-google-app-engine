package com.madgag.appengine.taskqueue;

import static com.google.appengine.api.labs.taskqueue.QueueConstants.maxTaskSizeBytes;
import static com.google.appengine.api.labs.taskqueue.QueueFactory.getQueue;
import static com.google.appengine.api.labs.taskqueue.TaskOptions.Method.POST;

import java.util.logging.Logger;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.labs.taskqueue.QueueFailureException;
import com.google.appengine.api.labs.taskqueue.TaskHandle;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.inject.Inject;

public class TaskQueueDeferrer implements Deferrer<TaskHandle> {

    private static final String TASK_CONTENT_TYPE = "application/x-java-serialized-object";
	private static final Logger log = Logger.getLogger( TaskQueueDeferrer.class.getName() );
    
	private final TaskStore taskStore;
	private final ObjectSerialisation objectSerialisation;
	private final String defaultQueueName="deferred";
	
	@Inject
	public TaskQueueDeferrer(TaskStore taskStore, ObjectSerialisation objectSerialisation) {
		this.taskStore = taskStore;
		this.objectSerialisation = objectSerialisation;
		
	}
	
	
    /**
     * Queues a task for background execution using the configured or default
     * queue name and the configured or default task URL.
     * 
     * <p>If the queue name is not configured via the <code>queueName</code>
     * init parameter, uses "deferred" as the queue name.
     * 
     * <p>If the task URL is not configured via the <code>taskUrl</code> init
     * parameter, uses the default task URL, which takes the form:
     * <blockquote>
     * <code>/_ah/queue/<i>&lt;queue name></i></code>
     * </blockquote>
     * 
     * @param task The task to be executed.
     * @throws QueueFailureException If an error occurs serializing the task.
     * @return A {@link TaskHandle} for the queued task.
     */
    public TaskHandle defer( Deferrable task ) {
        return defer( task, defaultQueueName );
    }
    
    /**
     * Queue a task for background execution using the specified queue name and
     * the specified task options (including the specified task URL).
     * 
     * <p>If the task URL is not specified in the task options, the
     * default task URL is used, even if a task URL is configured via the
     * <code>taskUrl</code> init parameter. The default task URL takes the form:
     * <blockquote>
     * <code>/_ah/queue/<i>&lt;queue name></i></code>
     * </blockquote>
     * 
     * <p>The following task options may be specified:
     * <ul>
     * <li><code>countdownMillis</code></li>
     * <li><code>etaMillis</code></li>
     * <li><code>taskName</code></li>
     * <li><code>url</code></li>
     * </ul>
     * 
     * <p>The following task options are ignored:
     * <ul>
     * <li><code>header</code></li>
     * <li><code>headers</code></li>
     * <li><code>method</code></li>
     * <li><code>payload</code></li>
     * </ul>
     * 
     * <p>The following task options will throw an {@link IllegalArgumentException}
     * if specified:
     * <ul>
     * <li><code>param</code></li>
     * </ul>
     * 
     * @param task The task to be executed.
     * @param taskOptions The task options.
     * @throws QueueFailureException If an error occurs serializing the task.
     * @throws IllegalArgumentException If any <code>param</code> task options
     * are specified.
     * @return A {@link TaskHandle} for the queued task.
     */
    public TaskHandle defer( Deferrable task, String queueName ) {
        // See issue #2461 (http://code.google.com/p/googleappengine/issues/detail?id=2461).
        // If this issue is ever resolved, the params should be removed from the TaskOptions.
    	TaskOptions taskOptions = TaskOptions.Builder.withDefaults();
        byte[] taskBytes = objectSerialisation.serialize( task );
        if ( taskBytes.length <= maxTaskSizeBytes() ) {
            try {
                return queueTask( taskBytes, queueName, taskOptions );
            } catch ( IllegalArgumentException e ) {
                log.warning( e.getMessage() + ": " + taskBytes.length );
                // task size too large, fall through
            }
        }
        log.info("Deferring task to '"+queueName+"' queue - "+taskBytes.length);
        Key key = taskStore.store(taskBytes);
        try {
			return queueTask( objectSerialisation.serialize( key ), queueName, taskOptions );
        } catch ( RuntimeException e ) {
            taskStore.deleteEntity( key ); // delete entity if error queuing task
            throw e;
        }
    }


    /**
     * Add a task to the queue.
     * 
     * @param taskBytes The task payload.
     * @param queueName The queue name.
     * @param taskOptions The task options.
     * @return
     */
    private static TaskHandle queueTask( byte[] taskBytes, String queueName, TaskOptions taskOptions ) {
        TaskOptions taskPayload = taskOptions.method(POST).payload(taskBytes, TASK_CONTENT_TYPE );
		return getQueue( queueName ).add( taskPayload );
    }
    
}
