package org.savetherobots.stellaris.modules.clausewitz;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import javax.annotation.PropertyKey;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Multimaps;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.savetherobots.stellaris.ParadoxClausewitzLexer;
import org.savetherobots.stellaris.ParadoxClausewitzParser;
import org.savetherobots.stellaris.ParadoxClausewitzParserBaseListener;
import org.savetherobots.stellaris.ParadoxClausewitzParserListener;
import org.savetherobots.stellaris.ParadoxClausewitzParser.BooleanLiteralContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.ElementContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.KeyContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.LiteralContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.PropertyContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.PropertyKeyContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.ValueContext;
import org.savetherobots.stellaris.types.PlanetClass;
import org.savetherobots.stellaris.types.clausewitz.BuildablePlanetClass;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

public class PlanetClassModule {

    private static enum ModuleContext {
        GLOBAL,
        PLANET,
        RANDOM_LIST
    };

    static final String RANDOM_LIST = "random_list";

    // The path for a module will never change. It must be exported/saved as for
    // this to change.
    private final Path path;

    // Each module contains a mapping of string identifiers to planet classes. A
    // multimap is used to make it so that duplicate entires can be easily
    // distinguished.
    private final Multimap<String, PlanetClass> planetClasses = MultimapBuilder.treeKeys().arrayListValues(1).build();

    // Track current parsing state.
    private boolean parseInProgress = false;
    private String currentIdentifier = "";
    private BuildablePlanetClass.Builder builder = new BuildablePlanetClass.Builder();
    private ModuleContext currentContext = ModuleContext.GLOBAL;

    // The lexer is updated
    private ParadoxClausewitzLexer lexer = null;

    // Parser listener is used during parsing to update module state
    private ParadoxClausewitzParserListener listener = new ParadoxClausewitzParserBaseListener() {
        @Override
        public void enterElement(ElementContext context) {
            currentContext = ModuleContext.PLANET;
            currentIdentifier = Optional.ofNullable(context.getChild(KeyContext.class, 0)).map(KeyContext::getText)
                    .orElseThrow();
        }

        @Override
        public void exitElement(ElementContext context) {
            planetClasses.put(currentIdentifier, builder.buildPartial());
            currentContext = ModuleContext.GLOBAL;
        }

        public void enterProperty(PropertyContext context) {
            switch (currentContext) {
                case PLANET:

                    // The entire text of the property key rule context is used.
                    // TODO(tullisar): Verify that the text of the property key is the same as the
                    // text of the first nested identifier rule context.
                    String propertyKey = Optional.ofNullable(context.getChild(PropertyKeyContext.class, 0))
                            .map(PropertyKeyContext::getText).orElseThrow();

                    switch (propertyKey) {
                        case "colonizable":
                            Optional.ofNullable(context.getChild(ValueContext.class, 0))
                                    .map(c -> c.getChild(LiteralContext.class, 0))
                                    .map(c -> c.getChild(BooleanLiteralContext.class, 0))
                                    .map(BooleanLiteralContext::getText);
                            break;
                    }

                    break;
                case RANDOM_LIST:

                    break;

                default:
                    throw new AssertionError();
            }
        }

    };

    /**
     * Create a new planet class module associated with the given file path.
     * 
     * @param path The path from which to parse planet class data.
     */
    private PlanetClassModule(final Path path) throws IOException {
        this.path = checkNotNull(path);
    }

    /**
     * 
     */
    private void parse() throws IOException {
        parseInProgress = true;
        lexer = new ParadoxClausewitzLexer(CharStreams.fromPath(path));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ParadoxClausewitzParser parser = new ParadoxClausewitzParser(tokens);
        ParseTree tree = parser.module();
        ParseTreeWalker walker = new ParseTreeWalker();

        parseInProgress = false;
    }

    public synchronized void reparse() throws IOException {
        checkState(!parseInProgress);
        planetClasses.clear();
        parse();
    }

    public static PlanetClassModule parseFrom(final Path path) throws IOException {
        PlanetClassModule module = new PlanetClassModule(path);
        module.parse();
        return module;
    }
}
