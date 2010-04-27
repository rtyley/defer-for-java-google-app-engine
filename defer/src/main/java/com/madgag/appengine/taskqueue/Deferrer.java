package com.madgag.appengine.taskqueue;


public interface Deferrer<Handle> {
    
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
    public Handle defer( Deferrable task );
    
    /**
     * Queues a task for background execution using the specified queue name and
     * the configured or default task URL.
     * 
     * <p>If the task URL is not configured via the <code>taskUrl</code> init
     * parameter, uses the default task URL, which takes the form:
     * <blockquote>
     * <code>/_ah/queue/<i>&lt;queue name></i></code>
     * </blockquote>
     * 
     * @param task The task to be executed.
     * @param queueName The name of the queue.
     * @throws QueueFailureException If an error occurs serializing the task.
     * @return A {@link TaskHandle} for the queued task.
     */
    public Handle defer( Deferrable task, String queueName );
    
}
