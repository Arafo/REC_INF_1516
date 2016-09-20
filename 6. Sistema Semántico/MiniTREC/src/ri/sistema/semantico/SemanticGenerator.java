/**
 * @authors Rafael Marcén Altarriba (650435)
 * 			
 * @version 1.0
 * @date 06-02-2016
 */

package ri.sistema.semantico;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Uso: java SemanticGenerator -rdf <rdfPath> -skos <skosPath> -docs <docsPath> 
 * - El argumento -rdf <rdf> permite indicar la ruta del fichero RDF donde se genera el grafo de la colección.
 * - El argumento -skos <rdf> permite indicar la ruta del fichero RDF donde se almacena el modelo terminológico 
 *   en formato RDF. Dicho modelo tiene que seguir el vocabulario de SKOS. 
 * - El argumento -docs <docsPath> permite indicar la ruta del directorio que contiene la colección de documentos 
 *   a transformar.
 */
public class SemanticGenerator {
	
	/* Dominios */
	private static final String MINITREC_PATH = "http://www.minitrec.com/";
	//private static final String CONCEPTO_PATH = "http://concepto/";
	private static final String SKOS_PATH = "http://www.w3.org/2004/02/skos/core#";
	private static final String FOAF_PATH = "http://xmlns.com/foaf/0.1/";

	public static void main(String[] args) {
		String usage = "java SemanticGenerator -rdf <rdfPath> -skos <skosPath> -docs <docsPath>";
		String rdfPath = "semantico/grafo.rdf"; // Fichero RDF donde se genera el grafo de la coleccion
		String skosPath = "semantico/skos.rdf"; // Fichero RDF donde se almacena el modelo terminologico
		//String docsPath = "C:\\Users\\Rafa\\Desktop\\MiniTREC\\recordsdc\\"; // Directorio donde estan los ficheros a transformar
		String docsPath = "/Users/Rafa/Downloads/recordsdc/";
		
		/* Obtener los argumentos */
		for (int i = 0; i < args.length; i++) {
			if ("-rdf".equals(args[i])) {
				rdfPath = args[i + 1];
				i++;
			} else if ("-skos".equals(args[i])) {
				skosPath = args[i + 1];
				i++;
			} else if ("-docs".equals(args[i])) {
				docsPath = args[i + 1];
				i++;
			}
		}
		
		/* Comprobar los argumentos */
		if (rdfPath == null || skosPath == null || docsPath == null) {
			System.err.println("Uso: " + usage);
			System.exit(1);
		}
		
		final File docDir = new File(docsPath);
		/*
		 * Comprobar que existe el directorio donde estan los ficheros a transformar
		 * y que se puede leer.
		 */
		if (!docDir.exists() || !docDir.canRead()) {
			System.out.println("El directorio '" + docDir.getAbsolutePath()
			+ "' no existe o no es legible, por favor compruebe la ruta");
			System.exit(1);
		}
		
		/* tiempo inicial */
		Date start = new Date();
		
		try {
			final FileOutputStream rdfFile = new FileOutputStream(new File(rdfPath));

			System.out.println("Trasnformando a RDF en el fichero '" + rdfPath + "'...");
			/* Obtenemos el modelo SKOS */
			Model skos = FileManager.get().loadModel(skosPath);
			/* Obtenemos el modelo RDF de la coleccion XML */
			Model model = transformDocs(docDir, skos);
			/* Anadimos el modelo SKOS al modelo RDF */
			model.add(skos);
			/* Escribimos el modelo al fichero especificado */
			model.write(rdfFile);
			
			/* tiempo final */
			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total de milisegundos");

		} catch (IOException e) {
			System.out.println(" encontrado un " + e.getClass() + "\n con el mensaje: " + e.getMessage());
		}
	}
	
