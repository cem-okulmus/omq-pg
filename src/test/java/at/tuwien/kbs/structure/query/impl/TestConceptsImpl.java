package at.tuwien.kbs.structure.query.impl;

import at.tuwien.kbs.logic.Rewriter;
import at.tuwien.kbs.logic.impl.RewriterImpl;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.ontology.exception.NotOWL2QLException;
import at.tuwien.kbs.structure.ontology.impl.OntologyImpl;
import at.tuwien.kbs.structure.query.Atom;
import at.tuwien.kbs.structure.query.Concepts;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestConceptsImpl {
    @Test
    public void testEqualConcepts() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        Concepts c1 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))),
                new VariableImpl("x"));
        Concepts c2 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))),
                new VariableImpl("x"));

        assertEquals(c1, c2);

        c1 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))),
                new UnboundVariableImpl("x"));
        c2 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))),
                new UnboundVariableImpl("y"));

        assertEquals(c1, c2);
    }

    @Test
    public void testUnequalConcepts() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        Concepts c1 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new VariableImpl("x"));
        Concepts c2 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Student"))), new VariableImpl("x"));

        assertNotEquals(c1, c2);

        c1 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new VariableImpl("x"));
        c2 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new VariableImpl("y"));

        assertNotEquals(c1, c2);

        c1 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new VariableImpl("x"));
        c2 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new UnboundVariableImpl("y"));

        assertNotEquals(c1, c2);
    }

    @Test
    public void testSetOfEqualConcepts() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        Concepts c1 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new VariableImpl("x"));
        Concepts c2 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new VariableImpl("x"));

        assertEquals(c1, c2);

        HashSet<Atom> set1 = new HashSet<>(Arrays.asList(c1, c2));
        HashSet<Atom> set2 = new HashSet<>();
        set2.add(c1);

        assertEquals(set2, set1);

        c1 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new UnboundVariableImpl("x"));
        c2 = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new UnboundVariableImpl("y"));

        assertEquals(c1, c2);

        set1 = new HashSet<>(Arrays.asList(c1, c2));
        set2 = new HashSet<>();
        set2.add(c1);

        assertEquals(set2, set1);
    }

    @Test
    public void testApplicable() throws OWLOntologyCreationException, NotOWL2QLException {
        // load ontology
        File resourcesDirectory = new File("src/test/resources/university.owl");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath());
        Concepts a = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new VariableImpl("x"));

        Set<OWLAxiom> applicableAxioms = new HashSet<>();

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (a.applicable(I)) {
                applicableAxioms.add(I);
            }
        }

        assertEquals(2, applicableAxioms.size());
    }

    @Test
    public void testApplicableWithSaturate() throws OWLOntologyCreationException, NotOWL2QLException {
        // load ontology
        File resourcesDirectory = new File("src/test/resources/university.owl");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath());
        Concepts a = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new VariableImpl("x"));
        a.saturate(o);

        Set<OWLAxiom> applicableAxioms = new HashSet<>();

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (a.applicable(I)) {
                applicableAxioms.add(I);
            }
        }

        assertEquals(1, applicableAxioms.size());
    }

    @Test
    public void testReplace() throws OWLOntologyCreationException, NotOWL2QLException {
        // load ontology
        File resourcesDirectory = new File("src/test/resources/university.owl");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath());
        Concepts a = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new VariableImpl("x"));
        a.saturate(o);
        Rewriter rewriter = new RewriterImpl();

        Set<Atom> rewritten = new HashSet<>(Collections.singleton(a));
        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (a.applicable(I)) {
                rewritten.add(a.replace(I, rewriter));
            }
        }
        assertEquals(2, rewritten.size());
        assertEquals(new HashSet<>(
                        Arrays.asList(
                                new ConceptsImpl(new HashSet<>(Arrays.asList(o.getClassMap().get("Professor"),
                                        o.getClassMap().get("Assistant_Prof"))), new VariableImpl("x")),
                                new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("teaches"))),
                                        new VariableImpl("x"), new UnboundVariableImpl("v1")))),
                rewritten);

        a = new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Course"))), new VariableImpl("y"));
        a.saturate(o);
        rewritten = new HashSet<>(Collections.singleton(a));
        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (a.applicable(I)) {
                rewritten.add(a.replace(I, rewriter));
            }
        }
        assertEquals(2, rewritten.size());
        assertEquals(new HashSet<>(Arrays.asList(new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Course"))), new VariableImpl("y")),
                new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("teaches").getInverseProperty())),
                        new VariableImpl("y"), new UnboundVariableImpl("v2")))), rewritten);
    }

}