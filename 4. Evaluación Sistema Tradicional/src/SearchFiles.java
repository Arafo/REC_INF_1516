import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.es.SpanishLightStemFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * @author Rafael Marcén Altarriba (650435)
 *
 */
public class SearchFiles {

	// Palabras a eliminar en una necesidad que no sean stop words
	private static List<String> wordsToRemove = new ArrayList<String>();

	/**
	 * TECNICAS UTILIZADAS
	 * - Nuevas stop words añadidas para filtrar las necesidades
	 * - Si una necesidad contiene la palabra autor o director, se busca 
	 *   las siguientes tres palabras en el atributo "creator"
	 * - Si una necesidad contiene dos valores numericos correspondiente a
	 *   fechas de cuatro numeros, ambos campos sen busca en el atributo
	 *   "description" como si fueran un rango.
	 * - Ademas de las tecnicas anteriores, se busca cada palabra en
	 *   los campos correspondientes.
	 * @param args Argumentos de linea de comandos
	 * @throws Exception Exception
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {
		String usage = "Uso:\tjava org.apache.lucene.demo.SearchFiles -index <indexPath> -infoNeeds <infoNeedsFile> -output <resultsFile>";
		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}

		String index = null;
		String infoNeeds = null;
		String output = null;
		String field = "contents";

		/* Obtener los argumentos */
		for (int i = 0;i < args.length;i++) {
			if ("-index".equals(args[i])) {
				index = args[i+1];
				i++;
			} else if ("-infoNeeds".equals(args[i])) {
				infoNeeds = args[i+1];
				i++;
			} else if ("-output".equals(args[i])) {
				output = args[i+1];
				i++;
			} 
		}
		
		/* Comprobar los argumentos */
		if (index == null || infoNeeds == null || output == null) {
			System.out.println(usage);
			System.exit(0);
		}
		
		/* Borrar fichero de resultados si ya existe */
		File outputFile = new File(output);
		if (outputFile.exists()) {
			outputFile.delete();
		}

		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
		IndexSearcher searcher = new IndexSearcher(reader);
		searcher.setSimilarity(new BM25Similarity());

		Analyzer analyzer = new Analyzer() { 
			@Override 
			protected TokenStreamComponents createComponents(String fieldName) {
				// Minusculas
	    		Tokenizer source = new LowerCaseTokenizer();
	    		// Filtrado de palabras vacias en español y teniendo en cuenta las necesidaes
	    		final CharArraySet stopSet = new CharArraySet(StopWords.stopWords, false);
	    		TokenStream filter = new StopFilter(source, stopSet);     
	    		filter = new SpanishLightStemFilter(filter); 
	    		return new TokenStreamComponents(source, filter);  
			}  
		};   
		QueryParser parser = new QueryParser(field, analyzer);
		
		/* Necesidades de informacion almacenadas en un hashmap y su iterador */
		HashMap<String, String> needs = getInfoNeeds(infoNeeds);
		Iterator<Entry<String, String>> it = needs.entrySet().iterator();
		String fields [] = {};

