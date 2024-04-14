package de.m_marvin.holostructures.client.levelbound.access;

public class AccessDeniedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4484928444028211951L;
	
    public AccessDeniedException() {
    }
    
    public AccessDeniedException(String message) {
        super(message);
    }
    
    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public AccessDeniedException(Throwable cause) {
        super(cause);
    }
	
}
