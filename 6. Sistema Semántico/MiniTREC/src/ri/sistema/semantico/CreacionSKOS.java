package ri.sistema.semantico;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * 
 * @author Rafael Marcén Altarriba (650435)
 * @version 1.0
 *
 */
public class CreacionSKOS {
	
	private static String SKOS = "http://www.w3.org/2004/02/skos/core#";
	private static String skosConceptURI = "http://www.w3.org/2004/02/skos/core#Concept";
	private static String URI = "http://www.minitrec.com/Subject/";
	private static String skosPrefLabelURI = "http://www.w3.org/2004/02/skos/core#prefLabel";
	private static String skosAltLabelURI = "http://www.w3.org/2004/02/skos/core#altLabel";
	private static String output = "semantico/skos.rdf";

	public static void main(String [] args) {
		/* Crea un nuevo modelo vacio */
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("skos", SKOS);
		
		Property prefLabel = model.createProperty(skosPrefLabelURI);
		Property altLabel = model.createProperty(skosAltLabelURI);
		
		/* Recursos sobre musica y sonido */	
		Resource musica = model.createResource(URI + "musica")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "musica");
		
		Resource sonido = model.createResource(URI + "sonido")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "sonido");
		
		Resource cancion = model.createResource(URI + "cancion")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "cancion");
		
		Resource ritmo = model.createResource(URI + "ritmo")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "ritmo");
		
		Resource melodia = model.createResource(URI + "melodia")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "melodia");
		
		Resource obra = model.createResource(URI + "obra")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "obra");
		
		
		/* Recursos sobre la Guerra de la Independencia */		
		Resource guerraIndependencia = model.createResource(URI + "guerraIndependencia")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "guerra de la independencia");
		
//		Resource historiaEspana = model.createResource(URI + "historiaEspana")
//				.addProperty(RDF.type, skosConceptURI)
//				.addProperty(prefLabel, "historia de españa");
		
		Resource ejercito = model.createResource(URI + "ejercito")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "ejercito");
		
		Resource tropasFrancesas = model.createResource(URI + "tropasFrancesas")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "tropas francesas");
		
//		Resource guerrilla = model.createResource(URI + "guerrilla")
//				.addProperty(RDF.type, skosConceptURI)
//				.addProperty(prefLabel, "Guerrilla");
		
		Resource CarlosIV = model.createResource(URI + "CarlosIV")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "carlos iv");
		
		/* Recursos sobre energias renovables */	
		Resource energia = model.createResource(URI + "energia")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "energia")
				.addProperty(altLabel, "energias");
		
		Resource energiaRenovable = model.createResource(URI + "energiaRenovable")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "energia renovable");
		
		Resource panel = model.createResource(URI + "panel")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "panel");
		
		Resource solar = model.createResource(URI + "solar")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "solar");
		
		Resource calor = model.createResource(URI + "calor")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "calor");
		
		Resource medioAmbiente = model.createResource(URI + "medioAmbiente")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "medio ambiente");
		
		Resource energiaTermica = model.createResource(URI + "energiaTermica")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "energia termica");
		
		Resource eolico = model.createResource(URI + "eolico")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "eolico");
		
		/* Recursos sobre videojuegos */		
		Resource videojuego = model.createResource(URI + "videojuego")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "videojuego");
		
		Resource disenoPersonajes = model.createResource(URI + "disenoPersonajes")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "diseño de personaje");
		
//		Resource desarrollo = model.createResource(URI + "desarrollo")
//				.addProperty(RDF.type, skosConceptURI)
//				.addProperty(prefLabel, "Desarrollo");

		Resource diseno = model.createResource(URI + "diseno")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "diseño");

		Resource personaje = model.createResource(URI + "personaje")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "personaje");

//		Resource agente = model.createResource(URI + "agente")
//				.addProperty(RDF.type, skosConceptURI)
//				.addProperty(prefLabel, "agente");

		Resource motorGrafico = model.createResource(URI + "motorGrafico")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "motor grafico");

		Resource virtual = model.createResource(URI + "virtual")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "virtual");

		Resource tresD = model.createResource(URI + "3d")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "3d");

		Resource jugabilidad = model.createResource(URI + "jugabilidad")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "jugabilidad");
		
		/* Recursos sobre arquitectura */
		
		Resource arquitectura = model.createResource(URI + "arquitectura")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "arquitectura");
		
//		Resource edadMedia = model.createResource(URI + "edadMedia")
//				.addProperty(RDF.type, skosConceptURI)
//				.addProperty(prefLabel, "edad media");
		
