package edu.san.luc.hosts_monitoring;

/**
 * Created by sanya on 20.09.15.
 */
public class AppInitializingException extends RuntimeException {

    public AppInitializingException(String message) {
        super(message);
    }

    public AppInitializingException(String message, Throwable cause) {
        super(message, cause);
    }
}
