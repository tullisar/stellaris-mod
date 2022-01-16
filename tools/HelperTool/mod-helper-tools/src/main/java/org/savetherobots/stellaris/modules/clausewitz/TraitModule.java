package org.savetherobots.stellaris.modules.clausewitz;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import org.inferred.freebuilder.FreeBuilder;
import org.savetherobots.stellaris.ParadoxClausewitzParser.CollectionValueContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.ObjectDeclarationContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.PropertyContext;
import org.savetherobots.stellaris.ParadoxClausewitzParserBaseListener;
import org.savetherobots.stellaris.database.DatabaseResolver;
import org.savetherobots.stellaris.types.Trait;

/**
 * Represents a source module containing trait declarations.
 */
public class TraitModule extends AbstractModule {

     /**
     * This is an abstract implementation of the trait interface. The builder
     * pattern is used to facilitate creating the objects from token context.
     */
    @FreeBuilder
    static abstract class Object extends AbstractModule.GameObject implements Trait {

        /**
         * Represents a builder for trait definitions.
         */
        protected static class Builder extends TraitModule_Object_Builder
                implements Trait.Builder, AbstractModule.GameObject.Builder {

            // Map the expected property keys to value consuming functions that will update
            // a builder
            public static final Map<String, BiConsumer<Builder, PropertyContext>> PROPERTY_SETTERS;
            static {
                PROPERTY_SETTERS = ImmutableMap.<String, BiConsumer<Builder, PropertyContext>>builder()
                        .put("initial", (b, c) -> b.booleanProperty(c, b::isInitial))
                        .put("randomized", (b, c) -> b.booleanProperty(c, b::isRandomized))
                        .put("modification", (b, c) -> b.booleanProperty(c, b::isModifiable))
                        .put("modifier", Builder::modifiers)
                        .put("allowed_archetypes", Builder::allowedArchetypes)
                        .build();
            }

            protected Builder() {
                isInitial(false);
                isRandomized(true);
                isModifiable(true);
            }

            public Builder modifiers(final PropertyContext context) {
                objectProperties(context).forEach(c -> {
                    putModifiers(c.propertyKey().getText(), c.value().getText());
                });
                return this;
            }

            public Builder allowedArchetypes(final PropertyContext context) {
                addAllAllowedArchetypes(collectionValue(context).map(CollectionValueContext::getText));
                return this;
            }

            @Override
            public Builder handledProperty(final PropertyContext context) {
                String key = context.propertyKey().getText();
                PROPERTY_SETTERS.get(key).accept(this, context);
                return this;
            }

            @Override
            public boolean isHandledProperty(PropertyContext context) {
                return PROPERTY_SETTERS.containsKey(context.propertyKey().getText());
            }

        }

        /**
         * Parses a planet class from the given object declaration parser context.
         * 
         * @param context The parser context from which to get planet class data.
         * @return the planet class that was parsed, which may not be a fully
         *         initialized planet class.
         */
        protected static final Object parseFrom(ObjectDeclarationContext context) {
            Builder builder = new Builder();
            builder.identifier(context.key().getText());
            builder.objectProperties(context.objectDefinition()).forEach(builder::property);
            return builder.buildPartial();
        }

    }

    /**
     * 
     */
    protected class Listener extends ParadoxClausewitzParserBaseListener {

        public void enterObjectDeclaration(final ObjectDeclarationContext context) {
            String identifier = context.key().getText();
            traits.put(identifier, Object.parseFrom(context));
        }

    }

    // Trait collection
    // TODO(tullisar): Document further
    private final Multimap<String, Trait> traits = MultimapBuilder.treeKeys().arrayListValues(1).build();

    /**
     * Create a new trait module associated with the given file path.
     * 
     * @param path The path from whcih to parse trait data.
     */

    private TraitModule(final Path path) {
        super(path);
    }

    /**
     * Returns a new planet class module parsed from the file at the given path.
     * 
     * @param path The path of the file to parse.
     * @throws IOException If there is an unexpected I/O error while parsing the
     *                     file.
     */
    public static Collection<TraitModule> parseFrom(final Path path) throws IOException {
        return parseFrom(path, TraitModule::new);
    }

    /**
     * {@inheritDoc}
     */
    protected Listener listener() {
        return new Listener();
    }

    /**
     * 
     */
    public Multimap<String, Trait> traits() {
        return ImmutableMultimap.copyOf(this.traits);
    }

}
