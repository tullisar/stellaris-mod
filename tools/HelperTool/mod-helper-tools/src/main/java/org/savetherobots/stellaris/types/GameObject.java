package org.savetherobots.stellaris.types;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.savetherobots.stellaris.ParadoxClausewitzLexer;

public interface GameObject {

    public static interface Builder
    {

    }

    public static boolean isValidIdentifier(final String identifier)
    {
        // TODO(robertb):
        return true;
    }

    // Intentionally empty at this time
}
