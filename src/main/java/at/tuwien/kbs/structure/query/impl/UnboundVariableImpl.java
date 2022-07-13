package at.tuwien.kbs.structure.query.impl;

import at.tuwien.kbs.logic.Substitution;
import at.tuwien.kbs.structure.query.Term;
import at.tuwien.kbs.structure.query.UnboundVariable;

public class UnboundVariableImpl implements UnboundVariable {

    private final String name;

    public UnboundVariableImpl(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof UnboundVariableImpl)) {
            return false;
        }

        UnboundVariableImpl v = (UnboundVariableImpl) obj;

        return this.name.equals(v.name);

    }

    @Override
    public int hashCode() {
        int hash = 3;
        // hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public Term getFresh() {
        return new UnboundVariableImpl(this.name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Apply a substitution to this unbound variable.
     *
     * @param s The substitution to be applied
     * @return A new Term with the substitution applied.
     */
    @Override
    public Term applySubstitution(Substitution s) {
        if (s.getIn().equals(this)) {
            return s.getOut().getFresh();
        }
        return new UnboundVariableImpl(this.name);
    }

    @Override
    public String toString() {
        return "_";
    }
}
