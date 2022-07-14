package at.tuwien.kbs.logic;

import at.tuwien.kbs.logic.impl.SubstitutionImpl;
import at.tuwien.kbs.logic.impl.UnifierImpl;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.ontology.exception.NotOWL2QLException;
import at.tuwien.kbs.structure.ontology.impl.OntologyImpl;
import at.tuwien.kbs.structure.query.*;
import at.tuwien.kbs.structure.query.impl.*;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUnifierImpl {

    @Test
    public void testUnifier() {
        List<Term> t1 = Collections.singletonList(new VariableImpl("x"));
        List<Term> t2 = Collections.singletonList(new VariableImpl("y"));

        Unifier unifier = new UnifierImpl(t1, t2);
        assertEquals(new LinkedList<>(Collections
                        .singletonList(new SubstitutionImpl(new VariableImpl("x"), new VariableImpl("y")))),
                unifier.getSubstitutions());

        t1 = Collections.singletonList(new VariableImpl("y"));
        t2 = Collections.singletonList(new UnboundVariableImpl("x"));

        unifier = new UnifierImpl(t1, t2);

        assertEquals(new LinkedList<>(Collections
                        .singletonList(new SubstitutionImpl(new UnboundVariableImpl("x"), new VariableImpl("y")))),
                unifier.getSubstitutions());

        t1 = Arrays.asList(new VariableImpl("z1"), new VariableImpl("z"));
        t2 = Arrays.asList(new VariableImpl("x"), new VariableImpl("z1"));

        unifier = new UnifierImpl(t1, t2);

        assertEquals(new LinkedList<>(
                        Arrays.asList(new SubstitutionImpl(new VariableImpl("z1"), new VariableImpl("x")),
                                new SubstitutionImpl(new VariableImpl("z"), new VariableImpl("x")))),
                unifier.getSubstitutions());

        t1 = Arrays.asList(new UnboundVariableImpl("z1"), new VariableImpl("z"));
        t2 = Arrays.asList(new VariableImpl("x"), new UnboundVariableImpl("y"));

        unifier = new UnifierImpl(t1, t2);

        assertEquals(new LinkedList<>(
                        Arrays.asList(new SubstitutionImpl(new UnboundVariableImpl("z1"), new VariableImpl("x")),
                                new SubstitutionImpl(new UnboundVariableImpl("y"), new VariableImpl("z")))),
                unifier.getSubstitutions());

        t1 = Collections.singletonList(new VariableImpl("x"));
        t2 = Collections.singletonList(new VariableImpl("x"));

        unifier = new UnifierImpl(t1, t2);

        assertEquals(new LinkedList<>(), unifier.getSubstitutions());

        t1 = Arrays.asList(new VariableImpl("x"), new VariableImpl("z"));
        t2 = Arrays.asList(new VariableImpl("x"), new UnboundVariableImpl("y"));

        unifier = new UnifierImpl(t1, t2);

        assertEquals(new LinkedList<>(
                        List.of(new SubstitutionImpl(new UnboundVariableImpl("y"), new VariableImpl("z")))),
                unifier.getSubstitutions());
    }

    @Test
    public void testApplyUnifier() throws OWLOntologyCreationException, NotOWL2QLException {
        // load ontology
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");

        Query q;
        Query qp;
        List<Variable> head;
        Set<Atom> body;
        Unifier unifier;

        head = new LinkedList<>(Arrays.asList(new VariableImpl("x"), new VariableImpl("y"),
                new VariableImpl("z")));
        body = new HashSet<>(Arrays.asList(
                new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                        new VariableImpl("x"), new VariableImpl("y")),
                new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                        new VariableImpl("y"), new VariableImpl("z")),
                new ConceptsImpl(Collections.singleton(o.getClassMap().get("A")), new VariableImpl("z"))
        ));
        q = new QueryImpl(head, body);

        unifier = new UnifierImpl(Arrays.asList(new VariableImpl("x"), new VariableImpl("y")),
                Arrays.asList(new VariableImpl("y"), new VariableImpl("z")));

        qp = new QueryImpl(new LinkedList<>(Arrays.asList(new VariableImpl("z"), new VariableImpl("z"),
                new VariableImpl("z"))),
                new HashSet<>(Arrays.asList(new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                                new VariableImpl("z"), new VariableImpl("z")),
                        new ConceptsImpl(Collections.singleton(o.getClassMap().get("A")), new VariableImpl("z")))));
        // test unifier correctly applied
        assertEquals(qp, unifier.apply(q));
        // test no side effects on the initial query
        assertEquals(new QueryImpl(new LinkedList<>(Arrays.asList(new VariableImpl("x"), new VariableImpl("y"),
                new VariableImpl("z"))),
                new HashSet<>(Arrays.asList(
                        new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                                new VariableImpl("x"), new VariableImpl("y")),
                        new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                                new VariableImpl("y"), new VariableImpl("z")),
                        new ConceptsImpl(Collections.singleton(o.getClassMap().get("A")), new VariableImpl("z"))
                ))), q);
    }

}
