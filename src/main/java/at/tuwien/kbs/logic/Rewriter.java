package at.tuwien.kbs.logic;

import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.query.Query;

import java.util.Set;

public interface Rewriter {

    public Set<Query> rewrite(Query query, Ontology ontology);

    public String getFreshVariableName();

}
