package at.tuwien.kbs.translation;

import at.tuwien.kbs.structure.query.Query;
import at.tuwien.kbs.structure.query.Variable;

import java.util.List;
import java.util.Set;

public interface Translator {

    String translate(List<Variable> answerVars, Set<Query> queries);
}
