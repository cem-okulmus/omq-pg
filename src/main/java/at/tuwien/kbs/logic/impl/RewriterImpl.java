package at.tuwien.kbs.logic.impl;

import at.tuwien.kbs.logic.Rewriter;
import at.tuwien.kbs.logic.Unifier;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.query.*;
import at.tuwien.kbs.structure.query.impl.*;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.*;
import java.util.stream.Collectors;

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

                // TODO make these loops more efficient with filtering?
                // (c) concatenate, if possible
                for (Atom atom1: qp.getBody()) {
                    for (Atom atom2: qp.getBody()) {
                        if(atom1 instanceof Binary && atom2 instanceof ArbitraryLengthRoles && !atom1.equals(atom2)) {
                            Q.add(tau(concatenate(qp, (Binary) atom1, (ArbitraryLengthRoles) atom2)));
                        }
                    }
                }

                // (d) merge atoms, if possible
                for (Atom atom1: qp.getBody()) {
                    for (Atom atom2: qp.getBody()) {
                        if (atom1 instanceof Binary && atom2 instanceof Binary && !atom1.equals(atom2)) {
                            Q.addAll(merge(qp, (Binary) atom1, (Binary) atom2).stream()
                                    .map(this::tau)
                                    .collect(Collectors.toSet()));
                        }
                    }
                }

                // (e) drop atoms, if possible
                for (Atom atom: qp.getBody()) {
                    if (atom instanceof ArbitraryLengthRoles) {
                        Q.add(tau(drop(qp, (ArbitraryLengthRoles) atom)));
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

    private Query concatenate(Query query, Binary atom1, ArbitraryLengthRoles atom2) {
        // small side-note: an unbound variable can become bound here, so we need to create new variables
        // create a copy of the query
        Query qp = new QueryImpl(new LinkedList<>(query.getHead()), new HashSet<>(query.getBody()));
        Binary r1;
        Binary r2;
        if (atom1 instanceof Roles) {
            if (atom2.getRoles().containsAll(atom1.getRoles())) {  // roles of atom 1 subset of roles of atom 2
                // check terms
                if (atom1.getLeft().equals(atom2.getLeft())) {  // append to front
                    return concatenateRoleToFront(qp, atom1, atom2);
                } else if (atom1.getRight().equals(atom2.getRight())) {  // append to back
                    return concatenateRoleToBack(qp, atom1, atom2);
                }
            } else {  // check the inverse
                atom1 = ((Roles) atom1).getInverse();  // check the inverse
                if (atom2.getRoles().containsAll(atom1.getRoles())) {  // roles of atom 1 subset of roles of atom 2
                    if (atom1.getLeft().equals(atom2.getLeft())) {  // append to front
                        return concatenateRoleToFront(qp, atom1, atom2);
                    } else if (atom1.getRight().equals(atom2.getRight())) {  // append to back
                        return concatenateRoleToBack(qp, atom1, atom2);
                    }
                }
            }
        } else {  // atom1 is also an ArbitraryLengthRole
            if (atom2.getRoles().containsAll(atom1.getRoles())) { // roles of atom1 subset of roles of atom2
                // check vars
                if (atom1.getLeft().equals(atom2.getLeft())) {  // append to front
                    // remove atoms
                    qp.getBody().remove(atom1);
                    qp.getBody().remove(atom2);
                    // generate new atoms
                    r1 = new ArbitraryLengthRolesImpl(atom1.getRoles(), new VariableImpl(atom1.getLeft().getName()),
                            new VariableImpl(atom1.getRight().getName()));
                    r2 = new ArbitraryLengthRolesImpl(atom2.getRoles(), new VariableImpl(atom1.getRight().getName()),
                            new VariableImpl(atom2.getRight().getName()));
                    // add atoms to query body
                    qp.getBody().add(r1);
                    qp.getBody().add(r2);
                    // return result
                    return qp;
                } else if (atom1.getRight().equals(atom2.getRight())) {  // append to back
                    // remove atoms
                    qp.getBody().remove(atom1);
                    qp.getBody().remove(atom2);
                    // generate new atoms
                    r1 = new ArbitraryLengthRolesImpl(atom1.getRoles(), new VariableImpl(atom1.getLeft().getName()),
                            new VariableImpl(atom1.getRight().getName()));
                    r2 = new ArbitraryLengthRolesImpl(atom2.getRoles(), new VariableImpl(atom2.getLeft().getName()),
                            new VariableImpl(atom1.getLeft().getName()));
                    // add atoms to query body
                    qp.getBody().add(r1);
                    qp.getBody().add(r2);
                    // return result
                    return qp;
                }
            }
        }
        // if no concatenation possible, return original query
        return query;
    }

    private Query concatenateRoleToFront(Query qp, Binary atom1, ArbitraryLengthRoles atom2) {
        // remove atoms
        qp.getBody().remove(atom1);
        qp.getBody().remove(atom2);
        // generate new atoms
        Roles r1 = new RolesImpl(atom1.getRoles(), new VariableImpl(atom1.getLeft().getName()),
                new VariableImpl(atom1.getRight().getName()));
        ArbitraryLengthRoles r2 = new ArbitraryLengthRolesImpl(atom2.getRoles(), new VariableImpl(atom1.getRight().getName()),
                new VariableImpl(atom2.getRight().getName()));
        // add atoms to query body
        qp.getBody().add(r1);
        qp.getBody().add(r2);
        // return result
        return qp;
    }

    private Query concatenateRoleToBack(Query qp, Binary atom1, ArbitraryLengthRoles atom2) {
        // remove atoms
        qp.getBody().remove(atom1);
        qp.getBody().remove(atom2);
        // generate new atoms
        Roles r1 = new RolesImpl(atom1.getRoles(), new VariableImpl(atom1.getLeft().getName()),
                new VariableImpl(atom1.getRight().getName()));
        ArbitraryLengthRoles r2 = new ArbitraryLengthRolesImpl(atom2.getRoles(), new VariableImpl(atom2.getLeft().getName()),
                new VariableImpl(atom1.getLeft().getName()));
        // add atoms to query body
        qp.getBody().add(r1);
        qp.getBody().add(r2);
        // return result
        return qp;
    }

    private Set<Query> merge(Query query, Binary atom1, Binary atom2) {
        Set<OWLObjectPropertyExpression> intersection;
        Set<Query> merges = new HashSet<>();
        Binary r1;
        Binary r2;
        Unifier unifier;
        if (atom1 instanceof Roles) {
            // compute first intersection
            intersection = new HashSet<>(atom1.getRoles());
            intersection.retainAll(atom2.getRoles());
            if (intersection.size() > 0) {
                // do the terms of a1 and a2 unify?
                unifier = new UnifierImpl(Arrays.asList(atom1.getLeft(), atom1.getRight()),
                        Arrays.asList(atom2.getLeft(), atom2.getRight()));
                if (unifier.getSubstitutions().size() > 0) {
                    // create a copy of the query
                    Query qp = new QueryImpl(new LinkedList<>(query.getHead()), new HashSet<>(query.getBody()));
                    // remove atoms
                    qp.getBody().remove(atom1);
                    qp.getBody().remove(atom2);
                    // generate new atoms
                    r1 = new RolesImpl(intersection, atom1.getLeft(), atom1.getRight());
                    r2 = new RolesImpl(intersection, atom2.getLeft(), atom2.getRight());
                    // add atoms to query body
                    qp.getBody().add(r1);
                    qp.getBody().add(r2);
                    // add the result of unification to the result
                    merges.add(unifier.apply(qp));
                }
            }
            // compute second intersection on the inverse of atom1
            atom1 = ((Roles) atom1).getInverse();
            intersection = new HashSet<>(atom1.getRoles());
            intersection.retainAll(atom2.getRoles());
            if (intersection.size() > 0) {
                // do the terms of atom1 and atom2 unify?
                unifier = new UnifierImpl(Arrays.asList(atom1.getLeft(), atom1.getRight()),
                        Arrays.asList(atom2.getLeft(), atom2.getRight()));
                if (unifier.getSubstitutions().size() > 0) {
                    // create a copy of the query
                    Query qp = new QueryImpl(new LinkedList<>(query.getHead()), new HashSet<>(query.getBody()));
                    // remove atoms
                    qp.getBody().remove(atom1);
                    qp.getBody().remove(atom2);
                    // generate new atoms
                    r1 = new RolesImpl(intersection, atom1.getLeft(), atom1.getRight());
                    r2 = new RolesImpl(intersection, atom2.getLeft(), atom2.getRight());
                    // add atoms to query body
                    qp.getBody().add(r1);
                    qp.getBody().add(r2);
                    // add the result of unification to the result
                    merges.add(unifier.apply(qp));
                }
            }
        } else {  // arbitrary length atom - only directed roles
            intersection = new HashSet<>(atom1.getRoles());
            intersection.retainAll(atom2.getRoles());
            if (intersection.size() > 0) {
                // do the terms of atom1 and atom2 unify?
                unifier = new UnifierImpl(Arrays.asList(atom1.getLeft(), atom1.getRight()),
                        Arrays.asList(atom2.getLeft(), atom2.getRight()));
                if (unifier.getSubstitutions().size() > 0) {
                    // create a copy of the query
                    Query qp = new QueryImpl(new LinkedList<>(query.getHead()), new HashSet<>(query.getBody()));
                    // remove atoms
                    qp.getBody().remove(atom1);
                    qp.getBody().remove(atom2);
                    // generate new atoms
                    if (atom2 instanceof ArbitraryLengthRoles) {
                        r1 = new ArbitraryLengthRolesImpl(intersection, atom1.getLeft(), atom1.getRight());
                        r2 = new ArbitraryLengthRolesImpl(intersection, atom2.getLeft(), atom2.getRight());
                    } else {
                        r1 = new RolesImpl(intersection, atom1.getLeft(), atom1.getRight());
                        r2 = new RolesImpl(intersection, atom2.getLeft(), atom2.getRight());
                    }
                    // add atoms to query body
                    qp.getBody().add(r1);
                    qp.getBody().add(r2);

                    // add the result of unification to the result
                    merges.add(unifier.apply(qp));
                }
            }
        }
        return merges;
    }

    private Query drop(Query query, ArbitraryLengthRoles atom) {
        // remember: no empty query body!
        if (query.getBody().size() > 1 &&
                (atom.getLeft() instanceof UnboundVariable || atom.getRight() instanceof UnboundVariable)) {
            // create copy of query
            Query qp = new QueryImpl(new LinkedList<>(query.getHead()), new HashSet<>(query.getBody()));
            // remove arb. length atom with unbound variable
            qp.getBody().remove(atom);
            // return new query
            return qp;
        }
        return query;
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
