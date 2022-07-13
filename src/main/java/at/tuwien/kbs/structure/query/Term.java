package at.tuwien.kbs.structure.query;

import at.tuwien.kbs.logic.Substitution;

public interface Term {

    Term getFresh();

    String getName();

    /**
     * Apply a substitution to this term.
     *
     * @param s The substitution to be applied
     * @return A new Term with the substitution applied.
     */
    Term applySubstitution(Substitution s);
}
