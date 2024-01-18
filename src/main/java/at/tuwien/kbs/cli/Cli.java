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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.nio.file.Files;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;


/**
 * TODOS:
 *
 *   - add ability to output queries in one (multiple?) file(s)
 *
 */

public class Cli {
    public static void main(String[] args) throws IOException {
        Ontology ontology;
        Translator translator = new CypherTranslator();
        Rewriter rewriter = new RewriterImpl();
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));


        ArgumentParser ArgParser = ArgumentParsers.newFor("OMQ-PG").build()
                .defaultHelp(true)
                .description("Ontology-Mediated Query Answering for Property Graphs.");
        ArgParser.addArgument("-o", "--ontology")
                .help("Provide the ontology to be used.");
        ArgParser.addArgument("-q", "--query")
                .help("The query to be rewritten into CYPHER.");
        Namespace ns = null;
        try {
            ns = ArgParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            ArgParser.handleError(e);
            System.exit(1);
        }

        if (ns.getString("ontology") == null   || ns.getString("query") == null) {
//            System.out.println("Need to provide both an OWL2 ontology and a query!");
            System.out.println(ArgParser.formatUsage());
            System.exit(1);
        }


        // read ontology
//        System.out.println("please enter the path to the ontology file you want to work with");
//        String ontology_path = br.readLine();
        String ontology_path = ns.getString("ontology");
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
//        System.out.println("Please enter the query file you want to rewrite with the ontology");
//        String queryString = br.readLine();
        Path queryFilePath = Path.of(ns.getString("query"));

        String queryString = Files.readString(queryFilePath);

        QueryParser parser = new QueryParserImpl(ontology);

        Query query = parser.parse(queryString);

        System.out.printf("Parsed Query:\n %s\n", query.toString());

        // rewrite query
        Set<Query> rewrittenQueries = rewriter.rewrite(query, ontology);

        System.out.println("Rewritten queries");
        // print rewritten queries
        rewrittenQueries.forEach(System.out::println);

        // translate to query over the sources
        String translatedQuery = translator.translate(query.getHead(), rewrittenQueries);

        System.out.printf("Translated Query:\n %s\n", translatedQuery);

//        // copy query to clipboard
//        Toolkit toolkit = Toolkit.getDefaultToolkit();
//        Clipboard clipboard = toolkit.getSystemClipboard();
//        StringSelection strSel = new StringSelection(translatedQuery);
//        clipboard.setContents(strSel, null);

        // "done" message
        System.out.println("Your query has been rewritten.");
    }
}
