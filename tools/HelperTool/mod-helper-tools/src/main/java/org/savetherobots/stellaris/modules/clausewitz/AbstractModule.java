package org.savetherobots.stellaris.modules.clausewitz;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.savetherobots.stellaris.ParadoxClausewitzLexer;
import org.savetherobots.stellaris.ParadoxClausewitzParser;
import org.savetherobots.stellaris.ParadoxClausewitzParser.CollectionContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.CollectionValueContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.ContainerContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.LiteralContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.MapContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.MapEntryContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.ObjectDefinitionContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.ObjectReferenceContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.PropertyContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.ReferenceContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.StringContentsContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.StringLiteralContext;
import org.savetherobots.stellaris.ParadoxClausewitzParser.ValueContext;
import org.savetherobots.stellaris.ParadoxClausewitzParserListener;

/**
 * An abstract implementation of a source file that contains game object
 * definitions.
 */
public abstract class AbstractModule {

    /**
     * An abstract imlementation of the game object interface which is used by the
     * various module classes inheriting from {@link AbstractModule}.
     */
    protected static abstract class GameObject implements org.savetherobots.stellaris.types.GameObject {

        /**
         * 
         */
        protected static interface Builder extends org.savetherobots.stellaris.types.GameObject.Builder {

            /**
             * Returns {@code true}} if the given strin represents a valid game object
             * identifier, {@code false} otherwise. Builders may override this method
             * if the range of valid identifiers is different.
             * 
             * @param string The string to check.
             */
            public default boolean isValidIdentifier(final String string) {
                return GameObject.isValidIdentifier(string);
            }

            /**
             * Handles a property context that describes an unsupported property key for the
             * game object being built.
             * 
             * @param context The property context that was not supported.
             * @return A reference to this builder.
             */
            public default Builder unhandledProperty(final PropertyContext context) {
                // This method does nothing unless overridden
                return this;
            }

            /**
             * Returns a runnable (for use with Optional.ifPresentOrElse and the like) which
             * will call this builder's {@link #unhandledProperty(PropertyContext)} method
             * with the provided property context.
             * 
             * @param context The property context for which to return a new runnable.
             */
            public default Runnable unhandledPropertyHandler(final PropertyContext context) {
                return () -> unhandledProperty(context);
            }

            /**
             * Handles a property context that describes an invalid value for the associated
             * property key in the game object being built.
             * 
             * @param context The property context that was not supported.
             * @return A reference to this builder.
             */
            public default Builder invalidValue(final PropertyContext context) {
                // This method does nothing unless overridden.
                return this;
            }

            /**
             * Returns a runnable (for use with Optional.ifPresentOrElse and the like) which
             * will call this builder's {@link #invalidValue(PropertyContext)} method with
             * the provided property context.
             * 
             * @param context The property context for which to return a new runnable.
             */
            public default Runnable invalidValueHandler(final PropertyContext context) {
                return () -> invalidValue(context);
            }

            /**
             * Updates a booelan value property (yes/no) from the given context. If the
             * property context's value describes a valid boolean value, then the game
             * object being built will have the property value updated. Otherwise, the
             * {@link #invalidValue(PropertyContext)} method is called with the given
             * property context.
             * 
             * @param context  The property context from which to get the boolean value.
             * @param consumer The consumer function to update the builder's property value
             *                 if valid.
             * @return A reference to this builder.
             */
            public default Builder booleanProperty(final PropertyContext context, Consumer<Boolean> consumer) {
                booleanLiteral(context).ifPresentOrElse(consumer, invalidValueHandler(context));
                return this;
            }

            /**
             * Updates a string value property from the given context. If the property
             * context's value describes a valid string literal value, then the game
             * object being built will have the property value updated. Otherwise, the
             * {@link #invalidValue(PropertyContext)} method is called with the given
             * property context.
             * 
             * @param context  The property context from which to get the string value.
             * @param consumer The consumer function to update the builder's property value
             *                 if valid.
             * @return A reference to this builder.
             */
            public default Builder stringProperty(final PropertyContext context, Consumer<String> consumer) {
                stringLiteral(context).ifPresentOrElse(consumer, invalidValueHandler(context));
                return this;
            }

