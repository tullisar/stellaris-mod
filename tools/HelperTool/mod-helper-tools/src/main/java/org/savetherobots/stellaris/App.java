package org.savetherobots.stellaris;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.savetherobots.stellaris.ParadoxClausewitzParser.ElementContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.IdentifierContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.KeyContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.PropertyContext;
import org.savetherobots.stellaris.types.PlanetClass;
import org.savetherobots.stellaris.types.clausewitz.BuildablePlanetClass;

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

        // System.out.println(tree.toStringTree());

    }
}
