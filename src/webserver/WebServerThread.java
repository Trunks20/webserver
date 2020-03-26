package webserver;

import java.net.*;
import java.io.*;

/**
 * Manages a single client TCP connection.
 * 
 * @author Ángel Miguélez Millos
 */
public class WebServerThread extends Thread {

	private final String DIR_INDEX, DIR;
	private final boolean ALLOW;
    private final Socket clientSocket;
	private final LogHandler logHandler;

	/**
	 * Saves the main server configuration and the client connection.
	 * 
	 * @param handler accesses and errors log handler
	 * @param s socket of the client
	 * @param defaultDir path to the resources directory
	 * @param defaultFile default filename when access to a directory
	 * @param allow allow value
	 */
    public WebServerThread(LogHandler handler, Socket s, String defaultDir, String defaultFile, boolean allow) {
		DIR_INDEX = defaultDir;
		DIR = defaultFile;
		ALLOW = allow;
		logHandler = handler;
		clientSocket = s;
    }

	/**
	 * Processes a message and updates the logs with the request exit status.
	 */
    public void run() {
        try {
            // Set the input channel
            BufferedReader sInput = new BufferedReader(new InputStreamReader(
                    clientSocket.getInputStream()));

            // Set the output channel
            OutputStream sOutput = clientSocket.getOutputStream();
			
            // Receive the message from the client
			String message = ServerUtils.readInput(sInput);
			
			// Process the message (if valid)
			if (!message.isEmpty()) {
				
				System.out.println("SERVER: Received message");
				System.out.println();
				System.out.print(message);
				
				// Create a handler to manage the request
				HttpRequestHandler handler = new HttpRequestHandler(sOutput, DIR_INDEX, DIR, ALLOW);
				
				// Process the request
				handler.processMessage(message);
								
				// Write into a log file the connection exit status
				logHandler.addLog(handler.getRequest(), 
						ServerUtils.getClientIP(clientSocket), 
						handler.getDate(), 
						handler.getCode(), 
						handler.getFile(),
						handler.getMessage());
				
				System.out.println("------------------------------");
			}
            	
            // Close the streams
            sInput.close();
            sOutput.close();

        } catch (IOException e) {
			System.out.println("Error in thread " + this.getId() + ": " + e.getMessage());
			e.printStackTrace();
			
		} finally {
            try {
				if (clientSocket != null)
					clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
	
}
