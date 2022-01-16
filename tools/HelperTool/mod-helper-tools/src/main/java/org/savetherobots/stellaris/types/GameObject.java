package org.savetherobots.stellaris.types;

import java.util.Optional;

import org.savetherobots.stellaris.database.DatabaseResolver;

/**
 * Represents a game object instance.
 */
public interface GameObject {

    /**
     * Represents an unresolved reference to a game object.
     */
    @FunctionalInterface
    public static interface Reference<T extends GameObject> {


        /**
         * Attempts to resolve this reference and return the game object that it
         * references.
         * 
         * @param resolver The datbase resolver to use when resolving this reference.
         * @return The object that was resolved, if available.
         */
        public Optional<T> get(final DatabaseResolver resolver);

    }

    /**
     * Represents a builder of game object instances.
     */
    public static interface Builder {

        /**
         * Sets the identifier of the game object being built to the given literal
         * string value.
         * 
         * @param identifier the identifier to set.
         * @return a reference to this builder.
         * @throws IllegalArgumentException if the identifier is not valid.
         */
        public Builder identifier(final String identifier);



    }

    /**
     * Returns the identifier of this object, if available. An object may not have
     * an identifier if it is declared in an anonymous scope.
     */
    public Optional<String> identifier();

}
