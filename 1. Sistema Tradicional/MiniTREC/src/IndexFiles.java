/**
* @author  Rafael Marcén
*/

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Aplicación de linea de comando para indexar todos los ficheros de texto en un
 * directorio. 
 * Uso: java IndexFiles -index indexPath -docs docsPath [-update]
 */
public class IndexFiles {

	/** Indexa todos los ficheros de texto en un directorio.
	 * @param args Argumentos de linea de comandos
	 */
	public static void main(String[] args) {
		String usage = "java IndexFiles -index <indexPath> -docs <docsPath> [-update]";
		String indexPath = "index";
		String docsPath = null;
		boolean create = true;
		for (int i = 0; i < args.length; i++) {
			if ("-index".equals(args[i])) {
				indexPath = args[i + 1];
				i++;
			} else if ("-docs".equals(args[i])) {
				docsPath = args[i + 1];
				i++;
			} else if ("-update".equals(args[i])) {
				create = false;
			}
		}

		if (indexPath == null || docsPath == null) {
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		final File docDir = new File(docsPath);
		if (!docDir.exists() || !docDir.canRead()) {
			System.out.println("Document directory '" + docDir.getAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		try {
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(Paths.get(indexPath));
			Analyzer analyzer = new Analyzer() {
				@Override
				protected TokenStreamComponents createComponents(String fieldName) {
					// Minusculas
					Tokenizer source = new LowerCaseTokenizer();
					// Filtrado de palabras vacias en español y teniendo en
					// cuenta las necesidaes
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
			indexDocs(writer, docDir);

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		} catch (IOException e) {
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	/**
	 * Indexes the given file using the given writer, or if a directory is
	 * given, recurses over files and directories found under the given
	 * directory.
	 * 
	 * NOTE: This method indexes one document per input file. This is slow. For
	 * good throughput, put multiple documents into your input file(s). An
	 * example of this is in the benchmark module, which can create "line doc"
	 * files, one document per line, using the <a href=
	 * "../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
	 * >WriteLineDocTask</a>.
	 * 
	 * @param writer
	 *            Writer to the index where the given file/dir info will be
	 *            stored
	 * @param file
	 *            The file to index, or the directory to recurse into to find
	 *            files to index
	 * @throws IOException
	 *             If there is a low-level I/O error
	 */
	private static void indexDocs(IndexWriter writer, File file) throws IOException {
		// do not try to index files that cannot be read
		if (file.canRead()) {
			if (file.isDirectory()) {
				String[] files = file.list();
				// an IO error could occur
				if (files != null) {
					for (int i = 0; i < files.length; i++) {
						indexDocs(writer, new File(file, files[i]));
					}
				}
			} else {

				FileInputStream fis;
				try {
					fis = new FileInputStream(file);
				} catch (FileNotFoundException fnfe) {
					return;
				}

				try {

					// Crea un nuevo documento vacio
					Document doc = new Document();

					// Add the path of the file as a field named "path". Use a
					// field that is indexed (i.e. searchable), but don't
					// tokenize
					// the field into separate words and don't index term
					// frequency
					// or positional information:
					Field pathField = new StringField("path", file.getPath(), Field.Store.YES);
					doc.add(pathField);

					// Add the last modified date of the file a field named
					// "modified".
					// Use a LongField that is indexed (i.e. efficiently
					// filterable with
					// NumericRangeFilter). This indexes to milli-second
					// resolution, which
					// is often too fine. You could instead create a number
					// based on
					// year/month/day/hour/minutes/seconds, down the resolution
					// you require.
					// For example the long value 2011021714 would mean
					// February 17, 2011, 2-3 PM.
					doc.add(new LongField("modified", file.lastModified(), Field.Store.YES));

					// Indexación de los documentos XML de la coleccion
					indexXml(doc, file);

					if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
						// New index, so we just add the document (no old
						// document can be there):
						System.out.println("adding " + file);
						writer.addDocument(doc);
					} else {
						// Existing index (an old copy of this document may have
						// been indexed) so we use updateDocument instead to replace 
						// the old one matching the exact path, if present:
						System.out.println("updating " + file);
						writer.updateDocument(new Term("path", file.getPath()), doc);
					}

				} finally {
					fis.close();
				}
			}
		}
	}

	/**
	 * Indexa los campos de los documentos XML de la coleccion
	 * @param doc El documento donde se añaden los indices
	 * @param file El fichero XML a indexar
	 * @throws IOException Exception
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