
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * 
 * @author Rafael Marcén Altarriba (650435)
 * @version 1.0
 *
 */

public class Evaluation {

	/**
	 * Uso: java Evaluation -qrels <qrelsFileName> -results <resultsFileName> -output <outputFileName>
	 * @param args
	 */
	public static void main(String[] args) {
		String usage = "Uso:\tjava Evaluation -qrels <qrelsFileName> -results <resultsFileName> -output <outputFileName>";
		if (args.length > 0 && ("-h".equals(args[0]) || "-help".equals(args[0]))) {
			System.out.println(usage);
			System.exit(0);
		}

		String qrels = null;
		String results = null;
		String output = "output.txt";

		for (int i = 0; i < args.length; i++) {
			if ("-qrels".equals(args[i])) {
				qrels = args[i + 1];
				i++;
			} else if ("-results".equals(args[i])) {
				results = args[i + 1];
				i++;
			} else if ("-output".equals(args[i])) {
				output = args[i + 1];
				i++;
			}
		}

		// Comprobacion de argumentos de entrada
		if (qrels == null || results == null || output == null) {
			System.out.println(usage);
			System.exit(0);
		}

		// Manejo de ficheros de entrada y salida
		PrintWriter out = null;
		HashMap<String, List<QrelValue>> qrelsMap = null;
		HashMap<String, List<String>> resultsMap = null;

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(output, false)));
			qrelsMap = getQrels(qrels);
			resultsMap = getResults(results);
		} catch (IOException e) {
			System.err.println(usage);
			System.exit(0);
		} 

		// Variables para los resultados totales
		double total_precision = 0;
		double total_recall = 0;
		double total_precision10 = 0;
		double map = 0;
		List<PrecisionRecall> total_interpolated_recall_precision = new ArrayList<PrecisionRecall>();
		int interpolated_min = Integer.MAX_VALUE;;
		
		// Iterador sobre las necesidades de informacion
		Iterator<Entry<String, List<QrelValue>>> it = qrelsMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<QrelValue>> pair = (Map.Entry<String, List<QrelValue>>) it.next();

			List<QrelValue> qrels_list = pair.getValue();
			List<String> results_list = resultsMap.get(pair.getKey());

			// Resultados para una necesidad
			double precision = precision(qrels_list, results_list, -1);
			double recall = recall(qrels_list, results_list, -1);
			double f1 = (2 * precision * recall) / (precision + recall);
			double precision10 = precision(qrels_list, results_list, 10);
			double averg_precision = average_precision(qrels_list, results_list, -1);
			List<PrecisionRecall> recall_precision = recall_precision(qrels_list, results_list);
			List<PrecisionRecall> interpolated_recall_precision = interpolated_recall_precision(qrels_list, results_list, 0, 1, 0.1);

			// Agregación a los resultados totales
			total_precision += precision;
			total_recall += recall;
			total_precision10 += precision10;
			map += averg_precision;
			for (int i = 0; i < interpolated_recall_precision.size(); i++) {
				try {
					total_interpolated_recall_precision.get(i).precision += interpolated_recall_precision
							.get(i).precision;
					total_interpolated_recall_precision.get(i).recall += interpolated_recall_precision.get(i).recall;
				} catch (IndexOutOfBoundsException e) {
					total_interpolated_recall_precision.add(i,
							new PrecisionRecall(interpolated_recall_precision.get(i).precision,
									interpolated_recall_precision.get(i).recall));
				}
			}
			if (interpolated_recall_precision.size() < interpolated_min)
				interpolated_min = interpolated_recall_precision.size();

			// Escritura de los resultados de una necesidad
			out.println("INFORMATION_NEED " + pair.getKey());
			out.printf("precision %.3f\n", precision);
			out.printf("recall %.3f\n", recall);
			out.printf("F1 %.3f\n", f1);
			out.printf("prec@10 %.3f\n", precision10);
			out.printf("average_precision %.3f\n", averg_precision);
			out.println("recall_precision");
			for (PrecisionRecall d : recall_precision) {
				out.printf("%.3f\t%.3f\n", d.recall, d.precision);
			}
			out.println("interpolated_recall_precision");
			for (PrecisionRecall d : interpolated_recall_precision) {
				out.printf("%.3f\t%.3f\n", d.recall, d.precision);
			}
			out.println();
		}

		// Escritura de los resultados totales
		out.println("TOTAL");
		out.printf("precision %.3f\n", total_precision / qrelsMap.size());
		out.printf("recall %.3f\n", total_recall / qrelsMap.size());
		out.printf("F1 %.3f\n", (2*total_precision*total_recall/(total_precision+total_recall))/qrelsMap.size());
		out.printf("prec@10 %.3f\n", total_precision10 / qrelsMap.size());
		out.printf("MAP %.3f\n", map / qrelsMap.size());
		out.println("interpolated_recall_precision");
		for (int i = 0; i < interpolated_min; i++) {
			out.printf("%.3f\t%.3f\n", total_interpolated_recall_precision.get(i).recall / qrelsMap.size(), 
					total_interpolated_recall_precision.get(i).precision / qrelsMap.size());
		}

		it.remove();
		out.close();
	}

	/**
	 * Devuelve un HashMap con los juicios de relevancia de cada documento agrupados por 
	 * su identificador.
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	private static HashMap<String, List<QrelValue>> getQrels(String file) throws FileNotFoundException {
		List<QrelValue> values = null;
		HashMap<String, List<QrelValue>> qrels = new HashMap<String, List<QrelValue>>();
		Scanner s = new Scanner(new File(file));

		while (s.hasNextLine()) {
			String information_need = s.next();
			String document_id = s.next();
			int relevancy = Integer.valueOf(s.next());
			if (!qrels.containsKey(information_need))
				values = new ArrayList<QrelValue>();
			values.add(new QrelValue(document_id, relevancy));
			qrels.put(information_need, values);
			s.nextLine();
		}
		s.close();
		return qrels;
	}

	/**
	 * Devuelve un HashMap con los resultados obtenidos para cada documento agrupados
	 * por su identificador.
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 */
	private static HashMap<String, List<String>> getResults(String file) throws FileNotFoundException {
		List<String> values = null;
		HashMap<String, List<String>> qrels = new HashMap<String, List<String>>();
		Scanner s = new Scanner(new File(file));

		while (s.hasNextLine()) {
			String information_need = s.next();
			String document_id = s.next();
			if (!qrels.containsKey(information_need))
				values = new ArrayList<String>();
			values.add(document_id);
			qrels.put(information_need, values);
			s.nextLine();
		}
		s.close();
		return qrels;
	}

	/**
	 * PRECISION: Fraccion de documentos recuperados que son relevantes
	 * 
	 * @param qrels
	 * @param results
	 * @param k
	 * @return
	 */
	private static double precision(List<QrelValue> qrels, List<String> results, int k) {
		double totalDocs = 0;
		double relevantDocs = 0;
		if (k < 0)
			k = results.size();
		for (int i = 0; i < qrels.size(); i++) {
			for (int j = 0; j < k; j++) {
				if (qrels.get(i).document_id.equals(results.get(j)) && qrels.get(i).relevancy == 1) {
					relevantDocs++;
				}
			}
			totalDocs++;
		}
		totalDocs = k != qrels.size() ? k : results.size();
		return relevantDocs / totalDocs;
	}

	/**
	 * RECALL: Fraccion de documentos relevantes que se recuperan
	 * 
	 * @param qrels
	 * @param results
	 * @return
	 */
	private static double recall(List<QrelValue> qrels, List<String> results, int k) {
		double relevantDocsRecupered = 0;
		double relevantDocs = 0;

		if (k < 0)
			k = results.size();
		for (int j = 0; j < qrels.size(); j++) {
			if (qrels.get(j).relevancy == 1) {
				relevantDocs++;
			}
		}
		
		for (int i = 0; i < qrels.size(); i++) {
			for (int j = 0; j < k; j++) {
				if (qrels.get(i).document_id.equals(results.get(j)) && qrels.get(i).relevancy == 1) {
					relevantDocsRecupered++;
				}
			}
		}
		return relevantDocsRecupered / relevantDocs;
	}

	/**
	 * PRECISION PROMEDIO (MAP): Promedia el valor de la precisión obtenido para 
	 * los k documentos top, cada vez que se recupera un documento relevante.
	 * 
	 * Ejemplo: 3 documentos relevantes, se recupera el 1 y el
	 * 2 average_precision = (1/1 + 2/3)/3
	 * 
	 * @param qrels
	 * @param results
	 * @param k
	 * @return
	 */
	private static double average_precision(List<QrelValue> qrels, List<String> results, int k) {
		double relevantDocs = 0;
		double precision = 0;
		if (k < 0)
			k = qrels.size();
		for (int i = 0; i < results.size(); i++) {
			for (int j = 0; j < k; j++) {
				if (qrels.get(j).document_id.equals(results.get(i)) && qrels.get(j).relevancy == 1) {
					relevantDocs++;
					precision += relevantDocs / (i + 1);
					break;
				}
			}
		}
		if (relevantDocs == 0) return 0;
		return precision / relevantDocs;
	}
	
	/**
	 * RECALL - PRECISION
	 * @param qrels
	 * @param results
	 * @return
	 */
	private static List<PrecisionRecall> recall_precision(List<QrelValue> qrels, List<String> results) {
		List<PrecisionRecall> pr = new ArrayList<PrecisionRecall>();
		List<PrecisionRecall> clean_pr = new ArrayList<PrecisionRecall>();
		for (int i = 0; i < results.size(); i++) {
			double precision = precision(qrels, results, i + 1);
			double recall = recall(qrels, results, i + 1);
			pr.add(new PrecisionRecall(precision, recall));
		}
		
		double current_recall = 0;
		double max_precision = 0;
		for (int r = 0; r < pr.size(); r++) {
			if (current_recall < pr.get(r).recall) {
				current_recall = pr.get(r).recall;
				max_precision = 0;
				for (int j = 0; j < pr.size(); j++) {
					if (pr.get(j).recall == current_recall) {
						if (pr.get(j).precision > max_precision) {
							max_precision = pr.get(j).precision;
							clean_pr.add(new PrecisionRecall(max_precision, current_recall));
						}
					} else if (pr.get(j).recall > current_recall) {
						break;
					}
				}
			}
		}
		return clean_pr;
	}

	/**
	 * INTERPOLATED RECALL - PRECISION: La precisión interpolada p a un nivel de 
	 * exhaustividad r se define como la precisión más alta encontrada para cualquier 
	 * nivel de exhaustividad r’>=r.
	 * 
	 * @param qrels
	 * @param results
	 * @param min
	 * @param max
	 * @param offset
	 * @return
	 */
	private static List<PrecisionRecall> interpolated_recall_precision(List<QrelValue> qrels, List<String> results,
			double min, double max, double offset) {
		List<PrecisionRecall> pr = new ArrayList<PrecisionRecall>();
		List<PrecisionRecall> interpolated = new ArrayList<PrecisionRecall>();
		for (int i = 0; i < results.size(); i++) {
			double precision = precision(qrels, results, i + 1);
			double recall = recall(qrels, results, i + 1);
			pr.add(new PrecisionRecall(precision, recall));
		}
		for (double recall = min; recall <= max; recall += offset) {
			double precision = max(pr, recall);
			//if (precision != 0) 
			interpolated.add(new PrecisionRecall(precision, recall));
		}
		return interpolated;
	}

	/**
	 * Devuelve, si existe, el maximo valor de precision para un cierto recall rprima.
	 * @param pr
	 * @param rprima
	 * @param offset
	 * @return
	 */
	private static double max(List<PrecisionRecall> pr, double rprima) {
		double max = 0;
		for (int i=0; i < pr.size(); i++) {
			if (rprima <= pr.get(i).recall) {
				if (pr.get(i).precision >= max) {
					max = pr.get(i).precision;
				}
			}
		}
		return max;
	}
}
