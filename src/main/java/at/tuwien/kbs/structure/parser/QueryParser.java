package at.tuwien.kbs.structure.parser;

import at.tuwien.kbs.structure.query.Query;

public interface QueryParser {

    /**
     * Parse a query string into a Query object
     *
     * @param queryString The query in string format, according to the grammar Q
     * @return A Query object
     */
    Query parse(String queryString);

}
