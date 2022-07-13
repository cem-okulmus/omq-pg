package at.tuwien.kbs.logic.impl;

import at.tuwien.kbs.logic.Substitution;
import at.tuwien.kbs.structure.query.Term;

public class SubstitutionImpl implements Substitution {
    private Term in;
    private Term out;

    public SubstitutionImpl(Term in, Term out) {
        this.in = in;
        this.out = out;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof SubstitutionImpl)) {
            return false;
        }

        SubstitutionImpl s = (SubstitutionImpl) obj;

        return this.in.equals(s.in) && this.out.equals(s.out);
    }

    @Override
    public String toString() {
        return this.out.toString() + "/" + this.in.toString();
    }

    public Term getIn() {
        return in;
    }

    public void setIn(Term in) {
        this.in = in;
    }

    public Term getOut() {
        return out;
    }

    public void setOut(Term out) {
        this.out = out;
    }
}
