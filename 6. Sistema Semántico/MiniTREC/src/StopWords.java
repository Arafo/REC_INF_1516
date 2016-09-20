import java.util.Arrays;
import java.util.List;

/**
 * 
 * @author Rafael Marcén Altarriba (650435)
 * @version 1.0
 *
 */
public class StopWords {
	
	/**
	 * Lista de palabras vacias teniendo en cuenta las 
	 * necesidades de información
	 */
	static final List<String> stopWords = Arrays.asList(
			// 1ª necesidad
    		"me", "interesan", "los", "trabajos", "en", "relación", 
    		"con", "el", /*"mundo",*/ "de", "la", "y", "el", "cuyo", "o",
    		"se", "llame",
    		
    		// 2ª necesidad
    		"estoy", "interesado", "gustaría", "saber", "hay", 
    		"relacionados", "periodo", /*"histórico",*/ "artículos",
    		/*"historia",*/ "hecho", "englobados",
    		
    		// 3ª necesidad
    		"traten",
    		
    		// 4ª necesidad
    		"quiero", "documentos", "sobre", "últimos",
    		
    		// 5ª necesidad
    		"gustaría", "encontrar", "situadas", "pertenezcan",
    		"tanto", "cuyo", "sea",
    		    		
    		// spanish_stop.txt (Stop words por defecto)
    		"que", "a", "del", "las", "por", "un", "para", "con",
    		"no", "una", "su", "al", "lo", "como", "más",
    		"pero", "sus", "le", "ya", "o", "este",
    		"sí", "porque", "esta", "entre",
    		"cuando", "muy", "sin", "sobre", "también",
    		"me", "hasta", "hay", "donde", "quien",
    		"desde", "todo", "nos", "durante", "todos",
    		"uno", "les", "ni", "contra", "otros", "ese",
    		"eso", "ante", "ellos", "e", "esto", "mí",
    		"antes", "algunos", "qué", "unos", "yo", "otro", "otras",
    		"otra", "él", "tanto", "esa", "estos", "mucho", "quienes",
    		"nada", "muchos", "cual", "poco", "ella", "estar",
    		"estas", "algunas", "algo", "nosotros",
    		"mi", "mis", "tú", "te", "ti", "tu",
    		"tus", "ellas", "nosotras", "vosotros", "vosotras", "os", "mío",
    		"mía", "míos", "mías", "tuyo", "tuya", "tuyos", "tuyas",
    		"suyo", "suya", "suyos", "suyas", "nuestro", "nuestra", "nuestros",
    		"nuestras", "vuestro", "vuestra", "vuestros", "vuestras", "esos", "esas",
    		"estoy", "estás", "está", "estamos", "estáis", "están",
    		"esté", "estés", "estemos", "estéis", "estén", "estaré", "estarás",
    		"estará", "estaremos", "estaréis", "estarán", "estaría", "estarías", "estaríamos",
    		"estaríais", "estarían", "estaba", "estabas", "estábamos", "estabais", "estaban",
    		"estuve", "estuviste", "estuvo", "estuvimos", "estuvisteis", "estuvieron", "estuviera",
    		"estuvieras", "estuviéramos", "estuvierais", "estuvieran", "estuviese", "estuvieses", "estuviésemos",
    		"estuvieseis", "estuviesen", "estando", "estado", "estada", "estados", "estadas",
    		"estad", "he", "has", "ha", "hemos", "habéis",
    		"han", "haya", "hayas", "hayamos", "hayáis", "hayan", "habré",
    		"habrás", "habrá", "habremos", "habréis", "habrán", "habría", "habrías",
    		"habríamos", "habríais", "habrían", "había", "habías", "habíamos", "habíais",
    		"habían", "hube", "hubiste", "hubo", "hubimos", "hubisteis", "hubieron",
    		"hubiera", "hubieras", "hubiéramos", "hubierais", "hubieran", "hubiese", "hubieses",
    		"hubiésemos", "hubieseis", "hubiesen", "habiendo", "habido", "habida", "habidos",
    		"habidas", "soy", "eres", "es", "somos", "sois",
    		"son", "sea", "seas", "seamos", "seáis", "sean", "seré",
    		"serás", "será", "seremos", "seréis", "serán", "sería", "serías",
    		"seríamos", "seríais", "serían", "era", "eras", "éramos", "erais",
    		"eran", "fui", "fuiste", "fue", "fuimos", "fuisteis", "fueron",
    		"fuera", "fueras", "fuéramos", "fuerais", "fueran", "fuese", "fueses",
    		"fuésemos", "fueseis", "fuesen", "siendo", "sido",
    		"tengo", "tienes", "tiene", "tenemos", "tenéis", "tienen", "tenga",
    		"tengas", "tengamos", "tengáis", "tengan", "tendré", "tendrás", "tendrá",
    		"tendremos", "tendréis", "tendrán", "tendría", "tendrías", "tendríamos", "tendríais",
    		"tendrían", "tenía", "tenías", "teníamos", "teníais", "tenían", "tuve",
    		"tuviste", "tuvo", "tuvimos", "tuvisteis", "tuvieron", "tuviera", "tuvieras",
    		"tuviéramos", "tuvierais", "tuvieran", "tuviese", "tuvieses", "tuviésemos", "tuvieseis",
    		"tuviesen", "teniendo", "tenido", "tenida", "tenidos", "tenidas", "tened", "¿", "?"
    		);
	
