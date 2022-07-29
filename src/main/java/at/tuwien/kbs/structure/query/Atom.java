package at.tuwien.kbs.structure.query;

import at.tuwien.kbs.logic.Rewriter;
import at.tuwien.kbs.logic.Substitution;
import at.tuwien.kbs.structure.ontology.Ontology;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * An interface that represents an atom in the query body.
 */
import java.util.List;

public interface Atom {

    /**
     * Saturate the roles or concepts in the atom according to a given ontology.
     * Ontology signature must contain the roles and concepts in the atom.
     *
     * @param o The ontology.
     */
    void saturate(Ontology o);

    /**
     * Check if an axiom is applicable to the atom, s.t. this atom can be replaced by another one.
     * Ontology signature must contain the roles and concepts in the atom.
     *
     * @param i The axiom to be checked for applicability on the atom.
     * @return True if applicable, False otherwise.
     */
    boolean applicable(OWLAxiom i);

    /**
     * Generate the atom that can be obtained from this atom by applying the axiom to it.
     * Applicable _must_ have been called before and returned True.
     *
     * @param i The axiom to be applied to the atom.
     * @param rewriter The rewriter object used in the rewriting of this atom.
     * @return Rewritten atom.
     */
    Atom replace(OWLAxiom i, Rewriter rewriter);

    /**
     * Apply a substitution to this atom.
     *
     * @param substitutions The substitution to be applied to this atom.
     * @return Atom with terms substituted according to the substitution.
     */
    Atom applySubstitution(List<Substitution> substitutions);
}
