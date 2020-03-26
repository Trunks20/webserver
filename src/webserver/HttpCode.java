package webserver;

/**
 * Enumeration of all the http codes supported.
 * 
 * @author Ángel Miguélez Millos
 */
public enum HttpCode {
    OK(200, ""), 
	NOT_MODIFIED(304, "The requested page has not been modified."),
    BAD_REQUEST(400, "Your client has issued a malformed or illegal request."),
	FORBIDDEN(403, "You don't have permission to view this resource."), 
    NOT_FOUND(404, "The requested URL was not found on this server."), 
    NOT_IMPLEMENTED(501, "The method or operation is not implemented."),
    HTTP_VERSION_NOT_SUPPORTED(505, "The server does not support the HTTP protocol "
			+ "version that was used in the request message.");
    
    private final int code;
    private final String message;
	
	/**
	 * Creates a http code with a code and a message.
	 * 
	 * @param code number of the code
	 * @param message message of the code
	 */
    private HttpCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    
	/**
	 * Gets the number of the code.
	 * @return the number of the code
	 */
    public int getCode() { return code; }
	
	/**
	 * Gets the message of the code.
	 * 
	 * @return the message of the code
	 */
    public String getMessage() { return message; }
	
	/**
	 * Gets the code, name and message formatted to an html message.
	 * 
	 * @return code formatted to html
	 */
	public String getHtmlFormat() {
		return "<html><body>"
				+ "<p><h1> Error " + getCode() + "</h1></p>"
                + "<p><h3> " + name() + "</h3></p>"
                + "<p><h5> " + getMessage() + "</h5></p>"
				+ "</body></html>";
	}
}
