package org.savetherobots.stellaris;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.savetherobots.stellaris.ParadoxClausewitzParser.IdentifierContext;

/**
 * Hello world!
 */
public final class App {



    private App() {
    }

    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     */
    public static void main(String[] args) throws IOException {

        Path path = Paths.get(
                "C:\\Program Files (x86)\\Steam\\steamapps\\common\\Stellaris\\common\\scripted_effects\\00_scripted_effects.txt");

        ParadoxClausewitzLexer lexer = new ParadoxClausewitzLexer(CharStreams.fromPath(path));

        // lexer.getAllTokens().stream().filter(t -> t.getType() == ParadoxClausewitzLexer.IDENTIFIER).map(Token::getText).forEach(System.out::print);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ParadoxClausewitzParser parser = new ParadoxClausewitzParser(tokens) 
        {

            @Override
            public void enterRule(ParserRuleContext localctx, int state, int ruleIndex) {
                // TODO Auto-generated method stub
                super.enterRule(localctx, state, ruleIndex);
            }

            @Override
            public void notifyErrorListeners(Token offendingToken, String msg, RecognitionException e) {
                // TODO Auto-generated method stub
                super.notifyErrorListeners(offendingToken, msg, e);
            }
            
        };
        

        ParseTree tree = parser.module();
        ParseTreeWalker walker = new ParseTreeWalker();
        ParadoxClausewitzParserListener listener = new ParadoxClausewitzParserBaseListener() {};
        walker.walk(listener, tree);
        // System.out.println(tree.toStringTree());

    }
}
