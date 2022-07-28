package at.tuwien.kbs.translation;

import at.tuwien.kbs.logic.Rewriter;
import at.tuwien.kbs.logic.impl.RewriterImpl;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.ontology.exception.NotOWL2QLException;
import at.tuwien.kbs.structure.ontology.impl.OntologyImpl;
import at.tuwien.kbs.structure.query.Query;
import at.tuwien.kbs.structure.query.Variable;
import at.tuwien.kbs.structure.query.impl.*;
import at.tuwien.kbs.translation.impl.CypherTranslator;
import com.google.errorprone.annotations.Var;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCypherTranslator {

    @Test
    public void testSingleQuery() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university2.ttl");
        Translator translator = new CypherTranslator();

        Set<Query> queries = new HashSet<>(Collections.singleton(
                new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                        new HashSet<>(Arrays.asList(
                                new RolesImpl(new HashSet<>(Collections.singleton(
                                        o.getPropertyMap().get("isSupervisedBy")
                                )), new VariableImpl("x"), new VariableImpl("y")),
                                new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor")))
                                        , new VariableImpl("y"))
                        )))
        ));

        System.out.println(translator.translate(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                queries));
    }

    @Test
    public void testSingleQueryAtom() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university2.ttl");
        Translator translator = new CypherTranslator();

        Set<Query> queries = new HashSet<>(Collections.singleton(
                new QueryImpl(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                        new HashSet<>(Collections.singleton(
                                new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor")))
                                        , new VariableImpl("y"))
                        )))
        ));

        System.out.println(translator.translate(new LinkedList<>(Collections.singleton(new VariableImpl("x"))),
                queries));
    }

    @Test
    public void testCQUniversityTranslation() throws OWLOntologyCreationException, NotOWL2QLException {
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
                        new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Course"))),
                                new VariableImpl("y"))
                )));

        Q = rewriter.rewrite(q, o);

        Translator translator = new CypherTranslator();

        String res = translator.translate(q.getHead(), Q);

        System.out.println(res);
    }

    @Test
    public void testCQUniversity2Translation() throws OWLOntologyCreationException, NotOWL2QLException {
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
                        new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))),
                                new VariableImpl("y"))
                )));

        Q = rewriter.rewrite(q, o);

        Translator translator = new CypherTranslator();

        String res = translator.translate(q.getHead(), Q);

        System.out.println(res);
    }

    @Test
    public void testCRPQWithConcatenationTwoTailArbitraryTranslation() throws OWLOntologyCreationException, NotOWL2QLException {
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

        Translator translator = new CypherTranslator();

        String res = translator.translate(q.getHead(), Q);

        System.out.println(res);
    }

    @Test
    public void testCRPQWithConcatenationOneTailArbOneAnswerVarTranslation() throws OWLOntologyCreationException, NotOWL2QLException {
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

        Translator translator = new CypherTranslator();

        String res = translator.translate(q.getHead(), Q);

        System.out.println(res);
    }

    @Test
    public void testCRPQWithConcatenationNoDropTranslation() throws OWLOntologyCreationException, NotOWL2QLException {
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

        Translator translator = new CypherTranslator();

        String res = translator.translate(q.getHead(), Q);

        System.out.println(res);

    }

}
