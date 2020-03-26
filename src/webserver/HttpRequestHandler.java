package webserver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Processes an http message.
 * 
 * @author Ángel Miguélez Millos
 */
public class HttpRequestHandler {
	
	private final String SERVER = "Apache/2.4.29";
    private final String DEFAULT_DIR;	// path to the resources directory
    private final String DEFAULT_FILE;	// default file
	private final boolean ALLOW;		// allow flag
	
    private final OutputStream sOut;
    private final PrintWriter out;
    
    private boolean sendBody=true;
    
	// exit status
	private HttpCode code;
	private String requestLine;
	private Date date;
    private File file;
	private String messageOut;
    
	/**
	 * Creates a new HttpRequestHandler.
	 * 
	 * @param sOut stream to send the response to the client
	 * @param resources default resource directory path
	 * @param defaultFile default resource file
	 * @param allow allow value
	 */
    public HttpRequestHandler(OutputStream sOut, String resources, String defaultFile, boolean allow) {
        this.sOut = sOut;
        out = new PrintWriter(sOut, true);
		DEFAULT_DIR = resources;
		DEFAULT_FILE = defaultFile;
		ALLOW = allow;
    }
    
	/**
	 * Gets the response http code.
	 * @return the response http code
	 */
	public HttpCode getCode() { return code; }
	
	/**
	 * Gets the request http line.
	 * @return the request http line
	 */
	public String getRequest() { return requestLine; }
	
	/**
	 * Gets the date of the response.
	 * @return the date of the response
	 */
	public Date getDate() { return date; }
	
	/**
	 * Gets the file sent as response.
	 * @return the file sent as response. If a message was sent, the file is null.
	 */
	public File getFile() { return file; }
	
	/**
	 * Gets the message sent as response.
	 * @return the response http code. If a file was sent, the message is empty.
	 */
	public String getMessage() { return messageOut; }
	
	/**
	 * Splits the message following the http request structure, process it and
	 * returns an http response.
	 * 
	 * @param message request message
	 * @return http code indicating the exit status of the request
	 */
    public HttpCode processMessage(String message) {   

		// Get current time
		date = new Date();
		
        // Process the request line
		int i = message.indexOf("\n");
        if ((code = processRequestLine(message.substring(0, i))) != HttpCode.OK)
			messageOut = code.getHtmlFormat();  // error message

		// Process the header lines
		else 
			code = processHeader(message.substring(i, message.length()));
			// OK or NOT_MODIFIED -> there is no message to send
		
		// Send the request info response
		sendResponseHeader();
		
		// Send the file request or an html message
		if (sendBody) {
			if (code != HttpCode.OK)
				sendMessage(code.getHtmlFormat());
			else if (sendBody && file != null)
				sendFile(file);
			else  // dynamic response
				sendMessage(messageOut);
			
		}
				
		// Close the steam
        out.close();     
		
		return code;
    }
    
	/**
	 * Checks the method, file requested and http version.
	 * 
	 * @param request request line
	 * @return HttpCode indicating the success or failure of the operation
	 */
    private HttpCode processRequestLine(String request) {   
		
		// Save the request
		requestLine = request;
		
		// Split the line into the different fields
        String[] tokens = request.split(" ");
        
        // Check the request format
        if (tokens.length != 3)
            return HttpCode.BAD_REQUEST;
		
        // Check the method	
		String method = tokens[0];
		
		if (method.equals("POST") || method.equals("PUT"))
			return HttpCode.NOT_IMPLEMENTED;
		
		if (!method.equals("GET")) {
			if (!method.equals("HEAD"))
				return HttpCode.BAD_REQUEST;  // unkown method
			sendBody = false;
		}

		// Check the version
		String version = tokens[2];
		
        if (!version.equals("HTTP/1.0") && !version.equals("HTTP/1.1"))
            return HttpCode.HTTP_VERSION_NOT_SUPPORTED;
		
		// Check the file
		String filename = tokens[1];
        return manageFileRequest(filename);
    }
    
	/**
	 * Manages the file requested, a normal file, a dynamic request or a directory.
	 * 
	 * @param f filepath relative to the resources directory
	 * @return HttpCode indicating success or failure to access the file
	 * @throws SecurityException If a security manager exists and its 
	 * SecurityManager.checkRead(java.lang.String) method denies read access to the file.
	 */
	private HttpCode manageFileRequest(String f) {	
		
		// Process a dynamic request
		if (f.contains(".do"))
			return manageDynRequest(f);
		
		// Find what file is requested
		file = ServerUtils.getFile(DEFAULT_DIR, f);

		// File not found
		if (file == null)
			return HttpCode.NOT_FOUND;

		// Directory requested
		if (file.isDirectory()) {
			
			// Try to get the default file into the dir
			String relativePath = f + DEFAULT_FILE;	
			file = ServerUtils.getFile(DEFAULT_DIR, relativePath);
					
			// Default file not found
			if (file == null) {

				// Show recursively the content of the dir
				if (ALLOW)
					messageOut = ServerUtils.getHtmlIndex(DEFAULT_DIR, f.substring(1));
				else
					return HttpCode.FORBIDDEN;
			}
				
		}
		
		return HttpCode.OK;
	}
	
