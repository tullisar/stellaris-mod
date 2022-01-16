package org.savetherobots.stellaris.database;

import java.util.Optional;

import org.savetherobots.stellaris.types.PlanetClass;
import org.savetherobots.stellaris.types.Trait;

/**
 * TBD
 */
public interface DatabaseResolver {


    /**
     * TBD
     */
    public default Optional<String> resolveIdentifier( final String string )
    {
        return Optional.of(string);
    }

    /**
     * Resolves a trait from the given reference within the databases known to this
     * resolver.
     * 
     * @param reference The trait reference to resolve.
     * 
     * @return The trait that was resolved, if found.
     */
    public default Optional<? extends Trait> resolveTrait(final Trait.Reference reference) {
        return Optional.empty();
    }

    /**
     * Resolves a planet class from the given reference within the databases known
     * to this resolver.
     * 
     * @param reference
     * @return
     */
    public default Optional<? extends PlanetClass> resolvePlanetClass(final String identifier) {
        return Optional.empty();
    }
}
