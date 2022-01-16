package org.savetherobots.stellaris.types;

import java.util.List;
import java.util.Optional;

/**
 * Represents a planet class game object.
 */
public interface PlanetClass extends GameObject {

    /**
     * Represents an unresovled reference to a planet class object.
     */
    public static interface Reference extends GameObject.Reference<PlanetClass> {
        // Intentionally empty at this time.
    }

    /**
     * Represents a bulider of planet class objects.
     */
    public static interface Builder extends GameObject.Builder {
        // Intentionally empty at this time.
    }

    /**
     * An interface representing a random planet class list definition. A random
     * planet class list defines a name for reference in system definitions and
     * planet creation, as well as a list of identifiers for planet class
     * definitions.
     */
    public static interface RandomList extends GameObject {

        /**
         * Represents a builder of random planet list objects.
         */
        public static interface Builder extends GameObject.Builder {

        }

        /**
         * Returns an immutable list of the planet classes in this random list.
         */
        public List<? extends PlanetClass.Reference> planets();
    }

    /**
     * Returns a reference to the entity used for this planet class.
     */
    public Optional<Object> entity();

    /**
     * Returns a reference to the icon used for this planet class.
     */
    public Optional<Object> icon();

    /**
     * Returns a reference to the picture used for this planet class.
     */
    public Optional<Object> picture();

    /**
     * Returns a reference to the climate for this planet class.
     */
    public Optional<Object> climate();

    /**
     * Returns {@code true} if this planet class is a type of star, {@code false}
     * otherwise.
     * 
     * @return
     */
    public boolean isStar();

    /**
     * Returns {@code true} if planets of this class are colonizable, {@code false}
     * otherwise.
     */
    public boolean isColonizable();

    /**
     * Returns {@code true} if planets of this class are considered ring world
     * megastructures, {@code false} otherwise.
     */
    public boolean isRingWorld();

    /**
     * Returns {@code true} if planets of this class may be starting planets,
     * {@code false} otherwise.
     */
    public boolean isStartingPlanet();

    /**
     * Returns {@code true} if planets of this class may be starting planets,
     * {@code false} otherwise.
     * 
     * <p>
     * <b>Note:</b> This appears to be a duplicate/deprecated variant of
     * starting_planet.
     * </p>
     */
    public boolean isInitial();

    /**
     * Returns {@code true} if planets of this class are 100% habitable to all
     * species.
     */
    public boolean isIdeal();

    /**
     * Returns {@code true} if planets of this class are artificial (affects
     * planetary modifiers).
     */
    public boolean isArtificial();

    /**
     * Returns the district set this planet class uses, if defined.
     */
    public Optional<Object> districtSet();

}
