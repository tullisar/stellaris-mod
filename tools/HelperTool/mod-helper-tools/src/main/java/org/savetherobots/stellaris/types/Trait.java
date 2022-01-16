package org.savetherobots.stellaris.types;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

/**
 * Represents a trait game object.
 */
public interface Trait extends GameObject {

    /**
     * Represents an unresovled trait object.
     */
    public static interface Reference extends GameObject.Reference<Trait> {
        // Intentionally empty at this time.
    }

    /**
     * Represents a builder of trait game objects.
     */
    public static interface Builder extends GameObject.Builder {
        // Intentionally empty at this time.
    }

    /**
     * Returns {@code true} if this trait can be an initial trait. The meaning of
     * this depends on the trait type.
     */
    public boolean isInitial();

    /**
     * Returns {@code true} if this trait can be picked by randomization of traits
     * where applicable, {@code false} otherwise.
     */
    public boolean isRandomized();

    /**
     * Returns {@code true} if this trait can be added or removed when modifying the
     * scoped owner. This may or may not have any effect for leader traits.
     */
    public boolean isModifiable();

    /**
     * Returns a map of modifiers this applies to the scoped object (species or
     * ldeader). At this time, this map just associates modifier keys with values.
     */
    public Map<? extends Object, ? extends Object> modifiers();

    /**
     * Returns a collection of the allowed archetypes associated with this species
     * trait.
     */
    public Set<? extends Object> allowedArchetypes();

    /**
     * Returns the sorting priority of this trait. Traits with the same sorting
     * priority will be sorted alphabetically by name.
     */
    public OptionalInt sortingPriority();

    /**
     * Returns a reference to the ideal planet class defined by this trait. Traits
     * which do not match the appropriate naming pattern must have this property set
     * to a suitable planet class to be considered a habitability trait.
     */
    public Optional<? extends PlanetClass.Reference> idealPlanetClass();

}
