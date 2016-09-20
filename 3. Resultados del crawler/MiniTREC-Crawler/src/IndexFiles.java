import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.es.SpanishLightStemFilter;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * 
 * @author Rafael Marcén Altarriba (650435)
 *
 * Aplicación de linea de comando para indexar todos los ficheros de texto en un
 * directorio o un archivo de dumpeo realizado con Apache Nutch. 
 * Uso: java IndexFiles -index indexPath [-docs docsPath | -dump dumpPath] [-update]
 */
public class IndexFiles {

	/**
	 * Indexa todos los ficheros de texto en un directorio o fichero de dump.
	 * 
	 * @param args:
	 *            Argumentos de la linea de comandos
	 */
	public static void main(String[] args) {
		String usage = "java IndexFiles -index <indexPath> [-docs <docsPath> | -dump <dumpPath>] [-update]";
		String indexPath = "index"; // Directorio del indice por defecto
		String dumpPath = null; // Directorio donde esta el fichero de dump
		String docsPath = null; // Directorio donde estan los ficheros a indexar
		boolean create = true;

		/* Obtener los argumentos */
		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				indexPath = args[i + 1];
				i++;
			} else if ("-docs".equals(args[i])) {
				docsPath = args[i + 1];
				i++;
			} else if ("-dump".equals(args[i])) {
				dumpPath = args[i + 1];
				i++;
			} else if ("-update".equals(args[i])) {
				create = false;
			}
		}

		/* Comprobar los argumentos */
		if ((indexPath == null || dumpPath == null) && (indexPath == null || docsPath == null)) {
			System.err.println("Uso: " + usage);
			System.exit(1);
		}

		/*
		 * Comprobar si el contenido a indexar son ficheros xml o un dump de
		 * Nutch.
		 */
		File docsDir = null;
		if (docsPath != null)
			docsDir = new File(docsPath);
		else
			docsDir = new File(dumpPath);

		/*
		 * Comprobar que existe el directorio donde estan los ficheros a indexar
		 * y que se puede leer.
		 */
		if (!docsDir.exists() || !docsDir.canRead()) {
			System.out.println("El directorio '" + docsDir.getAbsolutePath()
					+ "' no existe o no es legible, por favor comprueba la ruta");
			System.exit(1);
		}

		Date start = new Date();
		try {
			System.out.println("Indexando en el directorio '" + indexPath + "'...");

			/* Apertura del directorio */
			Directory dir = FSDirectory.open(Paths.get(indexPath));

			/* Analizador */
			Analyzer analyzer = new Analyzer() {
				@Override
				protected TokenStreamComponents createComponents(String fieldName) {
					// Minusculas
					Tokenizer source = new LowerCaseTokenizer();
					// Filtrado de palabras vacias en español y teniendo en
					// cuenta las necesidades
					final CharArraySet stopSet = new CharArraySet(StopWords.defaultStopWords, false);
					TokenStream filter = new StopFilter(source, stopSet);
					filter = new SpanishLightStemFilter(filter);
					return new TokenStreamComponents(source, filter);
				}
			};
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

			if (create) {
				// Crea un nuevo indice en el directorio, eliminando
				// cualquier documento que haya sido indexado previamente.
				iwc.setOpenMode(OpenMode.CREATE);
			} else {
				// Añade nuevos documentos a un indice existente.
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			IndexWriter writer = new IndexWriter(dir, iwc);

			/* Proceso de indexación */
			indexDocs(writer, docsDir, docsPath != null);

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total de milisegundos");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	/**
	 * Indexa el fichero dado usando el writer, o si el parametro file es un directorio
	 * se accede recursivamente sobre los ficheros y directorios encontrados en ese
	 * directorio.
	 * 
	 * @param writer
	 *            Writer al indice donde la informacion del fichero/directorioto se va a 
	 *            almacenar
	 * @param file
	 *            El fichero a indexar, o el directorio para recorrer recursivamente en busca de
	 *            ficheros a indexar
	 * @param docs
	 * @throws IOException
	 *             Exception
	 */
	private static void indexDocs(IndexWriter writer, File file, boolean docs) throws IOException {
		// No intentar indexar ficheros que no se puedan leer
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// Puede ocurrir un error de IO
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(writer, new File(file, files[i]), docs);
					}
				}
			} else {
				/* Si hay que indexar los ficheros xml */
				if (docs) {
					// Crea un nuevo documento vacio
					Document doc = new Document();

					// Añadir la ruta del fichero con nombre de campo  "path"
					Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
					doc.add(pathField);

					// Añadir la ultima fecha en la que se modifico el fichero con 
					// nombre de campo "modified".
					doc.add(new LongField("modified", file.lastModified(), Field.Store.YES));

					// Indexación de los documentos XML de la coleccion
					indexXml(doc, file);

					if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
						// Nuevo indice
						System.out.println("añadiendo " + file);
						writer.addDocument(doc);
					} else {
						// Indice ya existente
						System.out.println("actualizando " + file);
						writer.updateDocument(new Term("path", file.getPath()), doc);
					}
				}
				/* Si hay que indexar el dump de Nutch */
				else {
					indexDump(file, writer);

				}
			}
		}
	}

	/**
	 * Indexa los campos del fichero de dump generado por Nutch
	 * 
	 * @param file
	 *            El fichero a indexar
	 * @param writer
	 * @throws IOException
	 *             Exception
	 */
	private static void indexDump(File file, IndexWriter writer) throws IOException {

		Scanner sc = new Scanner(file);
		int numFiles = 0;
		while (sc.hasNextLine()) {
			// Recno::
			String line = sc.nextLine();
			if (line.contains("Recno::")) {
				Document doc = new Document();
				// URL::
				Scanner xmlLine = new Scanner(sc.nextLine());
				xmlLine.next();
				String xml = xmlLine.next();
				xmlLine.close();
				// Linea vacia
				sc.nextLine();
				// ParseText::
				sc.nextLine();
				// Contendio del fichero
				Scanner contentLine = new Scanner(sc.nextLine());
				String url = contentLine.next();
				if (url.contains("http://")) {
					String lang = contentLine.next();
					String content = contentLine.nextLine().trim();

					doc.add(new StringField("xml", xml, Field.Store.YES));
					doc.add(new StringField("url", url, Field.Store.YES));
					doc.add(new StringField("language", lang, Field.Store.YES));
					doc.add(new TextField("contents", content, Field.Store.YES));
					numFiles++;

					if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
						// Nuevo indice
						System.out.println("añadiendo " + xml);
						writer.addDocument(doc);
					} else {
						// Indice ya existente
						System.out.println("actualizando " + xml);
						writer.updateDocument(new Term("path", file.getPath()), doc);
					}
				}
				contentLine.close();

				// Linea vacia
				if (sc.hasNextLine())
					sc.nextLine();
			}
		}
		System.out.println(numFiles + " ficheros indexados");
		sc.close();
	}

	/**
	 * Indexa los campos de los documentos XML de la coleccion
	 * 
	 * @param doc
	 *            El documento donde se añaden los indices
	 * @param file
	 *            El fichero XML a indexar
	 * @throws IOException
	 *             Exception
	 */
	private static void indexXml(Document doc, File file) throws IOException {

		// Indices separados para cada campo o elemento de metadatos del fichero
		// XML
		// creator: Autor y director del trabajo.
		// title: Titulo del trabajo
		// description: Descripción breve del trabajo
		// date: Fecha de presentación del trabajo
		String textfields[] = { "creator", "title", "description", "date" };
		// identifier: URL e identificador del trabajo.
		// language: Idioma en el que se ha escrito el trabajo
		String stringfields[] = { "identifier", "language" };

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

		for (int j = 0; j < textfields.length; j++) {
			NodeList nodes = dom.getElementsByTagName("dc:" + textfields[j]);
			for (int i = 0; i < nodes.getLength(); i++) {
				// Se busca en el campo "description" dos fechas
				// compuestas por 4 numeros las cuales son el minimo
				// numero de 4 cifras y el maximo numero
				// de 4 cifras encontrados.
				if (textfields[j].equals("description")) {
					Pattern pattern = Pattern.compile("\\d{4}");
					Matcher matcher = pattern.matcher(nodes.item(i).getTextContent());
					int max = 0, min = Integer.MAX_VALUE;
					while (matcher.find()) {
						int temp = Integer.valueOf(matcher.group());
						if (temp > max) {
							max = temp;
						}
						if (temp < min) {
							min = temp;
						}
					}
					// Solo se añaden si existen dos fechas en el campo
					// "description"
					if (max != 0 && min != Integer.MAX_VALUE) {
						doc.add(new IntField("min_date", min, Field.Store.YES));
						doc.add(new IntField("max_date", min, Field.Store.YES));
					}
				}
				doc.add(new TextField(textfields[j], nodes.item(i).getTextContent(), Field.Store.YES));
			}
		}

		for (int j = 0; j < stringfields.length; j++) {
			NodeList nodes = dom.getElementsByTagName("dc:" + stringfields[j]);
			for (int i = 0; i < nodes.getLength(); i++) {
				doc.add(new StringField(stringfields[j], nodes.item(i).getTextContent(), Field.Store.YES));
			}
		}
	}
}