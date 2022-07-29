package at.tuwien.kbs.structure.query;

import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.Set;

/**
 * An interface that represents the functions of atom with concepts.
 */
public interface Concepts extends Unary {

    /**
     * Get the set of concepts contained in the atom.
     * @return The set of concepts.
     */
    Set<OWLClassExpression> getConceptNames();

}
