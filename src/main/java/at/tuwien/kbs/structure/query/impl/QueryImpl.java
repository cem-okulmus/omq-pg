package at.tuwien.kbs.structure.query.impl;

import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.query.Atom;
import at.tuwien.kbs.structure.query.Query;
import at.tuwien.kbs.structure.query.Variable;

import java.util.List;
import java.util.Set;

public class QueryImpl implements Query {

    private final List<Variable> head;

    private final Set<Atom> body;

    public QueryImpl(List<Variable> head, Set<Atom> body) {
        this.head = head;
        this.body = body;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.head != null ? this.head.hashCode() : 0);
        hash = 53 * hash + (this.body != null ? this.body.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof QueryImpl)) {
            return false;
        }

        QueryImpl q = (QueryImpl) obj;

        return this.body.equals(q.body) && this.head.equals(q.head);
    }

    @Override
    public void saturate(Ontology o) {
        this.body.forEach(atom -> atom.saturate(o));
    }

    @Override
    public Set<Atom> getBody() {
        return body;
    }

    @Override
    public List<Variable> getHead() {
        return head;
    }
}
