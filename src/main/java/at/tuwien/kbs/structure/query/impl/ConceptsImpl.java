package at.tuwien.kbs.structure.query.impl;

import at.tuwien.kbs.logic.Rewriter;
import at.tuwien.kbs.logic.Substitution;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.query.Atom;
import at.tuwien.kbs.structure.query.Concepts;
import at.tuwien.kbs.structure.query.Term;
import at.tuwien.kbs.structure.query.UnboundVariable;
import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConceptsImpl implements Concepts {

    private final Set<OWLClassExpression> conceptNames;

    private final Term term;

    public ConceptsImpl(Set<OWLClassExpression> conceptNames, Term term) {
        this.conceptNames = conceptNames;
        this.term = term;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.conceptNames != null ? this.conceptNames.hashCode() : 0);
        hash = 53 * hash + (this.term != null ? this.term.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ConceptsImpl)) {
            return false;
        }

        ConceptsImpl c = (ConceptsImpl) obj;

        return this.conceptNames.equals(c.conceptNames) && this.term.toString().equals(c.term.toString());
    }

    @Override
    public String toString() {
        String conceptString = this.conceptNames.stream()
                .map(p -> p.asOWLClass().getIRI().getFragment())
                .collect(Collectors.joining("|"));
        if (this.conceptNames.size() > 1) {
            conceptString = '(' + conceptString + ')';
        }
        return conceptString + '(' + this.term.toString() + ')';
    }

    @Override
    public void saturate(Ontology o) {
        Set<OWLClassExpression> subclasses = new HashSet<>();
        // get all the subclasses occurring in this concepts atom
        // exhaustively apply the subclass axioms
        while (!subclasses.equals(this.conceptNames)) {
            subclasses = new HashSet<>(this.conceptNames);
            // iterate over all the axioms that have c on the right-hand side
            // and a class/conceptname on the left-hand side
            for (OWLClassExpression c: subclasses) {
                this.conceptNames.addAll(o.getOntology().getSubClassAxiomsForSuperClass((OWLClass) c)
                        .stream()
                        .map(OWLSubClassOfAxiom::getSubClass)
                        .filter(cls -> cls instanceof OWLClass)
                        .collect(Collectors.toSet()));
            }
        }
    }

    @Override
    public boolean applicable(OWLAxiom i) {
        // for completeness, we assume that all atoms of this form are _always_ saturated!
        if (i instanceof OWLSubClassOfAxiom) {
            // for subclass axioms: if we already have the subclass in the atom, then no application
            return this.conceptNames.contains(((OWLSubClassOfAxiom) i).getSuperClass()) &&
                    !(this.conceptNames.contains(((OWLSubClassOfAxiom) i).getSubClass()));
        }
        if (i instanceof OWLObjectPropertyDomainAxiom) {
            return this.conceptNames.contains(((OWLObjectPropertyDomainAxiom) i).getDomain());
        }
        if (i instanceof OWLObjectPropertyRangeAxiom) {
            return this.conceptNames.contains(((OWLObjectPropertyRangeAxiom) i).getRange());
        }
        return false;
    }

    @Override
    public Atom replace(OWLAxiom i, Rewriter rewriter) {
        UnboundVariable newVar = new UnboundVariableImpl(rewriter.getFreshVariableName());
        OWLObjectPropertyExpression property;
        if (i instanceof OWLSubClassOfAxiom) {  // \exists R \ISA A
            // From the contract of replace, we know that applicable has to have been called beforehand.
            // Therefore, this can not be an OWLClass (because of the assumption that all atoms of the type
            // Concepts are saturated).
            // Because of DL Lite_R, this has to be OWLObjectSomeValuesFrom
            property = ((OWLObjectSomeValuesFrom) ((OWLSubClassOfAxiom) i).getSubClass()).getProperty();
        }
        // Domain or range axiom
        else if (i instanceof OWLObjectPropertyRangeAxiom) {  // exists r^- ISA A
            property = ((OWLObjectPropertyRangeAxiom) i).getProperty().getInverseProperty();
        } else {  // exists r ISA A
            property = ((OWLObjectPropertyDomainAxiom) i).getProperty();
        }
        // return new roles atom
        return new RolesImpl(new HashSet<>(Collections.singleton(property)),
                this.getTerm(), newVar);
    }

    @Override
    public Term getTerm() {
        return this.term.getFresh();
    }

    @Override
    public Set<OWLClassExpression> getConceptNames() {
        return this.conceptNames;
    }

    @Override
    public Atom applySubstitution(List<Substitution> substitutions) {
        Term t = this.getTerm();
        for (Substitution sub: substitutions) {
            t = t.applySubstitution(sub);
        }
        return new ConceptsImpl(new HashSet<>(this.conceptNames), t);
    }
}
