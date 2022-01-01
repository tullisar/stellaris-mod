package org.savetherobots.stellaris.types;

import java.util.Optional;

public interface PlanetClass extends GameObject 
{

    public static interface Builder extends GameObject.Builder
    {

        

    }

    /**
     * Returns a reference to the entity used for this planet class.
     */
    public Object entity();

    /**
     * Returns a reference to the icon used for this planet class.
     */
    public Object icon();

    /**
     * Returns a reference to the picture used for this planet class.
     */
    public Object picture();

    /**
     * Returns a reference to the climate for this planet class.
     */
    public Optional<Object> climate();

    /**
     * Returns true if planets of this class are colonizable, false otherwise.
     */
    public boolean isColonizable();

}
