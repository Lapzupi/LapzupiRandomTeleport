package de.themoep.randomteleport.api;

import org.bukkit.Location;

import java.util.concurrent.CompletableFuture;

/**
 * @author sarhatabaot
 */
public interface Searcher {
    CompletableFuture<Location> search();
}
