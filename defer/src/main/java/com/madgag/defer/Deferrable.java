package com.madgag.defer;

import java.io.Serializable;

/**
 * The <code>Deferrable</code> interface should be implemented by any class
 * whose instances are intended to be executed as background tasks.
 */
public interface Deferrable extends Serializable, Runnable {

}