	/**
	 * Lista de palabras vacias en castellano por defecto
	 */
	static final List<String> defaultStopWords = Arrays.asList(    		
    		// spanish_stop.txt (Stop words por defecto)
    		"que", "a", "del", "las", "por", "un", "para", "con",
    		"no", "una", "su", "al", "lo", "como", "más",
    		"pero", "sus", "le", "ya", "o", "este",
    		"sí", "porque", "esta", "entre",
    		"cuando", "muy", "sin", "sobre", "también",
    		"me", "hasta", "hay", "donde", "quien",
    		"desde", "todo", "nos", "durante", "todos",
    		"uno", "les", "ni", "contra", "otros", "ese",
    		"eso", "ante", "ellos", "e", "esto", "mí",
    		"antes", "algunos", "qué", "unos", "yo", "otro", "otras",
    		"otra", "él", "tanto", "esa", "estos", "mucho", "quienes",
    		"nada", "muchos", "cual", "poco", "ella", "estar",
    		"estas", "algunas", "algo", "nosotros",
    		"mi", "mis", "tú", "te", "ti", "tu",
    		"tus", "ellas", "nosotras", "vosotros", "vosotras", "os", "mío",
    		"mía", "míos", "mías", "tuyo", "tuya", "tuyos", "tuyas",
    		"suyo", "suya", "suyos", "suyas", "nuestro", "nuestra", "nuestros",
    		"nuestras", "vuestro", "vuestra", "vuestros", "vuestras", "esos", "esas",
    		"estoy", "estás", "está", "estamos", "estáis", "están",
    		"esté", "estés", "estemos", "estéis", "estén", "estaré", "estarás",
    		"estará", "estaremos", "estaréis", "estarán", "estaría", "estarías", "estaríamos",
    		"estaríais", "estarían", "estaba", "estabas", "estábamos", "estabais", "estaban",
    		"estuve", "estuviste", "estuvo", "estuvimos", "estuvisteis", "estuvieron", "estuviera",
    		"estuvieras", "estuviéramos", "estuvierais", "estuvieran", "estuviese", "estuvieses", "estuviésemos",
    		"estuvieseis", "estuviesen", "estando", "estado", "estada", "estados", "estadas",
    		"estad", "he", "has", "ha", "hemos", "habéis",
    		"han", "haya", "hayas", "hayamos", "hayáis", "hayan", "habré",
    		"habrás", "habrá", "habremos", "habréis", "habrán", "habría", "habrías",
    		"habríamos", "habríais", "habrían", "había", "habías", "habíamos", "habíais",
    		"habían", "hube", "hubiste", "hubo", "hubimos", "hubisteis", "hubieron",
    		"hubiera", "hubieras", "hubiéramos", "hubierais", "hubieran", "hubiese", "hubieses",
    		"hubiésemos", "hubieseis", "hubiesen", "habiendo", "habido", "habida", "habidos",
    		"habidas", "soy", "eres", "es", "somos", "sois",
    		"son", "sea", "seas", "seamos", "seáis", "sean", "seré",
    		"serás", "será", "seremos", "seréis", "serán", "sería", "serías",
    		"seríamos", "seríais", "serían", "era", "eras", "éramos", "erais",
    		"eran", "fui", "fuiste", "fue", "fuimos", "fuisteis", "fueron",
    		"fuera", "fueras", "fuéramos", "fuerais", "fueran", "fuese", "fueses",
    		"fuésemos", "fueseis", "fuesen", "siendo", "sido",
    		"tengo", "tienes", "tiene", "tenemos", "tenéis", "tienen", "tenga",
    		"tengas", "tengamos", "tengáis", "tengan", "tendré", "tendrás", "tendrá",
    		"tendremos", "tendréis", "tendrán", "tendría", "tendrías", "tendríamos", "tendríais",
    		"tendrían", "tenía", "tenías", "teníamos", "teníais", "tenían", "tuve",
    		"tuviste", "tuvo", "tuvimos", "tuvisteis", "tuvieron", "tuviera", "tuvieras",
    		"tuviéramos", "tuvierais", "tuvieran", "tuviese", "tuvieses", "tuviésemos", "tuvieseis",
    		"tuviesen", "teniendo", "tenido", "tenida", "tenidos", "tenidas", "tened"
    		);
}