	/**
	 * Gets dynamic content from a java class file.
	 * 
	 * @param f the path to the java class file
	 * @return NOT_FOUND if the class does not exist, OK otherwise
	 */
	private HttpCode manageDynRequest(String f) {

		// Get the path to the file without the extension
		String doFile = f.substring(0, f.indexOf(".do"));

		// Get the .do filename
		doFile = doFile.substring(doFile.lastIndexOf('/')+1);
		
		try {			
			// Get the parameters
			String args = f.substring(f.indexOf('?')+1);
			
			// Encapsulate each parameter
			Map<String,String> param = new HashMap<>();
			
			for (String p : args.split("&")) {
				String[] split = p.split("=");
				
				String key = split[0];
				String value = (split.length == 1 ? "" : split[1]);
				
				param.put(key, value);
			}
			
			// Dynamic responses that need to know the root path
			if (doFile.equals("MiServletSearch"))
				param.put("root", DEFAULT_DIR);
			
			// Create and call the class function
			messageOut = ServerUtils.processDynRequest("es.udc.redes.webserver." + doFile, param);
			
		} catch (ClassNotFoundException e) {
			System.out.println("Error: class not found");
			e.printStackTrace();
			return HttpCode.NOT_FOUND;
		} catch (Exception e) {
			System.out.println("Error getting the dynamic message: " + e.getMessage());
			e.printStackTrace();
		}
		
		return HttpCode.OK;
	}
	
	/**
	 * Checks for a If-Modified-Since header.
	 * 
	 * @param header header request lines
	 * @return NOT_MODIFIED if the file requested was not modified,
	 * OK otherwise
	 */
    private HttpCode processHeader(String header) {

		// dynamic message will be sent, i.e. request a dir and no default file exists
		if (file == null)
			return HttpCode.OK;
		
		// Search for If-Modified-Since line
		for (String line : header.split("\n")) {
			String[] tokens = line.split(": ");
			
			if (tokens[0].equals("If-Modified-Since"))
				return checkIfModSince(tokens);
		}

        return HttpCode.OK;
    }
	
	/**
	 * Checks if the file was modified after the since date.
	 * 
	 * @param line If-Modified-Since line
	 * @return NOT_MODIFIED if the file requested was not modified,
	 * OK otherwise
	 */
	private HttpCode checkIfModSince(String[] line) {
		
		// Get the last modified date
		Date last = new Date(file.lastModified());
		
		// Get the since date to compare
		Date since = ServerUtils.getDate(line[1], null);

		// Check if the resource was modified after that date
		if (since == null || !since.before(last)) {
			file = null;
			sendBody = false;
			return HttpCode.NOT_MODIFIED;
		}	
		
		return HttpCode.OK;
	}
	
	
	/**
	 * Sends the header of the http response to the client.
	 */
	private void sendResponseHeader() {
		
		// Print and send the necessary fields
		System.out.println("");
		System.out.println("HTTP/1.0 " + code.getCode() + " " + code.name());
		System.out.println("Date: " + ServerUtils.formatDate(date, ServerUtils.DATE_FORMAT));
		System.out.println("Server: " + SERVER);
        out.println("HTTP/1.0 " + code.getCode() + " " + code.name());
		out.println("Date: " + ServerUtils.formatDate(date, ServerUtils.DATE_FORMAT));
		out.println("Server: " + SERVER);
		
		try {
			if (file != null) {  // a file is sent
				
				// Get the last modified date
				Date d = new Date(file.lastModified());
				String lastMod = ServerUtils.formatDate(d, ServerUtils.DATE_FORMAT);
				
				// Get the MIME type
				String type = Files.probeContentType(file.toPath());
				
				System.out.println("Last-Modified: " + lastMod);
				System.out.println("Content-Type: " + type);
				System.out.println("Content-Length: " + (int) file.length());
				out.println("Last-Modified: " + lastMod);
				out.println("Content-Type: " + type);
				out.println("Content-Length: " + (int) file.length());

			} else if (messageOut != null) {  // html message
				System.out.println("Content-Length: " + messageOut.length());
				System.out.println("Content-Type: " + "text/html");
				out.println("Content-Type: " + "text/html");
				out.println("Content-Length: " + messageOut.length());
			}
			
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			
		} finally {
			System.out.println("");
			out.println();
		}
    }
	
	/**
	 * Sends the file requested to the client.
	 * 
	 * @param f file requested
	 */
    private void sendFile(File f) {
        FileInputStream inputStream = null;
        BufferedOutputStream outputStream = null;

        try {
			
            // Create input and output byte streams
            inputStream = new FileInputStream(f);
            outputStream = new BufferedOutputStream(sOut);

            // Copy input into output
			int c;
            while ((c = inputStream.read()) != -1)
                outputStream.write(c);
            outputStream.flush();
			
			System.out.println("sending file: " + f.getName());

		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			
        } finally {
			try {
				if (inputStream != null)
					inputStream.close();
				if (outputStream != null)
					outputStream.close();
			} catch (IOException e) {
				System.out.println("Error while closing stream(s): " + e.getMessage());
				e.printStackTrace();
			}
        }
    }
	
	/**
	 * Sends a html message to the client.
	 * 
	 * @param message message to send
	 */
	private void sendMessage(String message) {
		final int maxLength = 80;  // maximum amount of characters to print into the console
		
		if (message.length() > maxLength)
			System.out.println(message.substring(0, maxLength-5) + ".....");
		else
			System.out.println(message);
		out.println(message);
    }
}
