package at.tuwien.kbs.structure.ontology;

import org.semanticweb.owlapi.model.*;
import java.util.Map;
import java.util.Set;

/**
 * An interface that describes a wrapper for OWLOntology objects from the OWL API.
 */
public interface Ontology {

    /**
     * Get the class map.
     * @return Map of simple class names and their OWLClasses.
     */
    public Map<String, OWLClass> getClassMap();

    /**
     * Get the object property map.
     * @return Map of object property names and their OWLObjectProperties.
     */
    public Map<String, OWLObjectProperty> getPropertyMap();

    /**
     * Get the OWL2 QL Ontology.
     * @return The ontology OWLAPI object.
     */
    public OWLOntology getOntology();

    /**
     * Get the axioms of the ontology.
     * @return Set of OWL QL axioms.
     */
    public Set<OWLAxiom> getAxioms();

}
