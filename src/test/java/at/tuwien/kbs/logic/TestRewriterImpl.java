package at.tuwien.kbs.logic;

import at.tuwien.kbs.logic.impl.RewriterImpl;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.ontology.exception.NotOWL2QLException;
import at.tuwien.kbs.structure.ontology.impl.OntologyImpl;
import at.tuwien.kbs.structure.query.Query;
import at.tuwien.kbs.structure.query.Variable;
import at.tuwien.kbs.structure.query.impl.*;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestRewriterImpl {

    @Test
    public void testCQUniversity() throws OWLOntologyCreationException, NotOWL2QLException {
        // load ontology
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university.owl");

        Rewriter rewriter = new RewriterImpl();
        Query q;
        Set<Query> Q;
        // q(x):-teaches(x,y), Course(y)
        q = new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                new HashSet<>(Arrays.asList(
                        new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("teaches"))),
                                new VariableImpl("x"), new VariableImpl("y")),
                        new ConceptsImpl(Collections.singleton(o.getClassMap().get("Course")), new VariableImpl("y"))
                )));

        Q = rewriter.rewrite(q, o);

        assertEquals(4, Q.size());

        Set<Query> Qp = new HashSet<>();
        Qp.add(new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                new HashSet<>(Arrays.asList(
                        new RolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("teaches"),
                                o.getPropertyMap().get("taughtBy").getInverseProperty())),
                                new VariableImpl("x"), new VariableImpl("y")),
                        new ConceptsImpl(Collections.singleton(o.getClassMap().get("Course")), new VariableImpl("y"))
                ))));
        Qp.add(new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                new HashSet<>(Arrays.asList(
                        new RolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("teaches"),
                                o.getPropertyMap().get("taughtBy").getInverseProperty())),
                                new VariableImpl("x"), new VariableImpl("y")),
                        new RolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("teaches").getInverseProperty(),
                                o.getPropertyMap().get("taughtBy"))),
                                new VariableImpl("y"), new UnboundVariableImpl("z"))
                ))));
        Qp.add(new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                new HashSet<>(Collections.singleton(
                        new RolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("teaches").getInverseProperty(),
                                o.getPropertyMap().get("taughtBy"))),
                                new UnboundVariableImpl("y"), new VariableImpl("x")).getInverse()
                ))));
        Qp.add(new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                new HashSet<>(Collections.singleton(
                        new ConceptsImpl(new HashSet<>(Arrays.asList(o.getClassMap().get("Professor"), o.getClassMap().get("Assistant_Prof"))), new VariableImpl("x"))
                ))));

        assertEquals(Qp, Q);
    }

    @Test
    public void testCQUniversity2() throws OWLOntologyCreationException, NotOWL2QLException {
        // load ontology
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university2.ttl");

        Rewriter rewriter = new RewriterImpl();
        Query q;
        Set<Query> Q;

        // q(x):-supervisedBy(x,y), Professor(y)
        // this query needs to merge two roles atoms at some point such that
        // GradStudent(x) (and its subclasses) can be derived from the query.
        q = new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                new HashSet<>(Arrays.asList(
                        new RolesImpl(new HashSet<>(Collections.singleton(
                                o.getPropertyMap().get("isSupervisedBy"))),
                                new VariableImpl("x"), new VariableImpl("y")),
                        new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new VariableImpl("y"))
                )));

        Q = rewriter.rewrite(q, o);

        assertEquals(8, Q.size());
    }
}
