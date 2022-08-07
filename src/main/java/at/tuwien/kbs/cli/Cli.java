package at.tuwien.kbs.cli;

import at.tuwien.kbs.logic.Rewriter;
import at.tuwien.kbs.logic.impl.RewriterImpl;
import at.tuwien.kbs.structure.ontology.Ontology;
import at.tuwien.kbs.structure.ontology.exception.NotOWL2QLException;
import at.tuwien.kbs.structure.ontology.impl.OntologyImpl;
import at.tuwien.kbs.structure.parser.QueryParser;
import at.tuwien.kbs.structure.parser.impl.QueryParserImpl;
import at.tuwien.kbs.structure.query.Query;
import at.tuwien.kbs.translation.Translator;
import at.tuwien.kbs.translation.impl.CypherTranslator;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

public class Cli {
    public static void main(String[] args) throws IOException {
        Ontology ontology;
        Translator translator = new CypherTranslator();
        Rewriter rewriter = new RewriterImpl();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


        // read ontology
        System.out.println("please enter the path to the ontology file you want to work with");
        String ontology_path = br.readLine();
        try {
            ontology = new OntologyImpl(ontology_path);
        } catch (OWLOntologyCreationException e) {
            System.out.println("Something went wrong loading the ontology");
            System.out.println(e.getMessage());
            return;
        } catch (NotOWL2QLException e) {
            System.out.println("The given ontology was not in OWL2 QL");
            return;
        }

        // read query
        System.out.println("Please enter the query you want to rewrite with the ontology");
        String queryString = br.readLine();

        QueryParser parser = new QueryParserImpl(ontology);

        Query query = parser.parse(queryString);

        // rewrite query
        Set<Query> rewrittenQueries = rewriter.rewrite(query, ontology);

        // print rewritten queries
        rewrittenQueries.forEach(System.out::println);

        // translate to query over the sources
        String translatedQuery = translator.translate(query.getHead(), rewrittenQueries);

        // copy query to clipboard
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = toolkit.getSystemClipboard();
        StringSelection strSel = new StringSelection(translatedQuery);
        clipboard.setContents(strSel, null);

        // "done" message
        System.out.println("Your query has been rewritten. It has been copied to your system clipboard.");
    }
}
