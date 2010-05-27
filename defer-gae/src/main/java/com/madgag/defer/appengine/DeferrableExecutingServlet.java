package com.madgag.defer.appengine;

import static java.util.logging.Level.SEVERE;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class DeferrableExecutingServlet extends HttpServlet {
    
    private static final Logger log = Logger.getLogger( DeferrableExecutingServlet.class.getName() );

    private final TaskPayloadProcessor taskPayloadProcessor;
    private final ObjectSerialisation objectSerialisation;
    
	@Inject
	public DeferrableExecutingServlet(TaskPayloadProcessor taskPayloadProcessor, ObjectSerialisation objectSerialisation) {
		this.taskPayloadProcessor = taskPayloadProcessor;
		this.objectSerialisation = objectSerialisation;
		
	}
    
    /**
     * Executes a background task.
     * 
     * The task payload is either type Deferrable or Key; in the latter case,
     * retrieve (then delete) the Deferrable instance from the datastore.
     */
    @Override
    public void doPost( HttpServletRequest request, HttpServletResponse res ) {
        if ( request.getContentLength() == 0 ) {
            log.severe( "request content length is 0" );
            return;
        }
        
        Object payload=payloadFrom(request);
        if (payload!=null) {
        	taskPayloadProcessor.processPayload(payload);
        }
    }

	private Object payloadFrom(HttpServletRequest req) {
		try {
            byte[] bytesIn = new byte[ req.getContentLength() ];
            req.getInputStream().readLine( bytesIn, 0, bytesIn.length );
            return objectSerialisation.deserialize( bytesIn );
        } catch ( IOException e ) {
            log.log(SEVERE, "Error deserializing task", e );
        }
        return null;
	}

}
