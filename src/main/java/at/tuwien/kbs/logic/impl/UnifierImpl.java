package at.tuwien.kbs.logic.impl;

import at.tuwien.kbs.logic.Substitution;
import at.tuwien.kbs.logic.Unifier;
import at.tuwien.kbs.structure.query.*;
import at.tuwien.kbs.structure.query.impl.QueryImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UnifierImpl implements Unifier {
    private final List<Substitution> substitutions;

    /**
     * Initialize a new Unifier object.
     * Precondition for correctness: The input lists have equal length.
     * Post-condition: substitutions is not null if a most general unifier exists.
     *
     * @param t1 List of terms to be unified.
     * @param t2 List of terms to be unified.
     */
    public UnifierImpl(List<Term> t1, List<Term> t2) {
        this.substitutions = IntStream.range(0, Math.min(t1.size(), t2.size()))
                .mapToObj(i -> new SubstitutionImpl(t1.get(i), t2.get(i)))
                .collect(Collectors.toList());
        // compute most general unifier
        most_general_unifier();
    }

    /**
     * Compute the most general unifier of the list of substitutions.
     */
    private void most_general_unifier() {
        boolean action = true;
        while (action) {
            action = false;
            // delete equal pairs
            Iterator<Substitution> itr = this.substitutions.iterator();
            while (itr.hasNext()) {
                Substitution sub = itr.next();
                if (sub.getIn().equals(sub.getOut())) {
                    itr.remove();
                    action = true;
                }
            }
            // replace where out is an unbound variable and in is not
            for (Substitution sub : this.substitutions) {
                if (sub.getOut() instanceof UnboundVariable && !(sub.getIn() instanceof UnboundVariable)) {
                    Term tmp = sub.getOut();
                    sub.setOut(sub.getIn());
                    sub.setIn(tmp);
                    action = true;
                }
            }
            // perform substitution on other pairs
            for (Substitution sub : this.substitutions) {
                for (Substitution sub1 : this.substitutions) {
                    if (!sub1.equals(sub)) {
                        if (sub1.getIn().equals(sub.getIn())) {
                            sub1.setIn(sub.getOut());
                            action = true;
                        }
                        if (sub1.getOut().equals(sub.getIn())) {
                            sub1.setOut(sub.getOut());
                            action = true;
                        }
                    }
                }
            }
        }
    }

    /**
     * Apply a set of substitutions to the head and body of the query.
     *
     * @param query The input query.
     * @return A query q' where the substitutions have been applied to the query.
     */
    public Query apply(Query query) {
        if (this.substitutions.size() == 0) {
            return query;
        }
        // apply substitutions to head
        List<Variable> head = new LinkedList<>(query.getHead());
        ListIterator<Variable> it = head.listIterator();
        while (it.hasNext()) {
            Variable v = it.next();
            for (Substitution sub : this.substitutions) {
                if (v.equals(sub.getIn())) {
                    it.set((Variable) sub.getOut());
                }
            }
        }
        // apply substitutions to body
        Set<Atom> body = new HashSet<>();
        for (Atom a : query.getBody()) {
            body.add(a.applySubstitution(this.substitutions));
        }
        return new QueryImpl(head, body);
    }

    /**
     * Get the list of substitutions from this unifier.
     *
     * @return List of Substitutions.
     */
    public List<Substitution> getSubstitutions() {
        return substitutions;
    }
}
