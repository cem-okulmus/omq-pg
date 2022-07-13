package at.tuwien.kbs.structure.query;

import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.Set;

public interface Concepts extends Atom {

    Term getTerm();

    Set<OWLClassExpression> getConceptNames();

}
