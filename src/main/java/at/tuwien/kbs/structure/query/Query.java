package at.tuwien.kbs.structure.query;

import at.tuwien.kbs.structure.ontology.Ontology;

import java.util.List;
import java.util.Set;

public interface Query {

    void saturate(Ontology o);

    Set<Atom> getBody();

    List<Variable> getHead();

}
