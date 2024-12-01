package de.m_marvin.holostruct.client.levelbound.access;

/**
 * This exception is thrown by {@link IRemoteLevelAccessor} implementations if the requested action is not possible because of access restrictions.
 * @author Marvin Koehler
 */
public class AccessDeniedException extends RuntimeException {
	
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
