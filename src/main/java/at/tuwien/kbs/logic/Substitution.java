package at.tuwien.kbs.logic;

import at.tuwien.kbs.structure.query.Term;

/**
 * An interface that describes the functions of a substitution.
 */
public interface Substitution {

    /**
     * Get the term that should be substituted.
     * @return {@link Term}
     */
    public Term getIn();

    /**
     * Get the term that should be substituted with.
     * @return {@link Term}
     */
    public Term getOut();

    /**
     * Set the term that should be substituted.
     * @param in The term that should be substituted.
     */
    public void setIn(Term in);

    /**
     * Set the term that should be substituted with.
     * @param out The term that should be substituted with.
     */
    public void setOut(Term out);
}