	/**
	 * Transforma todos los documentos XML contenidos en el directorio [file] a RDF
	 * @param file Ruta donde se encuentra la coleccion XML
	 * @param skos Modelo SKOS
	 * @return devuelve el modelo RDF de la coleccion XML
	 * @throws IOException
	 */
	private static Model transformDocs(File file, Model skos) throws IOException {
		Model model = ModelFactory.createDefaultModel();
		// Definir los prefijos del modelo
		model.setNsPrefix("mt", MINITREC_PATH);
		model.setNsPrefix("skos", SKOS_PATH);
		model.setNsPrefix("foaf", FOAF_PATH);
		//model.setNsPrefix("co", CONCEPTO_PATH);
		// No intentar transformar a RDF ficheros que no se puedan leer
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// Puede ocurrir un error de IO
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						Model fileModel = xmlToRdf(file + "/" + files[i], skos);
						model.add(fileModel);
					}
				}
			} 
		}
		return model;
	}
	
	/**
	 * Transforma un fichero XML a RDF
	 * @param file Fichero XML a transformar
	 * @param skos Modelo SKOS
	 * @return devuelve el modelo RDF del fichero XML
	 * @throws IOException
	 */
	private static Model xmlToRdf(String file, Model skos) throws IOException {		
				
		/*
		 * Parseado del fichero XML
		 */
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		org.w3c.dom.Document dom = null;
		try {
			builder = factory.newDocumentBuilder();
			dom = builder.parse(file);
		} catch (SAXException e1) {
			System.out.println("Error en la clase: " + e1.getClass() + " - Mensaje: " + e1.getMessage());
		} catch (ParserConfigurationException e2) {
			System.out.println("Error en la clase " + e2.getClass() + " - Mensaje: " + e2.getMessage());
		}
		
		String[] identifier = null, creator = null;
		String language = null, title = null, description = null, publisher = null, date = null;
		
		try {
			// Identifier
			NodeList identifiers = dom.getElementsByTagName("dc:identifier");
			identifier = new String[identifiers.getLength()];
			for (int i=0; i<identifiers.getLength(); i++) {
				identifier[i] = identifiers.item(i).getTextContent();
			}
			// Language
			if (dom.getElementsByTagName("dc:language").item(0) != null)
				language = dom.getElementsByTagName("dc:language").item(0).getTextContent();
			// Creator
			NodeList creators = dom.getElementsByTagName("dc:creator");
			creator = new String[creators.getLength()];
			for (int i=0; i<creators.getLength(); i++) {
				creator[i] = creators.item(i).getTextContent();
			}
			// Title
			title = dom.getElementsByTagName("dc:title").item(0).getTextContent();
			// Description
			description = dom.getElementsByTagName("dc:description").item(0).getTextContent();
			// Publisher
			publisher = dom.getElementsByTagName("dc:publisher").item(0).getTextContent();
			// Date
			date = dom.getElementsByTagName("dc:date").item(0).getTextContent();
			System.out.println(identifier[0]);

		} catch (NullPointerException e) {
			System.out.println("Error en la clase " + e.getClass() + " - Mensaje: " + e.getMessage());
			System.exit(0);
		}
		
		/*
		 * Generacion del modelo RDF
		 */
		
		Model model = ModelFactory.createDefaultModel();	
		
		/* Obtenemos la URI del documento */
		String uri = "";
		for (String id : identifier) {
			if (id.startsWith("http")) {
				uri = id;
				break;
			} 
		}

		/* Crea el recurso Documento */
		Resource document = model.createResource(uri)
				.addProperty(RDF.type, MINITREC_PATH + "Documento")
				.addProperty(model.createProperty(MINITREC_PATH + "Titulo"), title)
				.addProperty(model.createProperty(MINITREC_PATH + "Fecha"), date)
				.addProperty(model.createProperty(MINITREC_PATH + "Descripcion"), description);
		
		if (language != null)
			document.addProperty(model.createProperty(MINITREC_PATH + "Lenguaje"), language);

		String publisherURI = MINITREC_PATH + "Publisher/" + publisher.replaceAll("[\\s,]", "");
		Resource publish = model.createResource(publisherURI).addProperty(FOAF.name, publisher);
		document.addProperty(model.createProperty(MINITREC_PATH + "Publisher"), publish);
		
		/* Se añaden todos los autores de un documento */
		for (String e : creator) {
			String personURI = MINITREC_PATH + "Autor/" + e.replaceAll("[\\s,]", "" );
			Resource person = model.createResource(personURI)
					.addProperty(RDF.type, MINITREC_PATH + "Persona")
					.addProperty(FOAF.name, e);
			document.addProperty(model.createProperty(MINITREC_PATH + "Autor"), person);
		}
		
		/*
		 * Modelo SKOS
		 */
		
		/* Se recorren todos los terminos del modelo SKOS */
		ResIterator it = skos.listSubjectsWithProperty(model.createProperty(SKOS_PATH + "prefLabel"));
		it.andThen(skos.listSubjectsWithProperty(model.createProperty(SKOS_PATH + "altLabel")));
		String conceptURI = MINITREC_PATH + "Subject";
		String titleAux = clean(title);
		String descriptionAux = clean(description);
		while (it.hasNext()) {
			Resource res = it.next();
			Statement st = res.getProperty(model.createProperty(SKOS_PATH + "prefLabel"));
			
			String co = "";
			if (st.getObject().isLiteral()) {
				co = st.getLiteral().toString();
			} else {
				co = st.getResource().getURI();
			}
			
			/* Se añade el termino SKOS si aparece en el titulo o en la descripcion del documento */
			if (titleAux.contains(co.trim().toLowerCase()) || descriptionAux.contains(co.trim().toLowerCase())) {
				document.addProperty(model.createProperty(conceptURI), res);
			}
			
			st = res.getProperty(model.createProperty(SKOS_PATH + "altLabel"));
			if (st != null) {
				if (st.getObject().isLiteral()) {
					co = st.getLiteral().toString();
				} else {
					co = st.getResource().getURI();
				}
				/* Se añade el termino alternativo SKOS si aparece en el titulo o en la descripcion del documento */
				if (titleAux.contains(co.trim().toLowerCase()) || descriptionAux.contains(co.trim().toLowerCase())) {
					document.addProperty(model.createProperty(conceptURI), res);
				}
			}
		}
		        
        return model;
	}
	
	/**
	 * Transforma la cadena de entrada a su forma normalizada en minuscula y
	 * sin acentos.
	 * @param cadena 
	 * @return devuelve una cadena en minuscula y sin acentos
	 */
	private static String clean(String cadena) {
		cadena = cadena.toLowerCase();
		cadena = cadena.replaceAll("á", "a");
		cadena = cadena.replaceAll("é", "e");
		cadena = cadena.replaceAll("í", "i");
		cadena = cadena.replaceAll("ó", "o");
		cadena = cadena.replaceAll("ú", "u");
		return cadena;
	}
}