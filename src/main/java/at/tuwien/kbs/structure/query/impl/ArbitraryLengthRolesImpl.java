package at.tuwien.kbs.structure.query.impl;

import at.tuwien.kbs.logic.Rewriter;
import at.tuwien.kbs.logic.Substitution;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.query.ArbitraryLengthRoles;
import at.tuwien.kbs.structure.query.Atom;
import at.tuwien.kbs.structure.query.Binary;
import at.tuwien.kbs.structure.query.Term;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ArbitraryLengthRolesImpl implements ArbitraryLengthRoles {

    private final Set<OWLObjectPropertyExpression> roles;

    private final Term left;

    private final Term right;

    public ArbitraryLengthRolesImpl(Set<OWLObjectPropertyExpression> roles, Term left, Term right) {
        this.roles = roles;
        this.left = left;
        this.right = right;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.roles != null ? this.roles.hashCode() : 0);
        hash = 53 * hash + (this.left != null ? this.left.hashCode() : 0);
        hash = 53 * hash + (this.right != null ? this.right.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ArbitraryLengthRolesImpl)){
            return false;
        }

        ArbitraryLengthRolesImpl r = (ArbitraryLengthRolesImpl) obj;

        // needs toString on the terms because two atoms are equivalent if they have the unbound
        // variables in the same positions
        return this.roles.equals(r.roles) && this.left.toString().equals(r.left.toString())
                && this.right.toString().equals(r.right.toString());
    }

    @Override
    public String toString() {
        String rolestring = this.roles.stream()
                .map(p -> p.getNamedProperty().getIRI().getFragment()) // remember, no inverses allowed/possible here
                .collect(Collectors.joining("|"));
        if (this.roles.size() > 1) {
            rolestring = '(' + rolestring + ')';
        }
        return rolestring + "*(" + this.left.toString() + "," + this.right.toString() + ')';
    }

    @Override
    public void saturate(Ontology o) {
        Set<OWLObjectPropertyExpression> subroles = new HashSet<>();
        // get the object property object for each role in this path element
        // note: all the object properties occurring in the query must be in the ontology signature
        // for _arbitrary length atoms_, saturation only for role names! (guaranteed no inverses).
        // exhaustively apply the subrole axioms
        while (!subroles.equals(this.roles)) {
            subroles = new HashSet<>(this.roles);
            // iterate over all the axioms for the roles that have r on the right side
            for (OWLObjectPropertyExpression r : subroles) {
                this.roles.addAll(o.getOntology().getObjectSubPropertyAxiomsForSuperProperty(r)
                        .stream()
                        .map(OWLSubObjectPropertyOfAxiom::getSubProperty)
                        .collect(Collectors.toSet()));
            }
        }
    }

    @Override
    public boolean applicable(OWLAxiom i) {
        return false;
    }

    @Override
    public Atom replace(OWLAxiom i, Rewriter rewriter) {
        // TODO maybe throw exception here? should never happen.
        return new ArbitraryLengthRolesImpl(new HashSet<>(this.roles), this.getLeft(), this.getRight());
    }

    @Override
    public Term getLeft() {
        return this.left.getFresh();
    }

    @Override
    public Term getRight() {
        return this.right.getFresh();
    }

    @Override
    public Set<OWLObjectPropertyExpression> getRoles() {
        return this.roles;
    }

    @Override
    public Binary replaceTerms(Term left, Term right) {
        return new ArbitraryLengthRolesImpl(new HashSet<>(this.roles), left, right);
    }

    @Override
    public Atom applySubstitution(List<Substitution> substitutions) {
        Term left = this.getLeft();
        Term right = this.getRight();

        for (Substitution sub : substitutions) {
            left = left.applySubstitution(sub);
            right = right.applySubstitution(sub);
        }
        return new ArbitraryLengthRolesImpl(new HashSet<>(this.roles), left, right);
    }
}
