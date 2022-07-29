package at.tuwien.kbs.translation;

import at.tuwien.kbs.structure.query.Query;
import at.tuwien.kbs.structure.query.Variable;

import java.util.List;
import java.util.Set;

/**
 * An interface that represents the functions of a class that translates a query into a string that can be evaluated
 * over a database.
 */
public interface Translator {

    /**
     * Generate a query string of a query language that can be evaluated over a database that accepts this query
     * language.
     *
     * @param answerVars The answer variables in the input query.
     * @param queries The set of queries to be rewritten into.
     * @return String that can be evaluated over a database.
     */
    String translate(List<Variable> answerVars, Set<Query> queries);
}