            /**
             * Returns a collection of collection value parser contexts for the given
             * property context, provided that it describes a valid collection. If the
             * property context does not describe a valid string, then the
             * {@link #invalidValue(PropertyContext)} method is called with the property
             * context.
             * 
             * @param context The property context for which to get a stream of collection
             *                values.
             */

            public default Stream<? extends CollectionValueContext> collectionValue(final PropertyContext context) {

                Optional<? extends Collection<? extends CollectionValueContext>> collection = Optional.of(context)
                        .map(PropertyContext::value).map(ValueContext::container).map(ContainerContext::collection)
                        .map(CollectionContext::collectionValue);

                if (!collection.isPresent()) {
                    invalidValue(context);
                    return Stream.empty();
                }

                return collection.get().stream();
            }

            /**
             * Returns a stream of property contexts for an object definition defined in the
             * given property context. If the property context does not describe a valid
             * object definition, then the {@link #invalidValue(PropertyContext)} method is
             * called.
             * 
             * @param context The property context for which to get a stream of object
             *                properties.
             */
            public default Stream<? extends PropertyContext> objectProperties(final ObjectDefinitionContext context) {

                Optional<List<MapEntryContext>> entries = Optional.of(context).map(ObjectDefinitionContext::map)
                        .map(MapContext::mapEntry);

                if (entries.isEmpty()) {
                    // invalidValue(context);
                    return Stream.empty();
                }

                return entries.stream().flatMap(List::stream).map(MapEntryContext::property).filter(Objects::nonNull);
            }

            /**
             * Returns a stream of property contexts for an object definition defined in the
             * given property context. If the property context does not describe a valid
             * object definition, then the {@link #invalidValue(PropertyContext)} method is
             * called.
             * 
             * @param context The property context for which to get a stream of object
             *                properties.
             */
            public default Stream<? extends PropertyContext> objectProperties(final PropertyContext context) {
                return Optional.of(context).map(PropertyContext::value)
                        .map(ValueContext::container)
                        .map(ContainerContext::objectDefinition).stream().flatMap(this::objectProperties);
            }

            /**
             * Updates a identifier property from the given context. If the property
             * context's value describes a valid identifier, then the game
             * object being built will have the property value updated. Otherwise, the
             * {@link #invalidValue(PropertyContext)} method is called with the given
             * property context.
             * 
             * @param context  The property context from which to get the identifier value.
             * @param consumer The consumer function to update the builder's property value
             *                 if valid.
             * @return A reference to this builder.
             */
            public default Builder identifierProperty(final PropertyContext context, Consumer<String> consumer) {
                Optional<String> identifier = objectReference(context).map(ObjectReferenceContext::getText)
                        .or(() -> stringLiteral(context))
                        .filter((i) -> this.isValidIdentifier(i));

                identifier.ifPresentOrElse(consumer, invalidValueHandler(context));
                return this;
            }

            /**
             * Determines whether the given property context describes a property key which
             * is supported for the game object being built.
             * 
             * @param context The property context to validate.
             * @return {@code true} if the given context is a valid property, {@code false}
             *         otherwise.
             */
            public boolean isHandledProperty(final PropertyContext context);

            /**
             * Accepts a parsed property context that is guaranteed to be a property key
             * that is supported by the game object being built.
             * 
             * @param context The property context to parse.
             * @return A reference to this builder.
             */
            public Builder handledProperty(final PropertyContext context);

            /**
             * Accepts a parsed property context and updates the game object being built a
             * appropriate. May result in {@link #unhandledProperty(PropertyContext)} being
             * called if the given property context is not valid for the game object being
             * built.
             * 
             * @param context The property context for the object to update.
             * @return A reference to this builder.
             */
            public default Builder property(final PropertyContext context) {
                checkNotNull(context);
                return isHandledProperty(context) ? handledProperty(context) : unhandledProperty(context);
            }

        }

        /**
         * Returns {@code true} if the given sting is a valid game object identifier.
         * 
         * @param string The string to check for validity.
         */
        protected static boolean isValidIdentifier(final String string) {
            return true;
        }

    }

