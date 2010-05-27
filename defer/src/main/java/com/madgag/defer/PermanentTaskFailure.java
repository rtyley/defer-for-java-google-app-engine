package com.madgag.defer;

/**
 * If thrown by the {@link Deferrable#doTask() doTask()} method, indicates
 * that a background task should <b>not</b> be retried.
 */
@SuppressWarnings("serial")
public class PermanentTaskFailure extends RuntimeException {

    public PermanentTaskFailure( String message ) {
        super( message );
    }
}