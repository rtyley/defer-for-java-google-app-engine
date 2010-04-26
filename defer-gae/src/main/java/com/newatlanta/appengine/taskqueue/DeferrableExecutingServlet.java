/*
 * Copyright 2009-2010 New Atlanta Communications, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.newatlanta.appengine.taskqueue;

import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.labs.taskqueue.TaskOptions;
import com.google.inject.Inject;

/**
 * Implements background tasks for
 * <a href="http://code.google.com/appengine/docs/java/overview.html">Google App
 * Engine for Java</a>, based on the
 * <a href="http://code.google.com/appengine/articles/deferred.html">Python 'deferred'
 * library</a>; simplifies use of the <a href="http://code.google.com/appengine/docs/java/taskqueue/overview.html">
 * Task Queue Java API</a> by automatically handling the serialization and
 * deserializtion of complex task arguments.
 * 
 * <p>Background tasks are implemented via the {@link Deferrable Deferrable}
 * interface; task logic is implemented in the {@link Deferrable#doTask() doTask()}.
 * Background tasks are queued for execution via the {@link DeferrableExecutingServlet#defer(Deferrable)
 * defer()} method and its overrides. For example:
 * <pre>
 * MyTask task = new MyTask(); // implements Deferrable
 * Deferred.defer( task );
 * </pre>
 * 
 * <p>{@link Deferrable Deferrable} task instances are serialized in order to be
 * queued for execution. If the serialized task size exceeds 10KB, it is saved to
 * the datastore and then removed prior to task execution.
 * 
 * <p><b>Configuration</b>
 * <p>There are several configuration steps that must be completed before
 * background tasks can be executed. First, the deferred task handler (this
 * servlet) needs to be configured within <code>web.xml</code>. There are two
 * optional init parameters that are discussed below; following is the minimal
 * configuration--without init parameters--required when using the default queue
 * name and default task URL:
 * <pre>
 * &lt;servlet>
 *     &lt;servlet-name>Deferred&lt;/servlet-name>
 *     &lt;servlet-class>com.newatlanta.appengine.taskqueue.Deferred&lt;/servlet-class>
 * &lt;/servlet>
 * &lt;servlet-mapping>
 *     &lt;servlet-name>Deferred&lt;/servlet-name>
 *     &lt;url-pattern>/_ah/queue/deferred&lt;/url-pattern>
 * &lt;/servlet-mapping>
 * </pre>
 * 
 * <p>The optional init parameters are <code>queueName</code> and
 * <code>taskUrl</code>. Note that if any init parameters are specified, the
 * <code>&lt;load-on-startup></code> element <b>must</b> also be specified.
 * 
 * <p>In the following example, only the <code>queueName</code>
 * is specified; note that the <code>&lt;url-pattern></code> element has also
 * been modified accordingly:
 * <pre>
 * &lt;servlet>
 *     &lt;servlet-name>Deferred&lt;/servlet-name>
 *     &lt;servlet-class>com.newatlanta.appengine.taskqueue.Deferred&lt;/servlet-class>
 *     &lt;init-param>
 *          &lt;param-name>queueName&lt;/param-name>
 *          &lt;param-value>background&lt;/param-value>
 *     &lt;/init-param>
 *     &lt;load-on-startup>1&lt;/load-on-startup>
 * &lt;/servlet>
 * &lt;servlet-mapping>
 *     &lt;servlet-name>Deferred&lt;/servlet-name>
 *     &lt;url-pattern>/_ah/queue/background&lt;/url-pattern>
 * &lt;/servlet-mapping>
 * </pre>
 * 
 * <p>In the following example, both the <code>queueName</code> and
 * <code>taskUrl</code> init parameters are specified; note that the
 * <code>&lt;url-pattern></code> element has been modified to match the
 * <code>taskUrl</code>:
 * <pre>
 * &lt;servlet>
 *     &lt;servlet-name>Deferred&lt;/servlet-name>
 *     &lt;servlet-class>com.newatlanta.appengine.taskqueue.Deferred&lt;/servlet-class>
 *     &lt;init-param>
 *          &lt;param-name>queueName&lt;/param-name>
 *          &lt;param-value>background&lt;/param-value>
 *     &lt;/init-param>
 *     &lt;init-param>
 *          &lt;param-name>taskUrl&lt;/param-name>
 *          &lt;param-value>/worker/deferred&lt;/param-value>
 *     &lt;/init-param>
 *     &lt;load-on-startup>1&lt;/load-on-startup>
 * &lt;/servlet>
 * &lt;servlet-mapping>
 *     &lt;servlet-name>Deferred&lt;/servlet-name>
 *     &lt;url-pattern>/worker/deferred&lt;/url-pattern>
 * &lt;/servlet-mapping>
 * </pre>
 * 
 * <p>Note that if you plan to specify the task URL via the task options
 * parameter to the {@link #defer(Deferrable, TaskOptions) defer()} method, you
 * must configure the task URL within a <code>&lt;url-pattern></code> element.
 * 
 * <p>After configuring <code>web.xml</code>, the queue name must be configured
 * within <code>queue.xml</code> (use whatever rate you want):
 * <pre>
 * &lt;queue>
 *     &lt;name>deferred&lt;/name>
 *     &lt;rate>10/s&lt;/rate>
 * &lt;/queue>
 * </pre>
 *    
 * @author <a href="mailto:vbonfanti@gmail.com">Vince Bonfanti</a>
 */
@SuppressWarnings("serial")
public class DeferrableExecutingServlet extends HttpServlet {
    
    private static final Logger log = Logger.getLogger( DeferrableExecutingServlet.class.getName() );

    private final TaskStore taskStore;
    private final ObjectSerialisation objectSerialisation;
    
	@Inject
	public DeferrableExecutingServlet(TaskStore taskStore, ObjectSerialisation objectSerialisation) {
		this.taskStore = taskStore;
		this.objectSerialisation = objectSerialisation;
		
	}
    
    /**
     * Executes a background task.
     * 
     * The task payload is either type Deferrable or Key; in the latter case,
     * retrieve (then delete) the Deferrable instance from the datastore.
     */
    @Override
    public void doPost( HttpServletRequest req, HttpServletResponse res )
            throws ServletException, IOException {
        try {
            Object payload = deserialize( req );
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


    /**
     * Deserialize an object from an HttpServletRequest input stream. Does not
     * throw any exceptions; instead, exceptions are logged and null is returned.
     * 
     * @param req An HttpServletRequest that contains a serialized object.
     * @return An object instance, or null if an exception occurred.
     */
    public Object deserialize( HttpServletRequest req ) {
        if ( req.getContentLength() == 0 ) {
            log.severe( "request content length is 0" );
            return null;
        }
        try {
            byte[] bytesIn = new byte[ req.getContentLength() ];
            req.getInputStream().readLine( bytesIn, 0, bytesIn.length );
            return objectSerialisation.deserialize( bytesIn );
        } catch ( IOException e ) {
            log.log(SEVERE, "Error deserializing task", e );
            return null; // don't retry task
        }
    }
    

}
