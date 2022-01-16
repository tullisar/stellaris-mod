package org.savetherobots.stellaris;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import com.google.common.collect.Multimap;

import org.savetherobots.stellaris.modules.clausewitz.PlanetClassModule;
import org.savetherobots.stellaris.modules.clausewitz.TraitModule;

/**
 * Hello world!
 */
public final class App {

    private App() {
    }

    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     */
    public static void main(String[] args) throws IOException {

        Path path = Paths.get("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Stellaris\\common\\planet_classes");
        // PlanetClassModule.parseFrom(path).stream().map(PlanetClassModule::planetClasses).map(Multimap::values).flatMap(Collection::stream).forEach(System.out::println);
        
        path = Paths.get("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Stellaris\\common\\traits");
        TraitModule.parseFrom(path).stream().map(TraitModule::traits).map(Multimap::values).flatMap(Collection::stream).forEach(System.out::println);
        
        

    }
}