		while (true) {
			String line = null;	
			Entry<String, String> pair = null;
			if (it != null && it.hasNext()) {
		        pair = it.next();
				line = pair.getValue();
				it.remove();
			} 
			else {
				// Ya se han procesado todas las necesidades
				break;
			}
			
			if (line == null || line.length() == -1) {
				break;
			}

			line = line.trim();
			if (line.length() == 0) {
				break;
			}
			line = line.replaceAll("[\\¿\\?]", "");

			BooleanQuery query = new BooleanQuery();

			/**
			 * Comprobar autor y director
			 */
			BooleanQuery creatorQuery = findCreators(line, parser);
			// Si la necesidad contiene un autor y/o director
			if (creatorQuery.toString().length() != 0) {
				// Se eliminan de la consulta las palabras ya procesadas
				for (String s: wordsToRemove) {
					line = line.replaceAll(s, "");
				}
				fields = new String[] {"title" ,"description", "date"};
				MultiFieldQueryParser multiParser = new MultiFieldQueryParser(fields, analyzer);
				// Boost para subir la puntuacion en la query creator
				creatorQuery.setBoost(2);
				// Ocurrencia del campo creador OPCIONAL
				// La ocurrencia OBLIGATORIA restringe de forma excesiva los resultados
				query.add(creatorQuery, Occur.SHOULD);
				query.add(multiParser.parse(line), Occur.MUST);

			}

			// Si no se ha encontrado ningun autor y/o director en la necesidad
			else {
				/**
				 *  Comprobar valores numericos
				 */
				BooleanQuery dateQuery = findDates(line);
				if (dateQuery != null && dateQuery.toString().length() != 0) {
					query.add(dateQuery, Occur.MUST);
				}
				
				fields = new String[] {"creator", "title", "description", "date"};
				MultiFieldQueryParser multiParser = new MultiFieldQueryParser(fields, analyzer);
				query.add(multiParser.parse(line), Occur.MUST);
			}

			System.out.println("Buscando: " + query.toString(field));
			doPagingSearch(searcher, query, 50, pair, output);
		}
		reader.close();
	}

	/**
	 * Tecnica utilizado en el parseo de las consulta en la cual, si una necesidad 
	 * contiene la palabra autor o director, se busca las siguientes tres palabras 
	 * solamente en el campo "creator"
	 * @param line La consulta o necesidad
	 * @param parser El parser para procesar la consulta
	 * @return Devuele una query booleana con los posibles creators
	 * @throws IOException Exception
	 * @throws ParseException Exception
	 */
	@SuppressWarnings("deprecation")
	private static BooleanQuery findCreators(String line, QueryParser parser) throws IOException, ParseException {
		int creatorIndex = -1;
		int wordsAhead = 3;
		BooleanQuery creatorQuery = new BooleanQuery();

		line = line.toLowerCase();
		String[] words = removeStopWords(line).split(" ");
		for (int i = 0; i < words.length; i++) {
			if (words[i].equals("director") || words[i].equals("autor")) {
				creatorIndex = i + 1;
				wordsToRemove.add(words[i]);
				wordsAhead += creatorIndex;
				for (int j = creatorIndex; j < wordsAhead; j++) {
					if (j < words.length) {
						creatorQuery.add(parser.parse("creator: " + words[j]), Occur.SHOULD);
						wordsToRemove.add(words[j]);
						line = line.replaceAll(words[j], "");
						//System.out.println("creator: " + words[j]);
					}
				}
			}
		}
		//System.out.println(line);
		return creatorQuery;
	}
	
	/**
	 * Tecnica utilizada en el parse de consultas en la cual, si una necesidad 
	 * contiene dos valores numericos correspondiente a fechas de cuatro numeros, 
	 * ambos campos sen busca en el campo "description" como si fueran un rango.
	 * @param line La consulta o necesidad
	 * @return Devuele una query booleana con el rango de fechas
	 */
	@SuppressWarnings("deprecation")
	private static BooleanQuery findDates(String line) {
		BooleanQuery query = new BooleanQuery();
		
		Pattern pattern = Pattern.compile("\\d{4}");
		Matcher matcher = pattern.matcher(line);
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
		if (max != 0 && min != Integer.MAX_VALUE) {
			// Ambas ocurrencias son OPCIONALES para no restringir
			// excesivamente la busqueda y que solo sirvan como boost
			
			// min_date <= max
			NumericRangeQuery<Integer> minDate = NumericRangeQuery.newIntRange("min_date", null, max, true , true ); 
			query.add(minDate, BooleanClause.Occur.SHOULD);

			// max_date >= min 
			NumericRangeQuery<Integer> maxDate = NumericRangeQuery.newIntRange("max_date", min, null, true , true ); 
			query.add(maxDate, BooleanClause.Occur.SHOULD);
		}
		return query;
	}

	/**
	 * Devuelve un String que contiene la cadena de texto input
	 * sin las stop words.
	 * @param input La cadena de texto sin procesar
	 * @return Devuelve el String resultante de eliminar las stop words
	 * 		   de la cadena input
	 * @throws IOException Exception
	 */
	private static String removeStopWords(String input) throws IOException {

		final CharArraySet stopSet = new CharArraySet(StopWords.stopWords, false);
		Analyzer analyzer = new StandardAnalyzer(stopSet);
		TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(input));
		analyzer.close();

		StringBuilder sb = new StringBuilder();
		CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);

		tokenStream.reset();
		while (tokenStream.incrementToken()) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append(token.toString());
		}
		tokenStream.close();
		return sb.toString();
	}

	/**
	 * Devuelve un HashMap que contiene las necesidades de informacion 
	 * donde el identificador de esta es la clave y el texto es el valor
	 * @param file El fichero de necesidades
	 * @return Devuelve un hashmap con las necesidades de informacion
	 */
	private static HashMap<String, String> getInfoNeeds(String file) {
		// HasMap utilizando la implementacion enlazada para mantener el orden
		// de las necesidades como estan en el fichero file
		HashMap<String, String> infoNeeds = new LinkedHashMap<String, String>();
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
		} catch (IOException e3) {
			System.out.println("Error en la clase " + e3.getClass() + " - Mensaje: " + e3.getMessage());
		}
		
		NodeList nodes = dom.getElementsByTagName("informationNeed");
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
            Element element = (Element) node;
            
			Node identifierNode = element.getElementsByTagName("identifier").item(0);
			Node textNode = element.getElementsByTagName("text").item(0);
			
			infoNeeds.put(identifierNode.getTextContent(), textNode.getTextContent());
		}
		return infoNeeds;
	}

	/**
	 * This demonstrates a typical paging search scenario, where the search engine presents 
	 * pages of size n to the user. The user can then go to the next page if interested in
	 * the next hits.
	 * @param searcher El buscador utilizado en el parseo de la consulta
	 * @param query La query generada en la consulta
	 * @param hitsPerPage El maximo numero de documentos en el resultado
	 * @param pair El par correspondiente a la necesidad (identifier, text)
	 * @param output El fichero donde de escriben los resultados de la consulta
	 * @throws IOException Exception
	 */
	public static void doPagingSearch(IndexSearcher searcher, Query query, int hitsPerPage, 
			Entry<String, String> pair, String output) throws IOException {
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(output, true)));

		TopDocs results = searcher.search(query, hitsPerPage);
		ScoreDoc[] hits = results.scoreDocs;

		int numTotalHits = results.totalHits;
		System.out.println(numTotalHits + " total matching documents");

		int start = 0;
		int end = numTotalHits;

		for (int i = start; i < end; i++) {
			Document doc = searcher.doc(hits[i].doc);
			// Parte el string path por "/", elige el elemento que queda a
			// la derecha que es el nombre del fichero y se queda con las 
			// dos primeros caracteres que son las cifras 
			String path = doc.get("path");
			if (path != null) {
				String[] absolutePath;
				// Windows
			    if (System.getProperty("os.name").startsWith("Windows")) {
			    	absolutePath = path.split("\\\\");
			    // Unix u otros
			    } else {
			    	absolutePath = path.split("/");
			    }
				out.println(pair.getKey() + "\t" + absolutePath[absolutePath.length - 1]);
			}
			// explain the scoring function
			//System.out.println(searcher.explain(query, hits[i].doc));
		}
		out.close();
	}
}
