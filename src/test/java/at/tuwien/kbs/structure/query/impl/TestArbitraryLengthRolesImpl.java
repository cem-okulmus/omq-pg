package at.tuwien.kbs.structure.query.impl;

import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.ontology.exception.NotOWL2QLException;
import at.tuwien.kbs.structure.ontology.impl.OntologyImpl;
import at.tuwien.kbs.structure.query.ArbitraryLengthRoles;
import at.tuwien.kbs.structure.query.Atom;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestArbitraryLengthRolesImpl {

    @Test
    public void testEqualArbitraryLengthRoles() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");
        ArbitraryLengthRoles p1 = new ArbitraryLengthRolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("r"),
                o.getPropertyMap().get("s"))),
                new VariableImpl("x"), new VariableImpl("y"));
        ArbitraryLengthRoles p2 = new ArbitraryLengthRolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("r"),
                o.getPropertyMap().get("s"))),
                new VariableImpl("x"), new VariableImpl("y"));

        assertEquals(p1, p2);

        p1 = new ArbitraryLengthRolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("r"))),
                new VariableImpl("x"), new VariableImpl("y"));
        p2 = new ArbitraryLengthRolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("r"))),
                new VariableImpl("x"), new VariableImpl("y"));

        assertEquals(p1, p2);
    }

    @Test
    public void testUnequalArbitraryLengthAtoms() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");
        ArbitraryLengthRoles p1 = new ArbitraryLengthRolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("r"),
                o.getPropertyMap().get("s"))),
                new VariableImpl("x"), new VariableImpl("y"));
        ArbitraryLengthRoles p2 = new ArbitraryLengthRolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("r"),
                o.getPropertyMap().get("t"), o.getPropertyMap().get("s"))),
                new VariableImpl("x"), new VariableImpl("y"));

        assertNotEquals(p1, p2);

        p1 = new ArbitraryLengthRolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("r"))),
                new VariableImpl("x"), new VariableImpl("y"));
        p2 = new ArbitraryLengthRolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("s"))),
                new VariableImpl("x"), new VariableImpl("y"));

        assertNotEquals(p1, p2);
    }

    @Test
    public void testSetOfEqualArbitraryLengthAtoms() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");

        ArbitraryLengthRoles p1 = new ArbitraryLengthRolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("r"),
                o.getPropertyMap().get("s"))),
                new VariableImpl("x"), new VariableImpl("y"));
        ArbitraryLengthRoles p2 = new ArbitraryLengthRolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("r"),
                o.getPropertyMap().get("s"))),
                new VariableImpl("x"), new VariableImpl("y"));

        assertEquals(p1, p2);

        Set<Atom> set1 = new HashSet<>(Arrays.asList(p1, p2));
        Set<Atom> set2 = new HashSet<>(Collections.singleton(p1));

        assertEquals(set1, set2);
    }

    @Test
    public void testSetOfUnequalSinglePathAtoms () throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");
        ArbitraryLengthRoles p1 = new ArbitraryLengthRolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("r"),
                o.getPropertyMap().get("s"))),
                new VariableImpl("x"), new VariableImpl("y"));
        ArbitraryLengthRoles p2 = new ArbitraryLengthRolesImpl(new HashSet<>(Arrays.asList(o.getPropertyMap().get("r"),
                o.getPropertyMap().get("t"), o.getPropertyMap().get("s"))),
                new VariableImpl("x"), new VariableImpl("z"));

        assertNotEquals(p1, p2);

        HashSet<Atom> set1 = new HashSet<>(Arrays.asList(p1, p2));
        HashSet<Atom> set2 = new HashSet<>(Collections.singleton(p1));

        assertNotEquals(set1, set2);
        assertEquals(set1.size(), 2);
    }
}
