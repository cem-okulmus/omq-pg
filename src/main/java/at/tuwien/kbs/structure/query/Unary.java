package at.tuwien.kbs.structure.query;

/**
 * An interface that represents the functions of a unary atom.
 */

public interface Unary extends Atom {

    /**
     * Get the term in the atom.
     * @return Term in the atom.
     */
    Term getTerm();
}
