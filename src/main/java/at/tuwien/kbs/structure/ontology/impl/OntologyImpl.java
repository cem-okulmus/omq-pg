package at.tuwien.kbs.structure.ontology.impl;

import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.ontology.exception.NotOWL2QLException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileReport;

import java.io.File;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class that wraps OWLOntolgy objects from the OWL API.
 */
public class OntologyImpl implements Ontology {

    /**
     * The OWL2 QL ontology to be used in QA.
     */
    private final OWLOntology ontology;
    private final OWLOntologyManager manager;
    /**
     * A Map that maps simple class names/labels to the class in the ontology.
     */
    private HashMap<String, OWLClass> classMap;
    /**
     * A Map that maps simple role names to the properties in the ontology.
     */
    private HashMap<String, OWLObjectProperty> propertyMap;

    /**
     * Initialize a new Ontology Wrapper from a file.
     * @param path The path to the ontology file.
     * @throws OWLOntologyCreationException If the ontology could not be loaded.
     * @throws NotOWL2QLException If the ontology is not in OWL2 QL.
     */
    public OntologyImpl(String path) throws OWLOntologyCreationException, NotOWL2QLException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(path));
        OWLProfileReport report = new OWL2QLProfile().checkOntology(ontology);
        if (!report.isInProfile()) {
            throw new NotOWL2QLException();
        }
        this.ontology = ontology;
        this.manager = manager;
        generateClassMap();
        generatePropertyMap();
    }


    public OWLClass addClass(String name) {

        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLEntity entity = df.getOWLEntity(EntityType.CLASS, IRI.create(name));
        OWLAxiom declare = df.getOWLDeclarationAxiom(entity);
        manager.addAxiom(this.ontology,declare);

        return (OWLClass) entity;
    }


    /**
     * Generate a map "A" -> ...#A (as OWLClass) from the ontology's signature.
     */
    private void generateClassMap() {
        this.classMap = new HashMap<>();
        for (OWLClass c : this.ontology.getClassesInSignature()) {
            if (!c.isTopEntity()) {
                classMap.put(c.getIRI().getShortForm(), c);
            }
        }
    }

    /**
     * Generate a map "r" -> ...#r (as OWLProperty) from the ontology's signature.
     */
    private void generatePropertyMap() {
        this.propertyMap = new HashMap<>();
        for (OWLObjectProperty p : this.ontology.getObjectPropertiesInSignature()) {
            propertyMap.put(p.getIRI().getShortForm(), p);
        }
    }

    /**
     * Get the class map
     * @return Map of simple class names and their OWLClasses
     */
    @Override
    public HashMap<String, OWLClass> getClassMap() {
        return classMap;
    }

    /**
     * Get the object property map
     * @return Map of object property names and their OWLObjectProperties
     */
    @Override
    public HashMap<String, OWLObjectProperty> getPropertyMap() {
        return propertyMap;
    }

    /**
     * Get the OWL2 QL Ontology.
     * @return The ontology OWLAPI object
     */
    @Override
    public OWLOntology getOntology() {
        return ontology;
    }

    @Override
    public Set<OWLAxiom> getAxioms() {
        return this.ontology.getAxioms();
    }
}
