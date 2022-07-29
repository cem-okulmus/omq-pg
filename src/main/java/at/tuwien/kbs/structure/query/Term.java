package at.tuwien.kbs.structure.query;

import at.tuwien.kbs.logic.Substitution;

/**
 * An interface that represents the functions of a term in a query.
 */
public interface Term {

    /**
     * Get a copy of the term with the same properties.
     * @return This term as a new object.
     */
    Term getFresh();

    /**
     * Get the name of the term.
     * @return Name of the term.
     */
    String getName();

    /**
     * Apply a substitution to this term.
     *
     * @param s The substitution to be applied
     * @return A new Term with the substitution applied.
     */
    Term applySubstitution(Substitution s);
}
