
/**
 * 
 * @author Rafael Marcén Altarriba (650435)
 * @version 1.0
 *
 */

public class QrelValue {
	
	// Variables
	public String document_id;
	public int relevancy;
	
	/**
	 * Identificador y relevancia de un documento 
	 * @param document_id
	 * @param relevancy
	 */
	public QrelValue(String document_id, Integer relevancy) {
		this.document_id = document_id;
		this.relevancy = relevancy;
	}

}
