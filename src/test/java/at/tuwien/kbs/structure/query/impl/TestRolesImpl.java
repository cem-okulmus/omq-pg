package at.tuwien.kbs.structure.query.impl;

import at.tuwien.kbs.logic.Rewriter;
import at.tuwien.kbs.logic.impl.RewriterImpl;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.ontology.exception.NotOWL2QLException;
import at.tuwien.kbs.structure.ontology.impl.OntologyImpl;
import at.tuwien.kbs.structure.query.Atom;
import at.tuwien.kbs.structure.query.Roles;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TestRolesImpl {

    @Test
    public void testEqualRoles() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");
        Roles r1 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new VariableImpl("y"));
        Roles r2 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new VariableImpl("y"));

        assertEquals(r1, r2);

        r1 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new UnboundVariableImpl("z"));
        r2 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new UnboundVariableImpl("y"));

        assertEquals(r1, r2);

        r1 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r").getInverseProperty()),
                new UnboundVariableImpl("z"), new VariableImpl("x"));
        r2 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new UnboundVariableImpl("y"));

        assertEquals(r1, r2);
    }

    @Test
    public void testUnequalRoles() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");
        Roles r1 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new VariableImpl("y"));
        Roles r2 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("t")),
                new VariableImpl("x"), new VariableImpl("y"));

        assertNotEquals(r1, r2);

        r1 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new VariableImpl("y"));
        r2 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new VariableImpl("z"));

        assertNotEquals(r1, r2);
    }

    @Test
    public void testSetOfEqualRoles() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");
        Roles r1 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new VariableImpl("y"));
        Roles r2 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new VariableImpl("y"));

        assertEquals(r1, r2);

        HashSet<Atom> set1 = new HashSet<>(Arrays.asList(r1, r2));
        HashSet<Atom> set2 = new HashSet<>(Collections.singleton(r1));

        assertEquals(set2, set1);

        r1 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new UnboundVariableImpl("z"));
        r2 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new UnboundVariableImpl("y"));

        assertEquals(r1, r2);

        set1 = new HashSet<>(Arrays.asList(r1, r2));
        set2 = new HashSet<>(Collections.singleton(r1));

        assertEquals(set2, set1);

        r1 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r").getInverseProperty()),
                new UnboundVariableImpl("z"), new VariableImpl("x"));
        r2 = new RolesImpl(Collections.singleton(o.getPropertyMap().get("r")),
                new VariableImpl("x"), new UnboundVariableImpl("y"));

        assertEquals(r1, r2);

        set1 = new HashSet<>(Arrays.asList(r1, r2));
        set2 = new HashSet<>(Collections.singleton(r1));

        assertEquals(set2, set1);
    }

    @Test
    public void testSaturate() throws OWLOntologyCreationException, NotOWL2QLException {
        File resourcesDirectory = new File("src/test/resources");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/subroles.owl");
        Roles r1 = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("s"))),
                new VariableImpl("x"), new VariableImpl("y"));
        r1.saturate(o);
        Set<OWLObjectPropertyExpression> subroles = new HashSet<>(Arrays.asList(o.getPropertyMap().get("r"),
                o.getPropertyMap().get("s"), o.getPropertyMap().get("t")));

        assertEquals(subroles, r1.getRoles());

        r1 = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("s").getInverseProperty())),
                new VariableImpl("x"), new VariableImpl("y"));
        r1.saturate(o);
        subroles = new HashSet<>(Arrays.asList(o.getPropertyMap().get("r").getInverseProperty(),
                o.getPropertyMap().get("s").getInverseProperty(), o.getPropertyMap().get("t").getInverseProperty()));

        assertEquals(subroles, r1.getRoles());

        o = new OntologyImpl(resourcesDirectory.getAbsolutePath() + "/university2.ttl");
        r1 = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("teaches"))),
                new VariableImpl("x"), new VariableImpl("y"));

        r1.saturate(o);

        subroles = new HashSet<>(Arrays.asList(o.getPropertyMap().get("teaches"),
                o.getPropertyMap().get("isTaughtBy").getInverseProperty(), o.getPropertyMap().get("givesLab"),
                o.getPropertyMap().get("givesLecture")));

        assertEquals(subroles, r1.getRoles());

        r1 = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("teaches").getInverseProperty())),
                new VariableImpl("x"), new VariableImpl("y"));

        r1.saturate(o);

        subroles = new HashSet<>(Arrays.asList(o.getPropertyMap().get("teaches").getInverseProperty(),
                o.getPropertyMap().get("isTaughtBy"), o.getPropertyMap().get("givesLab").getInverseProperty(),
                o.getPropertyMap().get("givesLecture").getInverseProperty()));

        assertEquals(subroles, r1.getRoles());
    }

    @Test
    public void testApplicable() throws OWLOntologyCreationException, NotOWL2QLException {
        // load ontology
        File resourcesDirectory = new File("src/test/resources/university.owl");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath());
        Roles p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("teaches"))),
                new VariableImpl("x"),
                new UnboundVariableImpl("y"));

        Set<OWLAxiom> applicableAxioms = new HashSet<>();

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                applicableAxioms.add(I);
            }
        }

        assertEquals(1, applicableAxioms.size());

        // load ontology
        resourcesDirectory = new File("src/test/resources/university2.ttl");
        o = new OntologyImpl(resourcesDirectory.getAbsolutePath());

        p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy"))),
                new VariableImpl("x"),
                new UnboundVariableImpl("y"));

        p.saturate(o);

        applicableAxioms = new HashSet<>();

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                applicableAxioms.add(I);
            }
        }

        assertEquals(1, applicableAxioms.size());

        p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy"))),
                new UnboundVariableImpl("x"),
                new UnboundVariableImpl("y"));

        p.saturate(o);

        applicableAxioms = new HashSet<>();

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                applicableAxioms.add(I);
            }
        }

        assertEquals(1, applicableAxioms.size());

        p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy")
                .getInverseProperty())),
                new UnboundVariableImpl("x"),
                new UnboundVariableImpl("y"));

        p.saturate(o);

        applicableAxioms = new HashSet<>();

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                applicableAxioms.add(I);
            }
        }

        assertEquals(1, applicableAxioms.size());

        p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy")
                .getInverseProperty())),
                new UnboundVariableImpl("x"),
                new VariableImpl("y"));

        p.saturate(o);

        applicableAxioms = new HashSet<>();

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                applicableAxioms.add(I);
            }
        }

        assertEquals(1, applicableAxioms.size());

        p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy")
                .getInverseProperty())),
                new VariableImpl("x"),
                new VariableImpl("y"));

        p.saturate(o);

        applicableAxioms = new HashSet<>();

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                applicableAxioms.add(I);
            }
        }

        assertEquals(0, applicableAxioms.size());

        p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy"))),
                new VariableImpl("x"),
                new VariableImpl("y"));

        p.saturate(o);

        applicableAxioms = new HashSet<>();

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                applicableAxioms.add(I);
            }
        }

        assertEquals(0, applicableAxioms.size());
    }

    @Test
    public void testApply() throws OWLOntologyCreationException, NotOWL2QLException {
        // load ontology
        File resourcesDirectory = new File("src/test/resources/university.owl");
        Ontology o = new OntologyImpl(resourcesDirectory.getAbsolutePath());
        Rewriter rewriter = new RewriterImpl();
        Roles p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("teaches"))),
                new VariableImpl("x"),
                new UnboundVariableImpl("y"));

        Set<Atom> rewritten = new HashSet<>(Collections.singleton(
                new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("teaches"))),
                        new VariableImpl("x"), new UnboundVariableImpl("y"))));

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                rewritten.add(p.replace(I, rewriter));
            }
        }

        assertEquals(2, rewritten.size());
        assertEquals(new HashSet<>(Arrays.asList(
                new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("teaches"))),
                        new VariableImpl("x"), new UnboundVariableImpl("y")),
                new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("Professor"))), new VariableImpl("x"))
        )), rewritten);

        // load ontology
        resourcesDirectory = new File("src/test/resources/university2.ttl");
        o = new OntologyImpl(resourcesDirectory.getAbsolutePath());

        p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy"))),
                new VariableImpl("x"),
                new UnboundVariableImpl("y"));

        p.saturate(o);

        rewritten = new HashSet<>(Collections.singleton(
                new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy"))),
                        new VariableImpl("x"), new UnboundVariableImpl("y"))));

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                rewritten.add(p.replace(I, rewriter));
            }
        }

        assertEquals(2, rewritten.size());
        assertEquals(new HashSet<>(Arrays.asList(
                new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy"))),
                        new VariableImpl("x"), new UnboundVariableImpl("y")),
                new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("GraduateStudent"))), new VariableImpl("x"))
        )), rewritten);

        p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy"))),
                new UnboundVariableImpl("x"), new UnboundVariableImpl("y"));

        p.saturate(o);

        rewritten = new HashSet<>(Collections.singleton(
                new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy"))),
                        new UnboundVariableImpl("x"), new UnboundVariableImpl("y"))));

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                rewritten.add(p.replace(I, rewriter));
            }
        }

        assertEquals(2, rewritten.size());
        assertEquals(new HashSet<>(Arrays.asList(
                new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy"))),
                        new UnboundVariableImpl("x"), new UnboundVariableImpl("y")),
                new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("GraduateStudent"))), new UnboundVariableImpl("x"))
        )), rewritten);

        p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy")
                .getInverseProperty())),
                new UnboundVariableImpl("x"),
                new UnboundVariableImpl("y"));

        p.saturate(o);

        rewritten = new HashSet<>(Collections.singleton(
                new RolesImpl(new HashSet<>(
                        Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy").getInverseProperty())),
                        new UnboundVariableImpl("x"), new UnboundVariableImpl("y"))));

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                rewritten.add(p.replace(I, rewriter));
            }
        }

        assertEquals(2, rewritten.size());
        assertEquals(new HashSet<>(Arrays.asList(
                new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy")
                        .getInverseProperty())),
                        new UnboundVariableImpl("x"),
                        new UnboundVariableImpl("y")),
                new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("GraduateStudent"))), new UnboundVariableImpl("y"))
        )), rewritten);

        p = new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy")
                .getInverseProperty())),
                new UnboundVariableImpl("x"),
                new VariableImpl("y"));

        p.saturate(o);

        rewritten = new HashSet<>(
                Collections.singleton(
                        new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy")
                                .getInverseProperty())), new UnboundVariableImpl("x"), new VariableImpl("y"))));

        for (OWLAxiom I: o.getOntology().getAxioms()) {
            if (p.applicable(I)) {
                rewritten.add(p.replace(I, rewriter));
            }
        }

        assertEquals(2, rewritten.size());
        assertEquals(new HashSet<>(Arrays.asList(
                new RolesImpl(new HashSet<>(Collections.singleton(o.getPropertyMap().get("gradStudentSupervisedBy")
                        .getInverseProperty())),
                        new UnboundVariableImpl("x"),
                        new VariableImpl("y")),
                new ConceptsImpl(new HashSet<>(Collections.singleton(o.getClassMap().get("GraduateStudent"))), new VariableImpl("y"))
        )), rewritten);
    }
}
