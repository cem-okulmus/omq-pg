package at.tuwien.kbs.structure.ontology;

import at.tuwien.kbs.structure.ontology.exception.NotOWL2QLException;
import at.tuwien.kbs.structure.ontology.impl.OntologyImpl;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestOntology {
    @Test
    public void testCompliantOntology() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        OntologyImpl o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
    }

    @Test
    public void testSubClassOf() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        OntologyImpl o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");

        HashSet<OWLClassExpression> subclasses = new HashSet<>();

        for (OWLAxiom a : o.getOntology().getAxioms()) {
            if (a instanceof OWLSubClassOfAxiom) {
                if(((OWLSubClassOfAxiom) a).getSuperClass() == o.getClassMap().get("Professor")) {
                    subclasses.add(((OWLSubClassOfAxiom) a).getSubClass());
                }
            }
        }

        HashSet<OWLClass> h = new HashSet<>();
        h.add(o.getClassMap().get("Assistant_Prof"));
        assertEquals(subclasses, h);
    }

    @Test
    public void testSubClassOfWithFoaf() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        OntologyImpl o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university2.ttl");

        HashSet<OWLClassExpression> subclasses = new HashSet<>();

        for (OWLAxiom a : o.getOntology().getAxioms()) {
            if (a instanceof OWLSubClassOfAxiom) {
                if(((OWLSubClassOfAxiom) a).getSuperClass() == o.getClassMap().get("Person")) {
                    subclasses.add(((OWLSubClassOfAxiom) a).getSubClass());
                }
            }
        }

        HashSet<OWLClass> h = new HashSet<>();
        h.add(o.getClassMap().get("Student"));
        h.add(o.getClassMap().get("FacultyMember"));
        assertEquals(subclasses, h);
    }
}
