package at.tuwien.kbs.structure.query;

import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.Set;

public interface Binary extends Atom {

    Term getLeft();

    Term getRight();

    Binary replaceTerms(Term left, Term right);

    Set<OWLObjectPropertyExpression> getRoles();
}
