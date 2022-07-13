package at.tuwien.kbs.structure.query;

import at.tuwien.kbs.logic.Rewriter;
import at.tuwien.kbs.logic.Substitution;
import at.tuwien.kbs.structure.ontology.Ontology;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.List;

public interface Atom {

    void saturate(Ontology o);

    boolean applicable(OWLAxiom i);

    Atom replace(OWLAxiom i, Rewriter rewriter);

    Atom applySubstitution(List<Substitution> substitutions);
}
