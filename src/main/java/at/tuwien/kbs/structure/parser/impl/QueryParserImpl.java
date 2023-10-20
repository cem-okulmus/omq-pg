package at.tuwien.kbs.structure.parser.impl;

import at.tuwien.kbs.generated.QBaseVisitor;
import at.tuwien.kbs.generated.QLexer;
import at.tuwien.kbs.generated.QParser;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.parser.QueryParser;
import at.tuwien.kbs.structure.query.*;
import at.tuwien.kbs.structure.query.impl.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class QueryParserImpl extends QBaseVisitor<Object> implements QueryParser {

    private final Ontology ontology;

    public QueryParserImpl(Ontology ontology) {
        this.ontology = ontology;
    }

    public Query parse(String queryString) {
        // parse query
        CharStream cs = CharStreams.fromString(queryString);
        QLexer lexer = new QLexer(cs);
        QParser parser = new QParser(new CommonTokenStream(lexer));

        ParseTree tree = parser.query();

//        System.out.println(tree.toStringTree(parser));

        return (Query) this.visit(tree);
    }

    /**
     * Visit the query. This is the entry point to our parser.
     * Returns a query with head and body.
     *
     * @param ctx The query context of the parser.
     * @return {@link Query}.
     */
    @Override
    public Object visitQuery(QParser.QueryContext ctx) {
        List<Variable> head = (List<Variable>) visit(ctx.head());
        Set<Atom> body = (Set<Atom>) visit(ctx.body());
        return new QueryImpl(head, body);
    }

    /**
     * Visit the head of the query and return a set of variables (the answer variables).
     *
     * @param ctx The head context of the parser.
     * @return A set of variables {@link Variable}.
     */
    @Override
    public Object visitHead(QParser.HeadContext ctx) {
        List<Variable> variables = new LinkedList<>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree c = ctx.getChild(i);
            Object result = c.accept(this);

            if (result != null) {
                variables.add((Variable) result);
            }
        }
        return variables;
    }

    /**
     * Visit a variable in the query and return a Variable object.
     *
     * @param ctx The variable context of the parser.
     * @return {@link Variable}.
     */
    @Override
    public Object visitVariable(QParser.VariableContext ctx) {
        return new VariableImpl(ctx.WORD().toString());
    }

    /**
     * Visit the body of the query and return a set of atoms.
     *
     * @param ctx The body context of the parser.
     * @return A Set of {@link Atom}.
     */
    @Override
    public Object visitBody(QParser.BodyContext ctx) {
        Set<Atom> atoms = new HashSet<>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree c = ctx.getChild(i);
            Object result = c.accept(this);

            if (result != null) {
                atoms.add((Atom) result);
            }
        }
        return atoms;
    }

    /**
     * Visit an atom in the body of a query.
     * Because the atom is either concepts, roles, or arb. length concepts, just descend in the AST.
     *
     * @param ctx The atom context of the parser.
     * @return {@link Atom}
     */
    @Override
    public Object visitAtom(QParser.AtomContext ctx) {
        return super.visitAtom(ctx);
    }

    /**
     * Visit a concepts atom and return a Concepts object with the concepts (disjunction) and the variable.
     *
     * @param ctx The concepts context of the parser.
     * @return {@link Concepts}.
     */
    @Override
    public Object visitConcepts(QParser.ConceptsContext ctx) {
        Set<OWLClassExpression> conceptNames = (Set<OWLClassExpression>) this.visitConceptnames(ctx.conceptnames());
        return new ConceptsImpl(conceptNames, (Term) this.visitVariable(ctx.variable()));
    }

    /**
     * Visit a set of class names.
     *
     * @param ctx The classes context of the parser.
     * @return Set of {@link OWLClass}
     */
    @Override
    public Object visitConceptnames(QParser.ConceptnamesContext ctx) {

        Set<OWLClass> conceptNames = new HashSet<>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree c = ctx.getChild(i);

//            System.out.println("Creating ConceptNames, current parseTree:");
//            System.out.println(c.toStringTree());

            Object result = c.accept(this);

            if (result != null) {
                conceptNames.add((OWLClass) result);
            }
//            else {
//                System.out.println("Not adding result from parsetree " + c.toStringTree());
//            }
        }
        return conceptNames;
    }

    /**
     * Visit a roles atom and return a Role object with the roles (disjunction) and the variables.
     *
     * @param ctx The role context of the parser.
     * @return {@link Roles}.
     */
    @Override
    public Object visitRoles(QParser.RolesContext ctx) {
        Set<OWLObjectPropertyExpression> properties = (Set<OWLObjectPropertyExpression>) this.visitProperties(ctx.properties());
        return new RolesImpl(properties, (Variable) this.visitVariable(ctx.left), (Variable) this.visitVariable(ctx.right));
    }

    /**
     * Visit an arbitrary length atom and return a ArbitraryLengthRoles object with the roles (disjunction) and the
     * variables.
     *
     * @param ctx the arbitrarylengthroles context of the parser.
     * @return {@link ArbitraryLengthRoles}
     */
    @Override
    public Object visitArbitraryLengthRoles(QParser.ArbitraryLengthRolesContext ctx) {
        Set<OWLObjectPropertyExpression> rolenames = (Set<OWLObjectPropertyExpression>) this.visitRolenames(ctx.rolenames());
        return new ArbitraryLengthRolesImpl(rolenames, (Variable) this.visitVariable(ctx.left), (Variable) this.visitVariable(ctx.right));
    }

    /**
     * Visit a set of rolenames.
     *
     * @param ctx The rolenames context of the parser.
     * @return List of {@link OWLObjectPropertyExpression}
     */
    @Override
    public Object visitRolenames(QParser.RolenamesContext ctx) {
        Set<OWLObjectPropertyExpression> rolenames = new HashSet<>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree c = ctx.getChild(i);
            Object result = c.accept(this);

            if (result != null) {
                rolenames.add((OWLObjectPropertyExpression) result);
            }
        }
        return rolenames;
    }

    /**
     * Visit a set of properties.
     *
     * @param ctx The properties context of the parser.
     * @return List of {@link OWLObjectPropertyExpression}
     */
    @Override
    public Object visitProperties(QParser.PropertiesContext ctx) {
        Set<OWLObjectPropertyExpression> properties = new HashSet<>();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree c = ctx.getChild(i);
            Object result = c.accept(this);

            if (result != null) {
                properties.add((OWLObjectPropertyExpression) result);
            }
        }
        return properties;
    }

    /**
     * Visit a property. Can be inverse or rolename.
     *
     * @param ctx The Property context of the parser.
     * @return {@link OWLObjectPropertyExpression}.
     */
    @Override
    public Object visitProperty(QParser.PropertyContext ctx) {
        return super.visitProperty(ctx);
    }

    /**
     * Visit a rolename.
     *
     * @param ctx The rolename context of the parser.
     * @return {@link OWLObjectPropertyExpression}
     */
    @Override
    public Object visitRolename(QParser.RolenameContext ctx) {
        return this.ontology.getPropertyMap().get((String) this.visitWords(ctx.words()));
    }

    /**
     * Visit an inverse property.
     *
     * @param ctx The Inverse context of the parser.
     * @return {@link OWLObjectPropertyExpression}.
     */
    @Override
    public Object visitInverse(QParser.InverseContext ctx) {
        return this.ontology.getPropertyMap()
                .get((String) this.visitWords(ctx.words()))
                .getInverseProperty();
    }

    /**
     * Visit words. Return the words as a String.
     *
     * @param ctx The Words context of the parser.
     * @return String representing the words.
     */
    @Override
    public Object visitWords(QParser.WordsContext ctx) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree c = ctx.getChild(i);
            builder.append(c);
        }
        return builder.toString();
    }

    /**
     * Visit a concept name.
     *
     * @param ctx The conceptname context of the parser.
     * @return {@link OWLClass}
     */
    @Override
    public Object visitConceptname(QParser.ConceptnameContext ctx) {

        String nameOfObject = (String) this.visitWords(ctx.words());

        OWLClass foundClass  = this.ontology.getClassMap().get(nameOfObject);


        if (foundClass == null) {
            return this.ontology.addClass(nameOfObject);
        }
        return foundClass;
    }
}
