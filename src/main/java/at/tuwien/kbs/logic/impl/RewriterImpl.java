package at.tuwien.kbs.logic.impl;

import at.tuwien.kbs.logic.Rewriter;
import at.tuwien.kbs.logic.Unifier;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.query.*;
import at.tuwien.kbs.structure.query.impl.ConceptsImpl;
import at.tuwien.kbs.structure.query.impl.QueryImpl;
import at.tuwien.kbs.structure.query.impl.UnboundVariableImpl;
import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.*;

public class RewriterImpl implements Rewriter {

    private int variable_counter = 0;

    @Override
    public Set<Query> rewrite(Query query, Ontology ontology) {
        // structure as in the paper
        Set<Query> Q = new HashSet<>(Collections.singleton(tau(saturate(query, ontology))));
        Set<Query> Qp = null;

        while (!Q.equals(Qp)) {
            Qp = new HashSet<>(Q);
            for (Query qp: Qp) {
                // (a) apply axioms, if possible
                for (Atom a: qp.getBody()) {
                    for (OWLAxiom i: ontology.getAxioms()) {
                        if (a.applicable(i)) {
                            Q.add(tau(replace(qp, a, ontology, i)));
                        }
                    }
                }
                // (b) unify atoms, if possible
                for (Atom atom1: qp.getBody()) {
                    for (Atom atom2: qp.getBody()) {
                        Q.add(tau(reduce(qp, atom1, atom2)));
                    }
                }
            }
        }

        return Qp;
    }

    private Query tau(Query query) {
        // map of variables and the number of atoms they occur in
        Map<Variable, Integer> variableCount = new HashMap<>();
        // first pass: get number of terms each variable occurs in
        for (Atom a : query.getBody()) {
            if (a instanceof Concepts) { // concepts
                Concepts b = (Concepts) a;
                if (b.getTerm() instanceof Variable) {
                    Integer count = variableCount.getOrDefault((Variable) b.getTerm(), 0);
                    variableCount.put((Variable) b.getTerm(), count + 1);
                }
            }
            if (a instanceof Binary) { // binary atom
                Binary b = (Binary) a;
                if (b.getLeft() instanceof Variable) {
                    Integer count = variableCount.getOrDefault((Variable) b.getLeft(), 0);
                    variableCount.put((Variable) b.getLeft(), count + 1);
                }
                if (b.getRight() instanceof Variable) {
                    Integer count = variableCount.getOrDefault((Variable) b.getRight(), 0);
                    variableCount.put((Variable) b.getRight(), count + 1);
                }
            }
        }
        // second pass: replace all entries that are not constants or answer variables with unbound variables
        Set<Atom> body = new HashSet<>();
        for (Atom a : query.getBody()) {
            if (a instanceof Concepts) { // concept name
                Concepts b = (Concepts) a;
                Term t = b.getTerm();
                if (t instanceof Variable) {  // contains variable
                    if ((variableCount.get((Variable) t) == 1) &&
                            !query.getHead().contains((Variable) t)) { // unbound variable
                        t = new UnboundVariableImpl(t.getName());  // replace term
                    }
                }
                body.add(new ConceptsImpl(b.getConceptNames(), t)); // add to new query
            }
            if (a instanceof Binary) { // roles, arb.length atoms
                Binary b = (Binary) a;
                Term left = b.getLeft();
                Term right = b.getRight();
                if (left instanceof Variable) {
                    if ((variableCount.get((Variable) left) == 1) &&
                            !query.getHead().contains((Variable) left)) { // unbound variable
                        left = new UnboundVariableImpl(left.getName()); // replace term
                    }
                }
                if (right instanceof Variable) {
                    if ((variableCount.get((Variable) right) == 1) &&
                            !query.getHead().contains((Variable) right)) { // unbound variable
                        right = new UnboundVariableImpl(right.getName()); // replace term
                    }
                }
                body.add(b.replaceTerms(left, right));
            }
        }
        // return query with unbound variables marked as such
        return new QueryImpl(new LinkedList<>(query.getHead()), body);
    }

    private Query replace(Query query, Atom atom, Ontology ontology, OWLAxiom i) {
        List<Variable> head = new LinkedList<>(query.getHead());
        Set<Atom> body = new HashSet<>(query.getBody());
        // remove atom from body
        body.remove(atom);
        // create a new atom
        Atom newAtom = atom.replace(i, this);
        // saturate the new atom
        newAtom.saturate(ontology);
        // add it to the body of the new query
        body.add(newAtom);
        return new QueryImpl(head, body);
    }

    private Query reduce(Query query, Atom atom1, Atom atom2) {
        if (atom1 instanceof Concepts && atom2 instanceof Concepts) {  // concepts
            Concepts b1 = (Concepts) atom1;
            Concepts b2 = (Concepts) atom2;
            if (b1.getConceptNames().equals(b2.getConceptNames())) {  // same set of concepts
                // compute unifier, return result of applying the unifier to q
                Unifier unifier = new UnifierImpl(Collections.singletonList(b1.getTerm()),
                        Collections.singletonList(b2.getTerm()));
                return unifier.apply(query);
            }
        } else if (atom1 instanceof Roles && atom2 instanceof Roles) {  // Roles
            Roles b1 = (Roles) atom1;
            Roles b2 = (Roles) atom2;
            if (b1.getRoles().equals(b2.getRoles())) {
                // compute unifier, return result of applying the unifier to q
                Unifier unifier = new UnifierImpl(Arrays.asList(b1.getLeft(), b1.getRight()),
                        Arrays.asList(b2.getLeft(), b2.getRight()));
                return unifier.apply(query);
            } else {
                // it could be that we can unify once we "invert" one of the role atoms
                // in this case, inverting means that we switch the left and right variable
                // and invert all the roles in the set of roles.
                Roles b3 = b1.getInverse();
                if (b3.getRoles().equals(b2.getRoles())) {
                    // no need to create a copy of the query, an inverse is the same as the original atom
                    // compute unifier, return result of applying the unifier to q
                    Unifier unifier = new UnifierImpl(Arrays.asList(b3.getLeft(), b3.getRight()),
                            Arrays.asList(b2.getLeft(), b2.getRight()));
                    return unifier.apply(query);
                }
            }
        } else if (atom1 instanceof ArbitraryLengthRoles && atom2 instanceof ArbitraryLengthRoles) {
            ArbitraryLengthRoles b1 = (ArbitraryLengthRoles) atom1;
            ArbitraryLengthRoles b2 = (ArbitraryLengthRoles) atom2;
            if (b1.getRoles().equals(b2.getRoles())) {
                // compute unifier, return result of applying the unifier to q
                Unifier unifier = new UnifierImpl(Arrays.asList(b1.getLeft(), b1.getRight()),
                        Arrays.asList(b2.getLeft(), b2.getRight()));
                return unifier.apply(query);
            }
        }
        // mismatched atoms, return original query
        return query;
    }

    private void concatenate() {

    }

    private void merge() {

    }

    private void drop() {

    }

    private Query saturate(Query query, Ontology ontology) {
        query.saturate(ontology);
        return query;
    }

    @Override
    public String getFreshVariableName() {
        return "v" + ++this.variable_counter;
    }
}
