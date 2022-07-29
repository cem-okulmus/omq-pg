package at.tuwien.kbs.logic;

import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.query.Query;

import java.util.Set;

/**
 * An interface describing the functions a rewriter must implement.
 */
public interface Rewriter {

    /**
     * Given a Xi-restricted query q, rewrite q into a set of queries such that the evaluation over the data returns
     * all the certain answers in the KB.
     *
     * @param query The input query.
     * @param ontology The ontology.
     * @return Set of queries.
     */
    public Set<Query> rewrite(Query query, Ontology ontology);

    /**
     * Get a fresh variable name, which has not occurred in any query yet.
     *
     * @return new Variable name as a String.
     */
    public String getFreshVariableName();

}
