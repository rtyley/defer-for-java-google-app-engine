package com.newatlanta.appengine.taskqueue;

import static com.google.appengine.api.datastore.DatastoreServiceFactory.getDatastoreService;

import java.util.logging.Logger;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.inject.Inject;

public class TaskStore {

	private static final Logger log = Logger.getLogger( TaskStore.class.getName() );
	
	private static final String ENTITY_KIND = Deferrable.class.getName();
    
    private static final String TASK_PROPERTY = "taskBytes";
    
    private final ObjectSerialisation objectSerialisation;
    
    @Inject
    public TaskStore(ObjectSerialisation objectSerialisation) {
		this.objectSerialisation = objectSerialisation;
    }

	Key store(byte[] taskBytes) {
		// create a datastore entity and add its key as the task payload
        Entity entity = new Entity( ENTITY_KIND );
        entity.setProperty( TASK_PROPERTY, new Blob( taskBytes ) );
        Key key = getDatastoreService().put( entity );
        log.info( "put datastore key: " + key );
		return key;
	}
	
    /**
     * Delete a datastore entity.
     * 
     * @param key The key of the entity to delete.
     */
    public void deleteEntity( Key key ) {
        try {
            getDatastoreService().delete( key );
            log.info( "deleted datastore key: " + key );
        } catch ( DatastoreFailureException e ) {
            log.warning( "failed to delete datastore key: " + key );
            log.warning( e.toString() );
        }
    }
    

	Deferrable getTask(Key taskKey) throws EntityNotFoundException {
		Blob taskBlob = (Blob) getDatastoreService().get(taskKey).getProperty( TASK_PROPERTY );
		if ( taskBlob != null ) {
		    return (Deferrable) objectSerialisation.deserialize( taskBlob.getBytes() );
		}
		return null;
	}
    
   
    




}
