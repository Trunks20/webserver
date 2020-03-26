package webserver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implements MiniServlet creating an html message containing all the entries under
 * a directory with a specific name and extension.
 * 
 * @author Ángel Miguélez Millos
 */
public class MiServletSearch implements MiniServlet {
	
	private String nombre, extension, raiz;
	
	/**
	 * Creates a new MiServletSearch.
	 */
	public MiServletSearch() {
		
	}
	
	/**
	 * Gets the html message.
	 * 
	 * @param parameters dynamic arguments
	 * @return the html dynamic message
	 */
	@Override
	public String doGet (Map<String, String> parameters){
		nombre = parameters.get("nombre");
		extension = parameters.get("extension");
		raiz = parameters.get("root");

		return printHeader() + printBody() + printEnd();
	}	

	/**
	 * Gets the html header.
	 * 
	 * @return html header
	 */
	private String printHeader() {
		return "<html><head> <title>Busqueda realizada</title> </head> ";
	}

	/**
	 * Goes all under a path finding files with a name and extension and creates
	 * an unordered list. If none matched, gets the files only with the same name.
	 * 
	 * @return html message with the filename and the relative path
	 */
	private String printBody() {
		StringBuilder out = new StringBuilder("<body>");
		List<Path> found = new ArrayList<>();
		List<Path> sameName = new ArrayList<>();
		
		try {
			// Walk recursively down the dirs searching for the file requested
			Files.walk(Paths.get(raiz))
				.filter(p -> p.getFileName().toString().equals(nombre+extension))
				.forEach(found::add);
			
			// Walk recursively down the dirs searching for the file with the same name
			if (found.isEmpty()) {
				Files.walk(Paths.get(raiz))
					.filter(p -> p.getFileName().toString().replaceFirst("[.][^.]+$", "").equals(nombre))
					.forEach(sameName::add);
			}
				
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
			return "";
		}
		
		// File found
		if (!found.isEmpty()) {
			out.append("<h1> Archivos encontrados:</h1>");
			out.append(createLinks(found));
		
		// Files with the same name found
		} else if (sameName.size() > 0) {
			out.append("<h1> Archivos encontrados con el mismo nombre:</h1>");
			out.append(createLinks(sameName));
		
		// File not found
		} else
			out.append("<h1> Archivo no encontrado</h1>");			
		
		out.append("</body>");
		
		return out.toString();
	}

	/**
	 * Closes the html message.
	 * 
	 * @return html message close
	 */
	private String printEnd() {
		return "</html>";
	}
	
	/**
	 * Gets the relative path of the entries and creates an html list with a link
	 * to each one.
	 * 
	 * @param paths paths to the entries
	 * @return html list of links to the entries
	 */
	private String createLinks(List<Path> paths) {
		StringBuilder out = new StringBuilder();
	
		out.append("<ul>");
		for (Path entry : paths) {
				
			// Get the relative path
			String relativePath = entry.toString().split(raiz)[1];
				
			// Link the file
			out.append("<li><a href=\"").append(relativePath).append("\">")
				.append(relativePath).append("</a></li>");
		}
		out.append("</ul>");
		
		return out.toString();
	}
}
