package com.madgag.appengine.taskqueue;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.util.logging.Logger;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;

class TaskStore {

	private static final Logger log = Logger.getLogger( TaskStore.class.getName() );
	
	static final String ENTITY_KIND = Deferrable.class.getName();
    static final String TASK_PROPERTY = "taskBytes";
    
    private final ObjectSerialisation objectSerialisation;
    private final DatastoreService datastoreService;
    
    @Inject
    public TaskStore(DatastoreService datastoreService, ObjectSerialisation objectSerialisation) {
		this.datastoreService = datastoreService;
		this.objectSerialisation = objectSerialisation;
    }

	Key store(byte[] taskBytes) {
		// create a datastore entity and add its key as the task payload
        Entity entity = new Entity( ENTITY_KIND );
        entity.setProperty( TASK_PROPERTY, new Blob( taskBytes ) );
        Key key = datastoreService.put( entity );
        log.log(FINE, "put datastore key: " + key );
		return key;
	}
	
    /**
     * Delete a datastore entity.
     * 
     * @param key The key of the entity to delete.
     */
    public void deleteEntity( Key key ) {
        try {
        	datastoreService.delete( key );
            log.log(FINE, "deleted datastore key: " + key );
        } catch ( DatastoreFailureException e ) {
            log.log(WARNING, "failed to delete datastore key: " + key, e );
        }
    }
    

	Deferrable getTask(Key taskKey) {
		try {
			Entity entity = datastoreService.get(taskKey);
			Blob taskBlob = (Blob) entity.getProperty( TASK_PROPERTY );
			Deferrable deferrable=null;
			if ( taskBlob != null ) {
				deferrable = (Deferrable) objectSerialisation.deserialize( taskBlob.getBytes() );
			}
		    if (deferrable==null) {
		    	deleteEntity(taskKey);
		    }
			return deferrable;
		} catch (EntityNotFoundException e) {
			log.log(SEVERE, "Couldn't find "+taskKey, e);
		}
		return null;
		
	}
    
   
    




}
