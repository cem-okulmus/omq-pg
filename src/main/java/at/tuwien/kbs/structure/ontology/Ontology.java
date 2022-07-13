package at.tuwien.kbs.structure.ontology;

import org.semanticweb.owlapi.model.*;
import java.util.Map;
import java.util.Set;

public interface Ontology {

    public Map<String, OWLClass> getClassMap();

    public Map<String, OWLObjectProperty> getPropertyMap();

    public OWLOntology getOntology();

    public Set<OWLAxiom> getAxioms();

}
