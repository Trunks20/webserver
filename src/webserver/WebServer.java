package webserver;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Creates and runs an http server using sockets and with the properties specified 
 * in a configuration file.
 * 
 * @author Ángel Miguélez Millos
 */
public class WebServer {
    		
	private final String WD;  // working directory
	
	private int port;
	private String dir, dir_index;	// default resources path and file
	private boolean allow;
	private String log_index;  // default log files path
	
	/**
	 * Creates a new WebServer with the parameters from a configuration file.
	 * 
	 * @param wd absolute path of the working directory, where the configuration 
	 * file must be located
	 * @throws IllegalArgumentException If the configuration file does not exist
	 * @throws IOException If a problem occurs while handling the configuration file
	 * @throws SecurityException If a security manager exists and its 
	 * SecurityManager.checkRead(java.lang.String) method denies read access to the configuration file
	 */
	public WebServer(String wd) throws IOException {
		WD = wd;

		// Check if exists the config file
		final String CONFIG = "config.properties";
		if (!Files.exists(Paths.get(WD + CONFIG)))
			throw new IllegalArgumentException("No config file found");
		
		// Load the properties
		loadProperties(WD + CONFIG);
	}

	/**
	 * Loads the default properties from a configuration file.
	 * 
	 * @param path path of the configuration file
	 */
	private void loadProperties(String path) throws IOException {
		FileInputStream input;

		// Open a stream to read the config file
		input = new FileInputStream(path);
			
		// Load the properties
		Properties prop = new Properties();
		prop.load(input);

		// Save the properties
		port = Integer.parseInt(prop.getProperty("PORT"));
		dir = WD + prop.getProperty("DIRECTORY");
		dir_index = prop.getProperty("DIRECTORY_INDEX");
		allow = prop.getProperty("ALLOW").equals("true");
		log_index = WD + prop.getProperty("LOG_INDEX");
		
		// Close the stream
		input.close();
	}
	
	/**
	 * Creates the server socket and waits for client connections.
	 * 
	 * @throws IOException If an I/O error occurs when opening the server socket
	 * or the log files.
	 */
	private void run() throws IOException {
        ServerSocket server = null;
        Socket client;

        try {		
			
            // Create a server socket
            server = new ServerSocket(port);
           
            // Set a timeout of 300 secs
            server.setSoTimeout(300000);

			// Create the log handler
			LogHandler logHandler = new LogHandler(log_index, "accesslogs.txt", "errorlogs.txt");
			
			// Working loop
            while (true) {
				
                // Wait for connections
                client = server.accept();

                // Create a WebServerThread object with the new connection
                WebServerThread serverThr = new WebServerThread(logHandler, client, dir, dir_index, allow);

                // Initiate thread using the start() method
                serverThr.start();
            }

        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid port value");
			e.printStackTrace();
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
		} catch (FileNotFoundException e) {
			System.err.println("Log files not found");
			
        } finally {
            if (server != null)
                server.close();
        }
    }
	
	/**
	 * Main method.
	 * 
	 * @param args array with the arguments (configuration file absolute path)
	 * @throws IOException If an I/O error occurs while running the server.
	 */
	public static void main(String[] args) throws IOException {
		
		// Create a server
		WebServer server = new WebServer(args[0]);
		
		// Run the server
		server.run();
	}
    
}
