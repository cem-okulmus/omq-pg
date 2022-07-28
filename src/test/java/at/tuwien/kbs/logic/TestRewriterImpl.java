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
                        new ConceptsImpl(new HashSet<>(Arrays.asList(o.getClassMap().get("Professor"),
                                o.getClassMap().get("Assistant_Prof"))), new VariableImpl("x"))
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

        assertEquals(4, Q.size());

        Set<Query> Qp = new HashSet<>();

        Qp.add(new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                new HashSet<>(Arrays.asList(
                        new RolesImpl(new HashSet<>(Arrays.asList(
                                o.getPropertyMap().get("isSupervisedBy").getInverseProperty(),
                                o.getPropertyMap().get("gradStudentSupervisedBy").getInverseProperty())),
                                new VariableImpl("y"), new VariableImpl("x")),
                        new ConceptsImpl(new HashSet<>(Arrays.asList(
                                o.getClassMap().get("Professor"),
                                o.getClassMap().get("AssistantProfessor"),
                                o.getClassMap().get("AssociateProfessor"),
                                o.getClassMap().get("FullProfessor"))),
                                new VariableImpl("y"))
                ))));

        Qp.add(new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                new HashSet<>(Collections.singleton(
                        new ConceptsImpl(new HashSet<>(Arrays.asList(
                                o.getClassMap().get("GraduateStudent"),
                                o.getClassMap().get("PhDStudent"))),
                                new VariableImpl("x"))
                ))));

        Qp.add(new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                new HashSet<>(Arrays.asList(
                        new RolesImpl(
                                new HashSet<>(Arrays.asList(
                                o.getPropertyMap().get("isSupervisedBy").getInverseProperty(),
                                o.getPropertyMap().get("gradStudentSupervisedBy").getInverseProperty())),
                                new VariableImpl("y"), new VariableImpl("x")),
                        new RolesImpl(
                                new HashSet<>(Collections.singleton(
                                o.getPropertyMap().get("gradStudentSupervisedBy"))),
                                new UnboundVariableImpl("z"), new VariableImpl("y"))
                ))));

        Qp.add(new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                new HashSet<>(Collections.singleton(
                        new RolesImpl(
                                new HashSet<>(Collections.singleton(
                                        o.getPropertyMap().get("gradStudentSupervisedBy"))),
                                new VariableImpl("x"), new UnboundVariableImpl("z"))
                ))));

        assertEquals(Qp, Q);
    }

    @Test
    public void testCRPQWithConcatenationTwoTailArbitrary() throws OWLOntologyCreationException, NotOWL2QLException {
        // load ontology
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/paths1.owl");

        Rewriter rewriter = new RewriterImpl();
        Query q;
        Set<Query> Q;

        // q():-t(y,z1),s*(z1,z2),r(z2,x)
        q = new QueryImpl(new LinkedList<>(),
                new HashSet<>(Arrays.asList(
                        new RolesImpl(
                                new HashSet<>(Collections.singleton(o.getPropertyMap().get("t"))),
                                new VariableImpl("y"), new VariableImpl("z1")),
                        new ArbitraryLengthRolesImpl(
                                new HashSet<>(Collections.singleton(o.getPropertyMap().get("s"))),
                                new VariableImpl("z1"), new VariableImpl("z2")),
                        new RolesImpl(
                                new HashSet<>(Collections.singleton(o.getPropertyMap().get("r"))),
                                new VariableImpl("z2"), new VariableImpl("x"))
                )));

        Q = rewriter.rewrite(q, o);

        assertEquals(11, Q.size());
    }

    @Test
    public void testCRPQWithConcatenationOneTailArbOneAnswerVar() throws OWLOntologyCreationException, NotOWL2QLException {
        // load ontology
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/paths1.owl");

        Rewriter rewriter = new RewriterImpl();
        Query q;
        Set<Query> Q;

        // q(x):-t(y,z1),s*(z1,z2),r(z2,x)
        q = new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                new HashSet<>(Arrays.asList(
                        new RolesImpl(
                                new HashSet<>(Collections.singleton(o.getPropertyMap().get("t"))),
                                new VariableImpl("y"), new VariableImpl("z1")),
                        new ArbitraryLengthRolesImpl(
                                new HashSet<>(Collections.singleton(o.getPropertyMap().get("s"))),
                                new VariableImpl("z1"), new VariableImpl("z2")),
                        new RolesImpl(
                                new HashSet<>(Collections.singleton(o.getPropertyMap().get("r"))),
                                new VariableImpl("z2"), new VariableImpl("x"))
                )));

        Q = rewriter.rewrite(q, o);

        assertEquals(12, Q.size());
    }

    @Test
    public void testCRPQWithConcatenationNoDrop() throws OWLOntologyCreationException, NotOWL2QLException {
        // load ontology
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/paths2.owl");

        Rewriter rewriter = new RewriterImpl();
        Query q;
        Set<Query> Q;

        // q():-A(x),r*(x,y),B(y)
        q = new QueryImpl(new LinkedList<>(),
                new HashSet<>(Arrays.asList(
                        new ConceptsImpl(
                                new HashSet<>(Collections.singleton(o.getClassMap().get("A"))),
                                new VariableImpl("x")),
                        new ArbitraryLengthRolesImpl(
                                new HashSet<>(Collections.singleton(o.getPropertyMap().get("r"))),
                                new VariableImpl("x"), new VariableImpl("y")),
                        new ConceptsImpl(
                                new HashSet<>(Collections.singleton(o.getClassMap().get("B"))),
                                new VariableImpl("y"))
                )));

        Q = rewriter.rewrite(q, o);

        assertEquals(14, Q.size());
    }
}
