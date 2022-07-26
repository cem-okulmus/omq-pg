package at.tuwien.kbs.structure.query.impl;

import at.tuwien.kbs.logic.Rewriter;
import at.tuwien.kbs.logic.Substitution;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.query.*;
import org.semanticweb.owlapi.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class RolesImpl implements Roles {

    /**
     * The names of the roles.
     */
    private final Set<OWLObjectPropertyExpression> roles;
    /**
     * The term on the left.
     */
    private final Term left;
    /**
     * The term on the right.
     */
    private final Term right;

    /**
     * Initialize a new Role object.
     * @param roles The roles/properties in this atom.
     * @param left The left {@link Term}.
     * @param right The right {@link Term}.
     */
    public RolesImpl(Set<OWLObjectPropertyExpression> roles, Term left, Term right) {
        this.roles = roles;
        this.left = left;
        this.right = right;
    }

    @Override
    public int hashCode() {
        return this.hashCodePart() * ((RolesImpl) this.getInverse()).hashCodePart();
    }

    private int hashCodePart() {
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

        if (!(obj instanceof RolesImpl)){
            return false;
        }

        RolesImpl r = (RolesImpl) obj;

        RolesImpl inv = (RolesImpl) this.getInverse();

        // needs toString on the terms because two atoms are equivalent if they have the unbound
        // variables in the same positions
        return (this.roles.equals(r.roles) && this.left.toString().equals(r.left.toString())
                && this.right.toString().equals(r.right.toString())) || (inv.roles.equals(r.roles) &&
                inv.left.toString().equals(r.left.toString()) && inv.right.toString().equals(r.right.toString()));
    }

    @Override
    public String toString() {
        String rolestring = this.roles.stream()
                .map(p -> p.getNamedProperty().getIRI().getFragment() + ((p instanceof OWLObjectInverseOf) ? "-" : ""))
                .collect(Collectors.joining("|"));
        if (this.roles.size() > 1) {
            rolestring = '(' + rolestring + ')';
        }
        return rolestring + '(' + this.left.toString() + "," + this.right.toString() + ')';
    }

    @Override
    public void saturate(Ontology o) {
        Set<OWLObjectPropertyExpression> subroles = new HashSet<>();
        while (!subroles.equals(this.roles)) {
            subroles = new HashSet<>(this.roles);
            // iterate over all the axioms for the roles that have r or r- on the right side
            for (OWLObjectPropertyExpression r : subroles) {
                // R1 \ISA R
                this.roles.addAll(o.getOntology().getObjectSubPropertyAxiomsForSuperProperty(r)
                        .stream()
                        .map(OWLSubObjectPropertyOfAxiom::getSubProperty)
                        .collect(Collectors.toSet()));
                // R1 \ISA R-
                this.roles.addAll(o.getOntology().getObjectSubPropertyAxiomsForSuperProperty(r.getInverseProperty())
                        .stream()
                        .map(OWLSubObjectPropertyOfAxiom::getSubProperty)
                        .map(OWLObjectPropertyExpression::getInverseProperty)
                        .collect(Collectors.toSet()));
                // inverses
                // use the named property (in case of inverse), and add the inverse of the inverse in case
                // r is an inverse itself.
                this.roles.addAll(o.getOntology().getInverseObjectPropertyAxioms(r.getNamedProperty())
                        .stream()
                        .map(p -> p.getPropertiesMinus(r.getNamedProperty()))
                        .flatMap(Collection::stream)
                        .map(p -> (r instanceof OWLObjectInverseOf) ? p : p.getInverseProperty())
                        .collect(Collectors.toSet()));
            }
        }
    }

    @Override
    public boolean applicable(OWLAxiom I) {
        if (I instanceof OWLSubClassOfAxiom) {  // C \ISA \exists R
            OWLSubClassOfAxiom i = (OWLSubClassOfAxiom) I;
            if (i.getSuperClass() instanceof OWLObjectSomeValuesFrom) {
                if (this.right instanceof UnboundVariable && // C \ISA \exists R, R(x,_)
                        this.roles.contains(((OWLObjectSomeValuesFrom) i.getSuperClass()).getProperty())) {
                    return true;
                }
                // C \ISA \exists R, R-(_,x)
                return this.left instanceof UnboundVariable &&
                        this.roles.contains(((OWLObjectSomeValuesFrom) i.getSuperClass())
                                .getProperty().getInverseProperty());
            }
        } else if (I instanceof OWLObjectPropertyDomainAxiom) { // exists r \ISA \exists R
            // casting to get the domain and making sure it's \exists R
            OWLObjectPropertyDomainAxiom i = (OWLObjectPropertyDomainAxiom) I;
            OWLClassExpression ii = i.getDomain();
            return domainOrRangeAxiomApplicable(ii);
        } else if (I instanceof OWLObjectPropertyRangeAxiom) { // exists r- \ISA \exists R
            // casting to get the domain and making sure it's \exists R
            OWLObjectPropertyRangeAxiom i = (OWLObjectPropertyRangeAxiom) I;
            OWLClassExpression ii = i.getRange();
            return domainOrRangeAxiomApplicable(ii);
        }
        return false;
    }

    private boolean domainOrRangeAxiomApplicable(OWLClassExpression i) {
        if (i instanceof OWLObjectSomeValuesFrom) {
            OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) i).getProperty();
            if (this.right instanceof UnboundVariable && this.roles.contains(property)) {
                return true;
            }
            return this.left instanceof UnboundVariable && this.roles.contains(property.getInverseProperty());
        }
        return false;
    }

    @Override
    public Atom replace(OWLAxiom i, Rewriter rewriter) {
        // note that both variables can be unbound, theoretically
        if (i instanceof OWLSubClassOfAxiom) { // C \ISA \exists R
            // from the contract of replace, we know that applicable has to have been called beforehand.
            // Therefore, the right-hand side is of the form OWLObjectSomeValuesFrom such that the axiom is applicable.
            // Furthermore, we know we can replace the atom with an expression of the left-hand side.
            // The left-hand side can be \exists r, \exists r^-, or A

            // get the property on the right-hand side
            OWLObjectPropertyExpression rhs = ((OWLObjectSomeValuesFrom) ((OWLSubClassOfAxiom) i).getSuperClass()).getProperty();
            if (((OWLSubClassOfAxiom) i).getSubClass() instanceof OWLClass) { // A
                OWLClass subclass = (OWLClass) ((OWLSubClassOfAxiom) i).getSubClass();
                if (this.right instanceof UnboundVariable && this.roles.contains(rhs)) { // A \ISA \exists R, R(x,_)
                    // return A(x)
                    return new ConceptsImpl(new HashSet<>(Collections.singleton(subclass)), this.left.getFresh());
                }
                // A \ISA \exists R, R-(_,y)
                // return A(y)
                return new ConceptsImpl(new HashSet<>(Collections.singleton(subclass)), this.right.getFresh());
            } else { // \exists R1 \ISA \exists R
                OWLObjectSomeValuesFrom subclass = (OWLObjectSomeValuesFrom) ((OWLSubClassOfAxiom) i).getSubClass();
                return applyDomainOrRangeAxiom(subclass.getProperty(), rhs);
            }
        } else if (i instanceof OWLObjectPropertyRangeAxiom) { // \exists r^-
            // don't forget to take the inverse here
            OWLObjectPropertyExpression lhs = ((OWLObjectPropertyRangeAxiom) i).getProperty().getInverseProperty();
            OWLClassExpression range = ((OWLObjectPropertyRangeAxiom) i).getRange();
            OWLObjectPropertyExpression rhs = ((OWLObjectSomeValuesFrom) range).getProperty();
            return applyDomainOrRangeAxiom(lhs, rhs);
        } else { // \exists r
            OWLObjectPropertyExpression lhs = ((OWLObjectPropertyDomainAxiom) i).getProperty();
            OWLClassExpression domain = ((OWLObjectPropertyDomainAxiom) i).getDomain();
            OWLObjectPropertyExpression rhs = ((OWLObjectSomeValuesFrom) domain).getProperty();
            return applyDomainOrRangeAxiom(lhs, rhs);
        }
    }

    private Atom applyDomainOrRangeAxiom(OWLObjectPropertyExpression lhs, OWLObjectPropertyExpression rhs) {
        if (this.roles.contains(rhs) && this.right instanceof UnboundVariable) {
            return new RolesImpl(new HashSet<>(Collections.singleton(lhs)), this.getLeft(), this.getRight());
        }
        return new RolesImpl(new HashSet<>(Collections.singleton(lhs)), this.getRight(), this.getLeft());
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
    public Binary replaceTerms(Term left, Term right) {
        return new RolesImpl(new HashSet<>(this.roles), left, right);
    }

    @Override
    public Set<OWLObjectPropertyExpression> getRoles() {
        return this.roles;
    }

    @Override
    public Roles getInverse() {
        Set<OWLObjectPropertyExpression> inverses = this.roles.stream()
                .map(OWLObjectPropertyExpression::getInverseProperty)
                .collect(Collectors.toSet());
        return new RolesImpl(inverses, this.right.getFresh(), this.left.getFresh());
    }

    /**
     * Apply a list of substitutions to the terms of an atom.
     *
     * @param substitutions A list of substitutions.
     * @return A new RewritableAtom with the substitutions applied to its terms.
     */
    @Override
    public Atom applySubstitution(List<Substitution> substitutions) {
        Term left = this.getLeft();
        Term right = this.getRight();

        for (Substitution sub : substitutions) {
            left = left.applySubstitution(sub);
            right = right.applySubstitution(sub);
        }
        return new RolesImpl(new HashSet<>(this.roles), left, right);
    }
}
