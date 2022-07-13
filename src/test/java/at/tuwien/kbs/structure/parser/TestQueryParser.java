package at.tuwien.kbs.structure.parser;

import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.ontology.exception.NotOWL2QLException;
import at.tuwien.kbs.structure.ontology.impl.OntologyImpl;
import at.tuwien.kbs.structure.parser.impl.QueryParserImpl;
import at.tuwien.kbs.structure.query.Atom;
import at.tuwien.kbs.structure.query.Query;
import at.tuwien.kbs.structure.query.Variable;
import at.tuwien.kbs.structure.query.impl.*;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQueryParser {

    @Test
    public void testQueryConcept() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        QueryParserImpl queryParser = new QueryParserImpl(o);

        Query q = queryParser.parse("q(x):-Assistant_Prof(x)");

        List<Variable> head = new LinkedList<>();
        head.add(new VariableImpl("x"));
        HashSet<Atom> body = new HashSet<>();
        body.add(new ConceptsImpl(Collections.singleton(o.getClassMap().get("Assistant_Prof")), new VariableImpl("x")));

        QueryImpl q1 = new QueryImpl(head, body);

        assertEquals(q1, q);
    }

    @Test
    public void testQueryConcepts() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        QueryParserImpl queryParser = new QueryParserImpl(o);

        Query q = queryParser.parse("q(x):-Assistant_Prof(x),Professor(y)");

        List<Variable> head = new LinkedList<>();
        head.add(new VariableImpl("x"));
        HashSet<Atom> body = new HashSet<>();
        body.add(new ConceptsImpl(Collections.singleton(o.getClassMap().get("Assistant_Prof")), new VariableImpl("x")));
        body.add(new ConceptsImpl(Collections.singleton(o.getClassMap().get("Professor")), new VariableImpl("y")));

        QueryImpl q1 = new QueryImpl(head, body);

        assertEquals(q1, q);
    }

    @Test
    public void testQueryConceptDisjunction() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        QueryParserImpl queryParser = new QueryParserImpl(o);

        Query q = queryParser.parse("q(x):-(Assistant_Prof|Professor)(x)");

        List<Variable> head = new LinkedList<>();
        head.add(new VariableImpl("x"));
        HashSet<Atom> body = new HashSet<>();
        body.add(new ConceptsImpl(new HashSet<>(Arrays.asList(o.getClassMap().get("Assistant_Prof"),
                o.getClassMap().get("Professor"))), new VariableImpl("x")));

        QueryImpl q1 = new QueryImpl(head, body);

        assertEquals(q1, q);
    }

    @Test
    public void testQueryRole() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        QueryParserImpl queryParser = new QueryParserImpl(o);

        Query q = queryParser.parse("q(x):-teaches(x,y)");

        List<Variable> head = new LinkedList<>();
        head.add(new VariableImpl("x"));
        HashSet<Atom> body = new HashSet<>();
        body.add(new RolesImpl(Collections.singleton(o.getPropertyMap().get("teaches")),
                new VariableImpl("x"), new VariableImpl("y")));

        QueryImpl q1 = new QueryImpl(head, body);

        assertEquals(q1, q);
    }

    @Test
    public void testQueryRoleWithTwoVariables() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        QueryParserImpl queryParser = new QueryParserImpl(o);

        Query q = queryParser.parse("q(x,y):-teaches(x,y)");

        List<Variable> head = new LinkedList<>();
        head.add(new VariableImpl("x"));
        head.add(new VariableImpl("y"));
        HashSet<Atom> body = new HashSet<>();
        body.add(new RolesImpl(Collections.singleton(o.getPropertyMap().get("teaches")),
                new VariableImpl("x"), new VariableImpl("y")));

        QueryImpl q1 = new QueryImpl(head, body);

        assertEquals(q1, q);
    }

    @Test
    public void testDoubleConcept() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        QueryParserImpl queryParser = new QueryParserImpl(o);

        Query q = queryParser.parse("q(x):-Assistant_Prof(x),Assistant_Prof(x)");

        List<Variable> head = new LinkedList<>();
        head.add(new VariableImpl("x"));
        HashSet<Atom> body = new HashSet<>();
        body.add(new ConceptsImpl(Collections.singleton(o.getClassMap().get("Assistant_Prof")), new VariableImpl("x")));

        QueryImpl q1 = new QueryImpl(head, body);

        assertEquals(q1, q);
    }

    @Test
    public void testDoubleConceptWithDifferentVariables() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        QueryParserImpl queryParser = new QueryParserImpl(o);

        Query q = queryParser.parse("q(x):-Assistant_Prof(x),Assistant_Prof(y)");

        List<Variable> head = new LinkedList<>();
        head.add(new VariableImpl("x"));
        HashSet<Atom> body = new HashSet<>();
        body.add(new ConceptsImpl(Collections.singleton(o.getClassMap().get("Assistant_Prof")), new VariableImpl("x")));
        body.add(new ConceptsImpl(Collections.singleton(o.getClassMap().get("Assistant_Prof")), new VariableImpl("y")));

        QueryImpl q1 = new QueryImpl(head, body);

        assertEquals(q1, q);
    }

    @Test
    public void testQueryRoles() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");
        QueryParserImpl queryParser = new QueryParserImpl(o);

        Query q = queryParser.parse("q(x):-(s|r-|t|r)(x,y)");

        List<Variable> head = new LinkedList<>();
        head.add(new VariableImpl("x"));
        HashSet<Atom> body = new HashSet<>();

        body.add(new RolesImpl(new HashSet<>(Arrays.asList(
                o.getPropertyMap().get("s"),
                o.getPropertyMap().get("r").getInverseProperty(),
                o.getPropertyMap().get("t"),
                o.getPropertyMap().get("r"))),
                new VariableImpl("x"), new VariableImpl("y")));

        QueryImpl q1 = new QueryImpl(head, body);

        assertEquals(q1, q);
    }

    @Test
    public void testQueryArbitraryLengthRole() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");
        QueryParserImpl queryParser = new QueryParserImpl(o);

        Query q = queryParser.parse("q(x):-r*(x,y)");

        List<Variable> head = new LinkedList<>();
        head.add(new VariableImpl("x"));
        HashSet<Atom> body = new HashSet<>();

        body.add(new ArbitraryLengthRolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new VariableImpl("y")));

        QueryImpl q1 = new QueryImpl(head, body);

        assertEquals(q1, q);
    }

    @Test
    public void testQueryArbitraryLengthRoles() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");
        QueryParserImpl queryParser = new QueryParserImpl(o);

        Query q = queryParser.parse("q(x):-(r|s)*(x,y)");

        List<Variable> head = new LinkedList<>();
        head.add(new VariableImpl("x"));
        HashSet<Atom> body = new HashSet<>();

        body.add(new ArbitraryLengthRolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("r"), o.getPropertyMap().get("s"))),
                new VariableImpl("x"), new VariableImpl("y")));

        QueryImpl q1 = new QueryImpl(head, body);

        assertEquals(q1, q);
    }

    @Test
    public void testBoolean() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");
        QueryParserImpl queryParser = new QueryParserImpl(o);

        Query q = queryParser.parse("q():-Assistant_Prof(x)");

        List<Variable> head = new LinkedList<>();
        HashSet<Atom> body = new HashSet<>();
        body.add(new ConceptsImpl(Collections.singleton(o.getClassMap().get("Assistant_Prof")), new VariableImpl("x")));

        QueryImpl q1 = new QueryImpl(head, body);

        assertEquals(q1, q);
    }

}
