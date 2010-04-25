package com.newatlanta.appengine.taskqueue;

import java.io.IOException;
import java.io.Serializable;

import com.newatlanta.appengine.taskqueue.Deferred.PermanentTaskFailure;

/**
 * The <code>Deferrable</code> interface should be implemented by any class
 * whose instances are intended to be executed as background tasks. The
 * implementation class must define a method with no arguments named
 * {@link Deferrable#doTask()}.
 */
public interface Deferrable extends Serializable {
    /**
     * Invoked to perform the background task.
     * 
     * @throws PermanentTaskFailure To indicate that the task should
     * <b>not</b> be retried; all other exceptions cause the task to be
     * retried. These exceptions are logged.
     * 
     * @throws ServletException To indicate that the task should be retried.
     * These exceptions are not logged.
     * 
     * @throws IOException To indicate that the task should be retried. These
     * exceptions are not logged.
     */
    public void doTask() throws ServletException, IOException;
}