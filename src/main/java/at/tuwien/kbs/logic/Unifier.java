package at.tuwien.kbs.logic;

import at.tuwien.kbs.structure.query.Query;

import java.util.List;

/**
 * An interface that describes the functions of a unifier of two lists of terms.
 */
public interface Unifier {

    /**
     * Apply a set of substitutions to the head and body of the query.
     *
     * @param query The input query.
     * @return A query q' where the substitutions have been applied to the query.
     */
    public Query apply(Query query);

    /**
     * Get the list of substitutions from this unifier.
     *
     * @return List of Substitutions.
     */
    public List<Substitution> getSubstitutions();
}
