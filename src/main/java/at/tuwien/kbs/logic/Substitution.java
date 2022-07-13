package at.tuwien.kbs.logic;

import at.tuwien.kbs.structure.query.Term;

public interface Substitution {

    public Term getIn();

    public Term getOut();

    public void setIn(Term in);

    public void setOut(Term out);
}
