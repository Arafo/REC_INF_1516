/**
 * @author Rafael Marcén Altarriba (650435)
 * 			
 * @version 1.0
 * @date 06-02-2016
 */

package ri.sistema.semantico;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.FileManager;

/**
 * Uso: java SemanticSearcher -rdf <rdfPath> -rdfs <rdfsPath> -infoNeeds <infoNeedsFile> -output <resultsFile> 
 * - -rdf <rdfPath> indica la ruta del fichero RDF que contiene la colección de recursos. 
 * - -rdfs <rdfsPath> indica la ruta del esquema que describe la estructura de la colección de recursos. 
 * - El argumento -infoNeeds <infoNeedsFile> permite indicar la ruta del fichero que contiene las necesidades de
 *   información. Se debe utilizar como formato de ese fichero el especificado en este enunciado. 
 * - El argumento -output <resultsFile> permite indicar la ruta del fichero donde se generarán los resultados del
 *   sistema para las necesidades de información.
 *
 */
public class SemanticSearcher {

	/**
	 * Descripcion del metodo main
	 * @param args
	 */
	public static void main(String[] args) {
		String usage = "java SemanticSearcher -rdf <rdfPath> -rdfs <rdfsPath> -infoNeeds <infoNeedsFile> -output <resultsFile>";
		String rdfPath = "semantico/grafo.rdf"; // Fichero RDF donde se almacena el grafo de la coleccion
		String rdfsPath = "semantico/modeloRDFS.xml"; // Fichero RDFS donde se almacena el esquema que describe la estructura de la colección de recursos
		String infoNeeds = "semantico/necesidadesInformacionElegidas.txt"; // Fichero de las necesidades de información
		String output = "semantico/semanticResults05.txt"; // Fichero donde se almacenan los resultados
		int outputLimit = 50;
		
		/* Obtener los argumentos */
		for (int i = 0; i < args.length; i++) {
			if ("-rdf".equals(args[i])) {
				rdfPath = args[i + 1];
				i++;
			} else if ("-rdfs".equals(args[i])) {
				rdfsPath = args[i + 1];
				i++;
			} else if ("-infoNeeds".equals(args[i])) {
				infoNeeds = args[i + 1];
				i++;
			} else if ("-output".equals(args[i])) {
				output = args[i + 1];
				i++;
			}
		}
		
		/* Comprobar los argumentos */
		if (rdfPath == null || rdfPath == null || infoNeeds == null || output == null) {
			System.out.println(usage);
			System.exit(1);
		}
		
		/* PrintWriter para escribir en el fichero de resultados */
		PrintWriter out = null;
		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(output)));
		} catch (IOException e) {
			e.printStackTrace();
		}

		/* Cargamos el fichero RDF de la coleccion */
		Model model = FileManager.get().loadModel(rdfPath);
		
		/* Creamos un modelo de inferencia */
		InfModel inf = ModelFactory.createRDFSModel(model);
		Model rdfs = FileManager.get().loadModel(rdfsPath);
		
		/* Añadimos el modelo RDFS al grafo RDF */
		inf.add(rdfs);
		
		/* Necesidades de informacion almacenadas en un hashmap y su iterador */
		HashMap<String, String> needs = getInfoNeeds(infoNeeds);
		Iterator<Entry<String, String>> it = needs.entrySet().iterator();
		
		while (it.hasNext()) {
			Entry<String, String> pair = it.next();
			System.out.println("Buscando: " + pair.getKey() + " -" + pair.getValue()); 

			/* Consulta SPARQL */
			Query query = QueryFactory.create(pair.getValue());
		    System.out.println(query);

			QueryExecution qexec = QueryExecutionFactory.create(query, inf);
		    ResultSet results = qexec.execSelect();
		    /* Comentar la siguiente linea para escribir en el fichero de salida */
		    //ResultSetFormatter.out(System.out, results, query);
            for (int i=0; i<outputLimit && results.hasNext(); i++) {
                QuerySolution qsol = results.nextSolution();
                Resource x = qsol.getResource("doc");
                String value = x.getURI();
                value = value.substring(value.lastIndexOf("/") + 1);
                value = "oai_zaguan.unizar.es_" + value + ".xml"; 
    			
    			out.println(pair.getKey() + "\t" + value);
            }
		    qexec.close();
		}
		out.close();
	}
	
	/**
	 * Devuelve un HashMap que contiene las necesidades de informacion 
	 * donde el identificador de esta es la clave y el texto es el valor
	 * @param file El fichero de necesidades
	 * @return Devuelve un hashmap con las necesidades de informacion
	 */
	private static HashMap<String, String> getInfoNeeds(String file) {
		// HasMap utilizando la implementacion enlazada para mantener el orden
		// de las necesidades como en el fichero file
		HashMap<String, String> infoNeeds = new LinkedHashMap<String, String>();
		Scanner s = null;
		try {
			s = new Scanner(new File(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while (s.hasNextLine()) {
			infoNeeds.put(s.next(), s.nextLine());
		}
		s.close();

		return infoNeeds;
	}
}