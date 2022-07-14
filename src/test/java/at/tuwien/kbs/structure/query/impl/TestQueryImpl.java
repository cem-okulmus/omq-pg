package at.tuwien.kbs.structure.query.impl;

import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.ontology.exception.NotOWL2QLException;
import at.tuwien.kbs.structure.ontology.impl.OntologyImpl;
import at.tuwien.kbs.structure.query.Query;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestQueryImpl {

    @Test
    public void testEqualQueries() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        Query q1 = new QueryImpl(new LinkedList<>(Collections.singletonList(new VariableImpl("x"))),
                new HashSet<>(Collections.singleton(new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))),
                        new VariableImpl("x")))));

        Query q2 = new QueryImpl(new LinkedList<>(Collections.singletonList(new VariableImpl("x"))),
                new HashSet<>(Collections.singletonList(new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))),
                        new VariableImpl("x")))));

        assertEquals(q1, q2);
    }

    @Test
    public void testUnequalQueries() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        Query q1 = new QueryImpl(new LinkedList<>(Collections.singletonList(new VariableImpl("x"))),
                new HashSet<>(Collections.singleton(new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))),
                        new VariableImpl("x")))));

        Query q2 = new QueryImpl(new LinkedList<>(Collections.singletonList(new VariableImpl("x"))),
                new HashSet<>(Collections.singletonList(new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))),
                        new VariableImpl("y")))));

        assertNotEquals(q1, q2);
    }

    @Test
    public void testSaturateQuery() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        Query q1 = new QueryImpl(new LinkedList<>(Collections.singletonList(new VariableImpl("x"))),
                new HashSet<>(Collections.singleton(new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))),
                        new VariableImpl("x")))));

        q1.saturate(o);

        Set<Query> queries;

        Query q2 = new QueryImpl(new LinkedList<>(Collections.singletonList(new VariableImpl("x"))),
                new HashSet<>(Collections.singletonList(new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))),
                        new VariableImpl("x")))));

        queries = new HashSet<>(Arrays.asList(q1, q2));

        assertEquals(2, queries.size());

        q2 = new QueryImpl(new LinkedList<>(Collections.singletonList(new VariableImpl("x"))),
                new HashSet<>(Collections.singletonList(new ConceptsImpl(new HashSet<>(Arrays.asList(o.getClassMap().get("Professor"), o.getClassMap().get("Assistant_Prof"))),
                        new VariableImpl("x")))));

        queries = new HashSet<>(Arrays.asList(q1, q2));

        assertEquals(1, queries.size());
    }
}
