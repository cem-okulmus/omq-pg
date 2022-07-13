package at.tuwien.kbs.logic;

import at.tuwien.kbs.structure.query.Query;

import java.util.List;

public interface Unifier {

    public Query apply(Query query);

    public List<Substitution> getSubstitutions();
}
