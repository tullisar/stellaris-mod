package org.savetherobots.stellaris.modules.clausewitz;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import org.inferred.freebuilder.FreeBuilder;
import org.savetherobots.stellaris.ParadoxClausewitzParser.MapEntryContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.ObjectDeclarationContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.PropertyContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.RandomListDeclarationContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.ReferenceContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.StringLiteralContext;
import org.savetherobots.stellaris.ParadoxClausewitzParserBaseListener;
import org.savetherobots.stellaris.database.DatabaseResolver;
import org.savetherobots.stellaris.types.PlanetClass;

/**
 * Represents a source module containing planet class declarations.
 */
public class PlanetClassModule extends AbstractModule {

    /**
     * Represents an unresolved planet class parsed from this module.
     */
    class Reference implements PlanetClass.Reference {
        private final Optional<ReferenceContext> referenceContext;
        private final Optional<StringLiteralContext> stringLiteralContext;

        /**
         * Constructs a new planet class reference from the given reference token
         * context.
         * 
         * @param context The token context for which the reference applies.
         */
        protected Reference(final ReferenceContext context) {
            this.referenceContext = Optional.of(context);
            stringLiteralContext = Optional.empty();
        }

        /**
         * Constructs a new planet class reference from the given string literal
         * context.
         * 
         * @param context The token context for which the reference applies.
         */
        protected Reference(final StringLiteralContext context) {
            this.stringLiteralContext = Optional.of(context);
            this.referenceContext = Optional.empty();
        }

        /**
         * Resolve a planet class that was referenced within this file.
         */
        @Override
        public Optional<PlanetClass> get(final DatabaseResolver resolver) {
            return referenceContext.map(ReferenceContext::getText)
                    .or(() -> stringLiteralContext.map(StringLiteralContext::getText))
                    .flatMap(resolver::resolveIdentifier)
                    .flatMap(identifier -> {
                        return classes.get(identifier).stream().findFirst()
                                .or(() -> resolver.resolvePlanetClass(identifier));
                    });
        }

    }

    /**
     * This is an abstract implementation of the planet class interface. The builder
     * pattern is used to facilitate creating the objects from the token context.
     */
    @FreeBuilder
    protected static abstract class Object extends AbstractModule.GameObject implements PlanetClass {

        /**
         * Represents a builder for planet class definitions.
         */
        protected static class Builder extends PlanetClassModule_Object_Builder
                implements PlanetClass.Builder, AbstractModule.GameObject.Builder {

            // Map the expected property keys to value consuming functions that will update
            // a builder
            protected static final Map<String, BiConsumer<Builder, PropertyContext>> PROPERTY_SETTERS;
            static {
                PROPERTY_SETTERS = ImmutableMap.<String, BiConsumer<Builder, PropertyContext>>builder()
                        .put("colonizable", (b, c) -> b.booleanProperty(c, b::isColonizable))
                        .put("climate", (b, c) -> b.stringProperty(c, b::climate))
                        .put("star", (b, c) -> b.booleanProperty(c, b::isStar))
                        .put("ringworld", (b, c) -> b.booleanProperty(c, b::isRingWorld))
                        .put("district_set", (b, c) -> b.identifierProperty(c, b::districtSet))
                        .put("starting_planet", (b, c) -> b.booleanProperty(c, b::isStartingPlanet))
                        .put("initial", (b, c) -> b.booleanProperty(c, b::isInitial))
                        .put("ideal", (b, c) -> b.booleanProperty(c, b::isIdeal))
                        .put("is_artificial_planet", (b, c) -> b.booleanProperty(c, b::isArtificial))
                        .build();
            }

            /**
             * Constructs a new planet class builder with the default properties.
             */
            protected Builder() {
                isIdeal(false);
                isStartingPlanet(false);
                isInitial(false);
                isStar(false);
                isColonizable(false);
                isRingWorld(false);
            }

            /**
             * {@inheritDoc}
             */
            public boolean isHandledProperty(final PropertyContext context) {
                return PROPERTY_SETTERS.containsKey(context.propertyKey().getText());
            }

            /**
             * {@inheritDoc}
             */
            public Builder handledProperty(final PropertyContext context) {
                String key = context.propertyKey().getText();
                PROPERTY_SETTERS.get(key).accept(this, context);
                return this;
            }

        }

        /**
         * Parses a planet class from the given object declaration parser context.
         * 
         * @param context The parser context from which to get planet class data.
         * @return the planet class that was parsed, which may not be a fully
         *         initialized planet class.
         */
        protected static final PlanetClass parseFrom(ObjectDeclarationContext context) {
            Builder builder = new Builder();
            builder.identifier(context.key().getText());
            builder.objectProperties(context.objectDefinition()).forEach(builder::property);
            return builder.buildPartial();
        }
    }

    /**
     * This is an abstract implementation of the random planet class list interface.
     * The builder pattern is used to facilitate creating objects from the token
     * context.
     */
    @FreeBuilder
    protected static abstract class RandomList implements PlanetClass.RandomList {

