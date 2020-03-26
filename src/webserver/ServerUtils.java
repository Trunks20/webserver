package webserver;
        
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * Contains some basic methods to access files, create dates, get IP and create
 * dynamic content.
 * 
 * @author Ángel Miguélez Millos
 */
public class ServerUtils {

	public final static String DATE_FORMAT = "E, dd MMM yyyy HH:mm:ss zzz";
	
	/**
	 * Creates an instance of the class requested and gets the dynamic response.
	 * 
	 * @param nombreclase name of the java class
	 * @param parameters parameters of the java class
	 * @return html dynamic message
	 * @throws ClassNotFoundException If the class cannot be located
	 * @throws InstantiationException If this Class represents an abstract class,
	 * an interface, an array class, a primitive type, or void; or if the class 
	 * has no nullary constructor; or if the instantiation fails for some other reason
	 * @throws IllegalAccessException If the class or its nullary constructor is not accessible
	 * @throws Exception If the class method called fails
	 */
    public static String processDynRequest(String nombreclase,
		Map<String, String> parameters) throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {

        MiniServlet servlet;  // interface
		Class<?> instancia;	  // instance of a class

		// Create an instance of the class requested
        instancia = Class.forName(nombreclase);  
		
		// Cast to the interface
		servlet = (MiniServlet) instancia.newInstance();

		// Call the function that returns the dynamic response
		return servlet.doGet(parameters);
    }
	
	/**
	 * Reads the user input until an empty line is sent.
	 * 
	 * @param in buffered to read from
	 * @return message separated by new line characters
	 * @throws IOException If an I/O error occurs while reading from the buffer
	 */
	public static String readInput(BufferedReader in) throws IOException {
        String line, message="";
        
		// Read input until an empty line is sent
        while ((line = in.readLine()) != null) {
            if (line.isEmpty()) break;
            message += line + '\n';
        }
        
        return message;
    }
	
	/**
	 * Finds and gets the file with a name from a path.
	 * 
	 * @param path directory where the file is located
	 * @param filename filename
	 * @return null if the file does not exist, the File if exists
	 * @throws IllegalArgumentException If the path or the file are null or empty
	 */
	public static File getFile(String path, String filename){
		
		// Check the path and file are valid
		if (path == null || path.isEmpty())
			throw new IllegalArgumentException("Invalid pathname");
		
		if (filename == null || filename.isEmpty())
			throw new IllegalArgumentException("Invalid filename");
		
		// avoid double '/' when join path+filename
		String p = path;
		if (!path.endsWith("/")) p = path + "/";
		
		String f = filename;
		if (filename.startsWith("/")) f = filename.substring(1);
		
		// Get the file with the absolute path
		File file = new File(p + f);

		// Check if the file exists. If the file exists as a directory, but is 
		// requested as a file return null too
		if (!file.exists() || (!filename.endsWith("/") && file.isDirectory()))
			return null;		
		
		return file;
	}
	
	/**
	 * Formats a date with a pattern given.
	 * 
	 * @param date date to format
	 * @param pattern pattern to format with. If is null, is formatted with the default one.
	 * @return the date formatted with the pattern
	 */
	public static String formatDate(Date date, String pattern) {
		SimpleDateFormat formatter;
		
		// Get the format pattern
		formatter = new SimpleDateFormat(pattern == null ? DATE_FORMAT : pattern);
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		// Format the date
		return formatter.format(date);
	}
	
	/**
	 * Gets a Date from a String, with a pattern given.
	 * 
	 * @param s string to format
	 * @param pattern pattern to format with
	 * @return the String formatted to Date
	 */
	public static Date getDate(String s, String pattern) {
		Date out=null;
		
		// Get the format pattern
		String format = (pattern == null ? DATE_FORMAT : pattern);
		
		// Create the formatter
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		try {		
			// Format the string
			out = formatter.parse(s);
			
		} catch (ParseException e) {
			out = null;
			System.out.println("Impossible to parse the date=" + s 
					+ " with the pattern=" + pattern);
			e.printStackTrace();
		}
		
		return out;
	}
	
	/**
	 * Gets the IP of a client by the socket.
	 * 
	 * @param s socked used to connect with the server
	 * @return the ip of the client
	 */
	public static InetAddress getClientIP(Socket s) {
		
		// Get the socket
		InetSocketAddress socketAddress = (InetSocketAddress)s.getRemoteSocketAddress();
		
		// Get the client address
		InetAddress clientAddress = socketAddress.getAddress();
		
		return clientAddress;
	}
	
	/**
	 * Gets a html message containing links to the files from a directory.
	 * 
	 * @param root root of the server resource directory
	 * @param path relative path of the directory to show
	 * @return a html page containing links to the files
	 */
	public static String getHtmlIndex(String root, String path) {
		StringBuilder m = new StringBuilder("");
		
		m.append("<html><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
				
				"	<head>\n" +
				"		<title> ¿A qué contenido quieres acceder?</title>\n" +
				"	</head>\n" +
				
				"	<body text=\"#000000\" bgcolor=\"#ffffff\">\n" +
				"		<div>\n" +
				"			<p><font color=\"#000099\" size=\"+2\" face=\"Arial, Helvetica, sans-serif\">\n" +
				"				<b>¿A qué contenido quieres acceder?</b></font>\n" +
				"			</p>\n" +	
				"		</div>\n" +
				
				"		<ul>\n");
		
		// Get the dir	
		File dir = new File(root, path);
		
		// Get each entry
		for (File entry : dir.listFiles()) {
			
			// Get the file or directory name
			String filename = entry.getName();
			if (entry.isDirectory())
				filename += '/';
			
			// Create a link to the resource
			m.append("			<li>\n");
			m.append("				<a href=\"").append(filename).append("\">").append(filename).append("</a>\n");
			m.append("			</li>\n");
		} 
		
		m.append("		</ul>\n" +
				"	</body>\n" +
				"</html>");
		
		return m.toString();
	}

}
