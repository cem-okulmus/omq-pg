package at.tuwien.kbs.structure.query;

import at.tuwien.kbs.structure.ontology.Ontology;

import java.util.List;
import java.util.Set;

/**
 * An interface that represents the functions of a query.
 */
public interface Query {

    /**
     * Saturate all atoms in the query according to an ontology.
     * The ontology signature must contain all roles and concepts that occur in the query.
     *
     * @param o The ontology.
     */
    void saturate(Ontology o);

    /**
     * Get the body of the query.
     * @return Set of atoms (body) in the query.
     */
    Set<Atom> getBody();

    /**
     * Get the head of the query.
     * @return List of Variables (head) in the body.
     */
    List<Variable> getHead();

}
