package com.madgag.defer.appengine;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.labs.taskqueue.QueueFailureException;

class ObjectSerialisation {
    
	private static final Logger log = Logger.getLogger( TaskStore.class.getName() );
	
    private static boolean isDevelopment() {
    	return true;
//        return ( SystemProperty.environment.value() ==
//                    SystemProperty.Environment.Value.Development );
    }
    
    /**
     * Deserialize an object from a byte array. Does not throw any exceptions;
     * instead, exceptions are logged and null is returned.
     * 
     * @param bytesIn A byte array containing a previously serialized object.
     * @return An object instance, or null if an exception occurred.
     */
    public Object deserialize( byte[] bytesIn ) {
        ObjectInputStream objectIn = null;
        try {
            if ( isDevelopment() ) { // workaround for issue #2097
                bytesIn = decodeBase64( bytesIn );
            }
            objectIn = new ObjectInputStream( new BufferedInputStream(
                                        new ByteArrayInputStream( bytesIn ) ) );
            return objectIn.readObject();
        } catch ( Exception e ) {
            log.log( Level.SEVERE, "Error deserializing task", e );
            return null; // don't retry task
        } finally {
            try {
                if ( objectIn != null ) {
                    objectIn.close();
                }
            } catch ( IOException ignore ) {
            }
        }
    }
    
    
    /**
     * Serialize an object into a byte array.
     * 
     * @param obj An object to be serialized.
     * @return A byte array containing the serialized object
     * @throws QueueFailureException If an I/O error occurs during the
     * serialization process.
     */
    public byte[] serialize( Object obj ) {
        try {
            ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream( 
                                                new BufferedOutputStream( bytesOut ) );
            objectOut.writeObject( obj );
            objectOut.close();
            if ( isDevelopment() ) { // workaround for issue #2097
                return encodeBase64( bytesOut.toByteArray() );
            }
            return bytesOut.toByteArray();
        } catch ( IOException e ) {
            throw new QueueFailureException( e );
        }
    }
}