        /**
         * Represents a builder for random planet class list definition.
         */
        protected static class Builder extends PlanetClassModule_RandomList_Builder
                implements PlanetClass.RandomList.Builder, AbstractModule.GameObject.Builder {

            // Map the expected property keys to value consuming functions that will update
            // a builder
            protected static final Map<String, BiConsumer<Builder, PropertyContext>> PROPERTY_SETTERS;
            static {
                PROPERTY_SETTERS = ImmutableMap.<String, BiConsumer<Builder, PropertyContext>>builder()
                        .put("name", (b, c) -> b.identifierProperty(c, b::identifier))
                        .put("planets", Builder::planets)
                        .build();
            }

            /**
             * Sets the name (also the identifier) of the random list being built.
             * 
             * @param context The property context from which to get the name.
             * @return A reference to this builder.
             */
            protected Builder name(final PropertyContext context) {
                identifierProperty(context, this::identifier);
                return this;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public Builder handledProperty(PropertyContext context) {
                String key = context.propertyKey().getText();
                PROPERTY_SETTERS.get(key).accept(this, context);
                return this;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public boolean isHandledProperty(PropertyContext context) {
                return PROPERTY_SETTERS.containsKey(context.propertyKey().getText());
            }

            /**
             * Adds all planet class keys from the given property context to the random list
             * being built.
             * 
             * @param context The property context from which to get the defined planet
             *                classes.
             * @return A reference to this builder.
             */
            public Builder planets(final PropertyContext context) {
                addAllClassReferenceContexts(collectionValue(context).flatMap(c -> reference(c).stream()));
                addAllStringLiteralContexts(collectionValue(context).flatMap(c -> stringLiteralContext(c).stream()));
                return this;
            }

        }

        /**
         * Parses a random list builder from the given random list declaration parser
         * context.
         * 
         * @param context The parser context from which to get random list data.
         * @return a random list builder that was parsed. Planet classes will not have
         *         been resolved in the list.
         */
        protected static final RandomList.Builder parseFrom(RandomListDeclarationContext context) {
            checkNotNull(context);
            Builder builder = new Builder();
            context.randomList().objectDefinition().map().mapEntry().stream().filter(e -> e.property() != null)
                    .map(MapEntryContext::property).forEach(builder::property);
            return builder;
        }

        /**
         * Returns a list of the parsed planet class reference contexts.
         */
        protected abstract List<ReferenceContext> classReferenceContexts();

        /**
         * Returns a list of the parsed planet class string literal contexts.
         */
        protected abstract List<StringLiteralContext> stringLiteralContexts();

    }

    /**
     * A parser listener implementation for handling planet class modules. The
     * listener has direct access to the module's parsing state.
     */
    protected class Listener extends ParadoxClausewitzParserBaseListener {

        // Upon entering an object declaration context, the listener will attempt to
        // parse a planet class definition from the context and store it in the module's
        // planet class map.
        @Override
        public void enterObjectDeclaration(ObjectDeclarationContext context) {
            String identifier = context.key().getText();
            classes.put(identifier, Object.parseFrom(context));
        }

        // Upon entering a random list declaration context, the listener will attempt
        // to parse a random list definition from the context and store it in the
        // module's list of random planet classes.
        @Override
        public void enterRandomListDeclaration(RandomListDeclarationContext context) {
            RandomList.Builder builder = RandomList.parseFrom(context);
            builder.addAllPlanets(builder.classReferenceContexts().stream().map(Reference::new));
            builder.addAllPlanets(builder.stringLiteralContexts().stream().map(Reference::new));
            RandomList list = builder.buildPartial();
            lists.put(list.identifier().orElse("unnamed_list"), list);
        }

    }

    // Planet class modules contain a set of top level object definitions that
    // define planet classes in game. Although the last encountered definition is
    // the one used within a module, a multimap is used here for diagnostic
    // purposes.
    private final Multimap<String, PlanetClass> classes = MultimapBuilder.treeKeys().arrayListValues(1).build();

    // Planet class modules can also contain random list definitions, which are
    // named lists of planet classes for use by system initializers. As with the
    // planet classes, a multimap is used to capture duplicate entries.
    private final Multimap<String, RandomList> lists = MultimapBuilder.treeKeys().arrayListValues(1).build();

    /**
     * Create a new planet class module associated with the given file path.
     * 
     * @param path The path from which to parse planet class data.
     */
    private PlanetClassModule(final Path path) {
        super(path);
    }

    /**
     * Returns a new planet class module parsed from the file at the given path.
     * 
     * @param path The path of the file to parse.
     * @throws IOException If there is an unexpected I/O error while parsing the
     *                     file.
     */
    public static Collection<PlanetClassModule> parseFrom(final Path path) throws IOException {
        return parseFrom(path, PlanetClassModule::new);
    }

    /**
     * Returns an immutable multi-map of all planet classes defined within this
     * module.
     */
    public synchronized Multimap<String, PlanetClass> planetClasses() {
        return ImmutableMultimap.copyOf(this.classes);
    }

    /**
     * {@inheritDoc}
     */
    protected Listener listener() {
        return new Listener();
    }

    /**
     * Reparses the file associated with this module to process any changes. This
     * will overwrite any changes made to this module.
     * 
     * @throws IOException If there is an unexpected I/O error while parsing the
     *                     file.
     */
    @Override
    public synchronized void reparse() throws IOException {
        classes.clear();
        lists.clear();
        super.reparse();
    }

}
