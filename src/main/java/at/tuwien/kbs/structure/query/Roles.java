package at.tuwien.kbs.structure.query;

/**
 * An interface that represents the functions of an atom with roles (not arbitrary length).
 */
public interface Roles extends Binary {

    /**
     * Get the equivalent inverse of the atom.
     * @return An equivalent atom that has the terms swapped and the roles inversed.
     */
    Roles getInverse();
}
