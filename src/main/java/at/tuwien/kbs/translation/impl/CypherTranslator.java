package at.tuwien.kbs.translation.impl;

import at.tuwien.kbs.structure.query.*;
import at.tuwien.kbs.translation.Translator;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CypherTranslator implements Translator {

    @Override
    public String translate(List<Variable> answerVars, Set<Query> queries) {
        Set<String> queryStrings = queries.stream().map(q -> queryToCypher(answerVars, q)).collect(Collectors.toSet());
        return String.join("\nunion\n", queryStrings);
    }

    private String queryToCypher(List<Variable> answerVars, Query query) {
        int variableCounter = 0;
        Set<String> matchClauses = new HashSet<>();
        Set<String> dependencies = new HashSet<>();

        for (Atom atom : query.getBody()) {
            // three possibilities: Concepts, Roles or Arbitrary length Roles
            if (atom instanceof Concepts) {
                Term term = ((Concepts) atom).getTerm();
                Set<OWLClassExpression> concepts = ((Concepts) atom).getConceptNames();
                // we must "bind" the variable because of the where clause
                String matchClause = "match (" +
                        term.getName() +
                        ")";
                matchClauses.add(matchClause);
                // (x:Label1 OR x:Label2 OR x:Label3...)
                Set<String> atomDependencies = concepts
                        .stream()
                        .map(concept -> term.getName() + ":" + concept.asOWLClass().getIRI().getFragment())
                        .collect(Collectors.toSet());
                dependencies.add("(" + String.join(" or ", atomDependencies) + ")");
            } else if (atom instanceof Roles) {
                // performance consideration: if no mixing of directions, make directed
                if (((Roles) atom).getRoles().stream().noneMatch(p -> p instanceof OWLObjectInverseOf)) {
                    // all directed
                    String match = "match (" +
                            ((Roles) atom).getLeft().getName() +
                            ")-[" +
                            ":" +
                            String.join("|", ((Roles) atom).getRoles().stream().map(
                                    p -> p.getNamedProperty().getIRI().getFragment()
                            ).collect(Collectors.toSet())) +
                            "]->(" +
                            ((Roles) atom).getRight().getName() +
                            ")";
                    matchClauses.add(match);
                } else if (((Roles) atom).getRoles().stream().allMatch(p -> p instanceof OWLObjectInverseOf)) {
                    // all directed inverses
                    String match = "match (" +
                            ((Roles) atom).getLeft().getName() +
                            ")<-[" +
                            ":" +
                            String.join("|", ((Roles) atom).getRoles().stream().map(
                                    p -> p.getNamedProperty().getIRI().getFragment()
                            ).collect(Collectors.toSet())) +
                            "]-(" +
                            ((Roles) atom).getRight().getName() +
                            ")";
                    matchClauses.add(match);
                } else {
                    // mixing of directions
                    String match = "match (" +
                            ((Roles) atom).getLeft().getName() +
                            ")-[r" +
                            ++variableCounter +
                            ":" +
                            String.join("|", ((Roles) atom).getRoles().stream().map(
                                    p -> p.getNamedProperty().getIRI().getFragment()
                            ).collect(Collectors.toSet())) +
                            "]-(" +
                            ((Roles) atom).getRight().getName() +
                            ")";
                    matchClauses.add(match);
                    Set<String> atomDependencies = new HashSet<>();
                    for (OWLObjectPropertyExpression p : ((Roles) atom).getRoles()) {
                        String dependency = "(startnode(r" +
                                variableCounter +
                                ")=" +
                                ((p instanceof OWLObjectInverseOf) ? ((Roles) atom).getRight().getName() :
                                        ((Roles) atom).getLeft().getName()) +
                                " and type(r" +
                                variableCounter +
                                ")=\"" +
                                p.getNamedProperty().getIRI().getFragment() +
                                "\")";
                        atomDependencies.add(dependency);
                    }
                    dependencies.add("(" + String.join(" or ", atomDependencies) + ")");
                }
            } else {  // Arbitrary Length Roles
                // can only be uni-directed
                String match = "match (" +
                        ((ArbitraryLengthRoles) atom).getLeft().getName() +
                        ")-[" +
                        ":" +
                        String.join("|", ((ArbitraryLengthRoles) atom).getRoles().stream().map(
                                p -> p.getNamedProperty().getIRI().getFragment()
                        ).collect(Collectors.toSet())) +
                        "*0..]->(" +
                        ((ArbitraryLengthRoles) atom).getRight().getName() +
                        ")";
                matchClauses.add(match);
            }
        }

        String returnClause = "return distinct ";
        if (answerVars.size() == 0) {  // boolean query
            returnClause += "1";
        } else {  // contains answer variables
            returnClause += IntStream.range(0, Math.min(answerVars.size(), query.getHead().size()))
                    .mapToObj(i -> query.getHead().get(i).getName() + " as " + answerVars.get(i).getName())
                    .collect(Collectors.joining(", "));
        }
        return String.join("\n", matchClauses) + "\n" +
                (dependencies.size() > 0 ? "where " + String.join(" and ", dependencies) + "\n" : "") +
                returnClause;
    }
}