    // The path for a module will never change. It must be exported/saved as for
    // this to change.
    private final Path path;

    /**
     * Constructs a new abstract module with the given details.
     * 
     * @param path the path to the module in the file system.
     */
    protected AbstractModule(final Path path) {
        this.path = checkNotNull(path);
        checkArgument(Files.isRegularFile(path));
    }

    /**
     * Returns the string literal contents of a given value context, if applicable.
     * 
     * @param context the value context from which to get the string literal
     *                contents.
     */
    public static Optional<String> stringLiteral(final ValueContext context) {
        return Optional.of(context).map(ValueContext::literal).map(LiteralContext::stringLiteral)
                .map(StringLiteralContext::stringContents).map(StringContentsContext::getText);
    }

    public static Optional<StringLiteralContext> stringLiteralContext(final CollectionValueContext context) {
        return Optional.of(context).map(CollectionValueContext::literal).map(LiteralContext::stringLiteral);
    }

    /**
     * Returns the string literal contents of a given value context, if applicable.
     * 
     * @param context the value context from which to get the string literal
     *                contents.
     */
    public static Optional<String> stringLiteral(final CollectionValueContext context) {
        return Optional.of(context).map(CollectionValueContext::literal).map(LiteralContext::stringLiteral)
                .map(StringLiteralContext::stringContents).map(StringContentsContext::getText);
    }

    public static Optional<String> stringLiteral(final PropertyContext context) {
        return stringLiteral(checkNotNull(context).value());
    }

    public static Optional<Boolean> booleanLiteral(final ValueContext context) {
        return Optional.of(context).map(ValueContext::literal).map(LiteralContext::booleanLiteral)
                .map(c -> (c.YES() != null));
    }

    public static Optional<Boolean> booleanLiteral(final PropertyContext context) {
        return booleanLiteral(checkNotNull(context).value());
    }

    public static Optional<ObjectReferenceContext> objectReference(final PropertyContext context) {
        return Optional.of(context).map(PropertyContext::value).flatMap(AbstractModule::objectReference);
    }

    public static Optional<ObjectReferenceContext> objectReference(final ValueContext context) {
        return Optional.of(context).map(ValueContext::reference)
                .map(ReferenceContext::objectReference);
    }

    public static Optional<ReferenceContext> reference(final CollectionValueContext context) {
        return Optional.of(context).map(CollectionValueContext::reference);
    }

    /**
     * 
     * @param <T>
     * @param path
     * @param constructor
     * @return
     * @throws IOException
     */
    protected static <T extends AbstractModule> Collection<T> parseFrom(final Path path,
            final Function<Path, T> constructor) throws IOException {
        ImmutableList.Builder<T> modules = ImmutableList.builder();
        if (Files.isDirectory(checkNotNull(path))) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, "*.txt")) {
                Iterator<Path> paths = stream.iterator();
                while (paths.hasNext()) {
                    Path current = paths.next();
                    System.out.println("Parsing File: " + current.toString());
                    T module = constructor.apply(current);
                    module.parse();
                    modules.add(module);
                }
            }
        } else {
            T module = constructor.apply(path);
            module.parse();
            modules.add(module);
        }

        return modules.build();
    }

    /**
     * Returns the path of this module in the file system.
     */
    public final Path path() {
        return path;
    }

    /**
     * Returns a parser listener to be used when parsing this module.
     */
    protected abstract ParadoxClausewitzParserListener listener();

    /**
     * Parses the file associated with this module and populates it with the
     * resulting data.
     * 
     * @throws IOException If there is an unexpected I/O error while parsing the
     *                     file.
     */
    protected synchronized void parse() throws IOException {
        ParadoxClausewitzLexer lexer = new ParadoxClausewitzLexer(CharStreams.fromPath(path));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ParadoxClausewitzParser parser = new ParadoxClausewitzParser(tokens);
        ParseTree tree = parser.module();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(listener(), tree);
    }

    /**
     * Reparses the file associated with this module to process any changes. This
     * will overwrite any changes made to this module.
     * 
     * @throws IOException If there is an unexpected I/O error while parsing the
     *                     file.
     */
    public synchronized void reparse() throws IOException {
        parse();
    }

}
