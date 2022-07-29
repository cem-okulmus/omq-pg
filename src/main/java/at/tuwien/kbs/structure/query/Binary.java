package at.tuwien.kbs.structure.query;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.Set;

/**
 * An interface that represents the functions of a binary atom in a query.
 */
public interface Binary extends Atom {

    /**
     * Get the term on the left of the atom.
     *
     * @return Term on the left.
     */
    Term getLeft();

    /**
     * Get the term on the right of the atom.
     *
     * @return Term on the right.
     */
    Term getRight();

    /**
     * Replace the terms of the atom.
     *
     * @param left The term that should be on the left.
     * @param right The term that should be on the right.
     * @return The atom with the replaced terms.
     */
    Binary replaceTerms(Term left, Term right);

    /**
     * Get the set of roles in the atom.
     *
     * @return The set of roles in the atom.
     */
    Set<OWLObjectPropertyExpression> getRoles();
}
