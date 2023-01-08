package de.themoep.randomteleport.api;

import org.bukkit.Location;

/**
 * @author sarhatabaot
 */
public interface Validator<T extends Searcher> {
    /**
     * Validate a location
     * @param searcher  The searcher attempting to use this validator
     * @param location  The location to validate
     * @return          True if it's valid; false if not
     */
    boolean validate(T searcher, Location location);
    
    String getType();
}
