package webserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.util.Date;

/**
 * Writes the connections exit information into log files.
 * 
 * @author Ángel Miguélez Millos
 */
public class LogHandler {
		
	private final String dateFormat = "dd/MM/yyyy HH:mm:ss zzz";
	private final File accesses, errors;
	
	/**
	 * Creates a new LogHandler.
	 * 
	 * @param path directory where log files are
	 * @param accesses successful requests log filename
	 * @param errors fail requests log filename
	 * @throws FileNotFoundException If any of the log files does not exist
	 */
	public LogHandler(String path, String accesses, String errors) throws FileNotFoundException {
		this.accesses = ServerUtils.getFile(path, accesses);
		this.errors = ServerUtils.getFile(path, errors);

		if (this.accesses == null || this.errors == null)
			throw new FileNotFoundException();
	}
	
	/**
	 * Writes the exit status of a connection into a log file.
	 * 
	 * @param requestLine client request line
	 * @param ip client ip
	 * @param date date of response
	 * @param codeHttp code exit value
	 * @param f file sent
	 * @param message message sent
	 * @throws IOException If an I/O error occurs when opening or writing into
	 * the log files.
	 */
	public void addLog(String requestLine, InetAddress ip, Date date,
			HttpCode codeHttp, File f, String message) throws IOException {
		
		File logFile;
		int code = codeHttp.getCode();
		
		// Choose the log file to update
		if (code >= 200 && code < 400)
			logFile = accesses;
		else
			logFile = errors;
		
		// Seek to the end of the file
		RandomAccessFile fw = new RandomAccessFile(logFile, "rwd");
		fw.seek(logFile.length());
		
		// Write the general info
		fw.writeBytes("Request=" + requestLine + '\n');
		fw.writeBytes("IP=" + ip.toString().split("/")[1] + '\n');
		fw.writeBytes("Date=" + ServerUtils.formatDate(date, dateFormat) + '\n');
		
		// Write into the acceses log file
		if (code >= 200 && code < 400) {
			fw.writeBytes("Code=" + code + '\n');
			fw.writeBytes("Size=");
			
			String method = requestLine.split(" ")[0];
			if (method.equals("HEAD") || code == 304)  // no body sent
				fw.writeBytes("0" + '\n');
			else if (f != null)  // file sent
				fw.writeBytes(String.valueOf(f.length()) + '\n');
			else if (message != null)  // html message
				fw.writeBytes(String.valueOf(message.length()) + '\n');
			
		// Write into the error file
		} else if (code >= 400)
			fw.writeBytes("Error=" + codeHttp.getMessage() + '\n');
		
		fw.writeBytes("\n");
		fw.close();
	}
	
}