//		Resource gotico = model.createResource(URI + "gotico")
//				.addProperty(RDF.type, skosConceptURI)
//				.addProperty(prefLabel, "gotico");
		
		Resource renacimiento = model.createResource(URI + "renacimiento")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "renacimiento");
		
		Resource santomense = model.createResource(URI + "santomense")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "santomense");
		
		Resource patrimonio = model.createResource(URI + "patrimonio")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "patrimonio historico");
		
		Resource iglesia = model.createResource(URI + "iglesia")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "iglesia");
		
		Resource monasterio = model.createResource(URI + "monasterio")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "monasterio de piedra")
				.addProperty(altLabel, "monasterio de san juan de la peña");
		
		Resource castillo = model.createResource(URI + "castillo")
				.addProperty(RDF.type, skosConceptURI)
				.addProperty(prefLabel, "castillo");
		
		/**
		 * Narrower: “Laptop” is narrower than the concept “Computer”
		 * Broader : “Computer” is broader than the concept “Laptop”
		 * Related : Relacionado
		 */
		
		Property narrower = model.createProperty("http://www.w3.org/2004/02/skos/core#narrower");
		Property broader = model.createProperty("http://www.w3.org/2004/02/skos/core#broader");
		Property related = model.createProperty("http://www.w3.org/2004/02/skos/core#related");
		
		/* Relaciones entre conceptos de musica y sonido */
		sonido.addProperty(narrower, musica);
		//sonido.addProperty(narrower, ruido);
		
		musica.addProperty(broader, sonido);
		musica.addProperty(narrower, ritmo);
		musica.addProperty(narrower, melodia);
		musica.addProperty(related, cancion);
		
		ritmo.addProperty(broader, musica);
		ritmo.addProperty(related, melodia);
		ritmo.addProperty(related, cancion);
		
		melodia.addProperty(broader, musica);
		melodia.addProperty(related, ritmo);
		melodia.addProperty(related, cancion);
		
		cancion.addProperty(broader, musica);
		cancion.addProperty(related, ritmo);

		obra.addProperty(related, cancion);
		
		/* Relaciones entre conceptos de guerra de la independencia */
		//historiaEspana.addProperty(narrower, guerraIndependencia);
		
		//guerraIndependencia.addProperty(broader, historiaEspana);
		guerraIndependencia.addProperty(related, ejercito);
		//guerraIndependencia.addProperty(related, guerrilla);
		guerraIndependencia.addProperty(related, tropasFrancesas);
		guerraIndependencia.addProperty(related, CarlosIV);

		ejercito.addProperty(related, guerraIndependencia);
		ejercito.addProperty(related, tropasFrancesas);
		//ejercito.addProperty(related, guerrilla);

		tropasFrancesas.addProperty(related, guerraIndependencia);
		tropasFrancesas.addProperty(related, ejercito);
		
		//guerrilla.addProperty(related, guerraIndependencia);
		//guerrilla.addProperty(related, ejercito);
		
		//CarlosIV.addProperty(related, historiaEspana);
		CarlosIV.addProperty(related, guerraIndependencia);
		
		/* Relaciones sobre energias renovables */
		energia.addProperty(broader, energiaTermica);
		energia.addProperty(broader, energiaRenovable);
		
		panel.addProperty(related, solar);
		
		energiaRenovable.addProperty(narrower, energia);
		energiaRenovable.addProperty(narrower, solar);
		energiaRenovable.addProperty(narrower, eolico);
		//energiaRenovable.addProperty(related, medioAmbiente);
		energiaRenovable.addProperty(related, calor);
		energiaRenovable.addProperty(related, energiaTermica);
		
		//medioAmbiente.addProperty(related, energiaRenovable);
		
		energiaTermica.addProperty(related, energiaRenovable);
		energiaTermica.addProperty(related, calor);
		energiaTermica.addProperty(narrower, energia);
		
		solar.addProperty(related, panel);
		solar.addProperty(broader, energiaRenovable);
		
		eolico.addProperty(broader, energiaRenovable);
		
		calor.addProperty(related, energiaRenovable);
		calor.addProperty(related, energiaTermica);
		
		/* Relaciones sobre videojuegos */
		videojuego.addProperty(narrower, disenoPersonajes);
		videojuego.addProperty(related, personaje);
		videojuego.addProperty(related, virtual);
		videojuego.addProperty(related, jugabilidad);
		videojuego.addProperty(related, tresD);
		videojuego.addProperty(related, motorGrafico);

		disenoPersonajes.addProperty(broader, diseno);
		disenoPersonajes.addProperty(broader, videojuego);
		
		personaje.addProperty(related, videojuego);

		diseno.addProperty(narrower, disenoPersonajes);
		
		virtual.addProperty(related, videojuego);
		
		motorGrafico.addProperty(related, videojuego);
		
		tresD.addProperty(related, videojuego);
		
		jugabilidad.addProperty(related, videojuego);
		//desarrollo.addProperty(related, videojuego);
		//videojuego.addProperty(related, desarrollo);
		
		/* Relaciones sobre arquitectura */
		arquitectura.addProperty(broader, monasterio);
		//arquitectura.addProperty(broader, gotico);
		arquitectura.addProperty(broader, iglesia);
		arquitectura.addProperty(broader, castillo);
		arquitectura.addProperty(broader, santomense);
		//arquitectura.addProperty(related, edadMedia);
		arquitectura.addProperty(related, renacimiento);
		arquitectura.addProperty(related, patrimonio);

		iglesia.addProperty(narrower, arquitectura);
		
		monasterio.addProperty(narrower, arquitectura);
		
		//edadMedia.addProperty(related, arquitectura);
		
		//gotico.addProperty(narrower, arquitectura);
		
		renacimiento.addProperty(related, arquitectura);
		
		castillo.addProperty(narrower, arquitectura);
		
		santomense.addProperty(narrower, arquitectura);

		patrimonio.addProperty(related, arquitectura);
		
		/* Guarda el modelo en un fichero XML*/
		try {
			model.write(new FileOutputStream(new File(output)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}