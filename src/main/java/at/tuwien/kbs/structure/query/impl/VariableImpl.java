package at.tuwien.kbs.structure.query.impl;

import at.tuwien.kbs.logic.Substitution;
import at.tuwien.kbs.structure.query.Term;
import at.tuwien.kbs.structure.query.Variable;

/**
 * A class that represents a variable in the query.
 */
public class VariableImpl implements Variable {

    /**
     * The name of the variable.
     */
    private final String name;

    /**
     * Initialize a new variable object with a name.
     * @param name The name of the variable.
     */
    public VariableImpl(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if(!(obj instanceof VariableImpl)) {
            return false;
        }

        VariableImpl v = (VariableImpl) obj;

        return this.name.equals(v.name);
    }

    @Override
    public Term getFresh() {
        return new VariableImpl(this.name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Apply a substitution to this variable.
     *
     * @param s The substitution to be applied
     * @return A new Term with the substitution applied.
     */
    @Override
    public Term applySubstitution(Substitution s) {
        if (s.getIn().equals(this)) {
            return s.getOut().getFresh();
        }
        return new VariableImpl(this.name);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
