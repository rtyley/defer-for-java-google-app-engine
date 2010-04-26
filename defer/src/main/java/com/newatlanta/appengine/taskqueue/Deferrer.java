/*
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
