package de.themoep.randomteleport;

/*
 * RandomTeleport
 * Copyright (c) 2019 Max Lee aka Phoenix616 (mail@moep.tv)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.themoep.randomteleport.api.RandomTeleportAPI;
import de.themoep.randomteleport.hook.HookManager;
import de.themoep.randomteleport.listeners.SignListener;
import de.themoep.randomteleport.searcher.RandomSearcher;
import de.themoep.randomteleport.searcher.options.AdditionalOptionParser;
import de.themoep.randomteleport.searcher.options.NotFoundException;
import de.themoep.randomteleport.searcher.options.OptionParser;
import de.themoep.randomteleport.searcher.options.PlayerNotFoundException;
import de.themoep.randomteleport.searcher.options.SimpleOptionParser;
import de.themoep.randomteleport.searcher.options.WorldNotFoundException;
import de.themoep.randomteleport.searcher.validators.BiomeValidator;
import de.themoep.randomteleport.searcher.validators.BlockValidator;
import de.themoep.randomteleport.searcher.validators.HeightValidator;
import de.themoep.randomteleport.searcher.validators.LocationValidator;
import de.themoep.randomteleport.searcher.validators.ProtectionValidator;
import de.themoep.randomteleport.searcher.validators.WorldborderValidator;
import de.themoep.utils.lang.bukkit.LanguageManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RandomTeleport extends JavaPlugin implements RandomTeleportAPI {

    public static final Random RANDOM = new Random();
    private HookManager hookManager;
    private LanguageManager lang;
    private Table<String, UUID, Map.Entry<Long, Integer>> cooldowns = HashBasedTable.create();
    private Map<UUID, RandomSearcher> runningSearchers = new HashMap<>();

    private ValidatorRegistry locationValidators = new ValidatorRegistry();
    private List<OptionParser> optionParsers = new ArrayList<>();

    private Material[] saveBlocks;
    private Material[] unsaveBlocks;
    private Set<String> signVariables;

    public void onEnable() {
        hookManager = new HookManager(this);
        loadConfig();
        initOptionParsers();
        initValidators();
        getCommand("randomteleport").setExecutor(new RandomTeleportCommand(this));
        getServer().getPluginManager().registerEvents(new SignListener(this), this);
    }

    public void loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        saveBlocks = getConfig().getStringList("save-blocks").stream()
                .map(s -> {
                    Material mat = Material.matchMaterial(s);
                    if (mat == null) {
                        getLogger().log(Level.WARNING, "Error in save-blocks config! No material found with name " + s);
                    }
                    return mat;
                })
                .filter(Objects::nonNull)
                .toArray(Material[]::new);
        unsaveBlocks = getConfig().getStringList("unsave-blocks").stream()
                .map(s -> {
                    Material mat = Material.matchMaterial(s);
                    if (mat == null) {
                        getLogger().log(Level.WARNING, "Error in unsave-blocks config! No material found with name " + s);
                    }
                    return mat;
                })
                .filter(Objects::nonNull)
                .toArray(Material[]::new);
        signVariables = getConfig().getStringList("sign-variables").stream().map(String::toLowerCase).collect(Collectors.toSet());
        lang = new LanguageManager(this, getConfig().getString("lang"));
        lang.setPlaceholderPrefix("{");
        lang.setPlaceholderSuffix("}");
    }

    private void initOptionParsers() {
        addOptionParser(new SimpleOptionParser(array("p", "player"), (searcher, args) -> {
            if (args.length > 0) {
                List<Player> players = new ArrayList<>();
                for (String arg : args) {
                    for (String s : arg.split(",")) {
                        Player player = getServer().getPlayer(s);
                        if (player == null) {
                            throw new PlayerNotFoundException(s);
                        }
                        players.add(player);
                    }
                }
                searcher.getTargets().addAll(players);
                return true;
            }
            return false;
        }));
        addOptionParser(new SimpleOptionParser(array("x", "xpos"), (searcher, args) -> {
            if (args.length > 0) {
                searcher.getCenter().setX(Integer.parseInt(args[0]));
                return true;
            }
            return false;
        }));
        addOptionParser(new SimpleOptionParser(array("z", "zpos"), (searcher, args) -> {
            if (args.length > 0) {
                searcher.getCenter().setZ(Integer.parseInt(args[0]));
                return true;
            }
            return false;
        }));
        addOptionParser(new SimpleOptionParser(array("w", "world"), (searcher, args) -> {
            if (args.length > 0) {
                World world = getServer().getWorld(args[0]);
                if (world == null) {
                    throw new WorldNotFoundException(args[0]);
                }
                searcher.getCenter().setWorld(world);
                return true;
            }
            return false;
        }));
        addOptionParser(new SimpleOptionParser(array("c", "cooldown"), (searcher, args) -> {
            if (args.length > 0) {
                searcher.setCooldown(Integer.parseInt(args[0]));
                return true;
            }
            return false;
        }));
        addOptionParser(new SimpleOptionParser(array("f", "force"), (searcher, args) -> {
            if (args.length > 0) {
                if ("regions".equalsIgnoreCase(args[1])) {
                    searcher.getValidators().remove("protection");
                } else if ("blocks".equalsIgnoreCase(args[1])) {
                    searcher.getValidators().add(new BlockValidator(false, unsaveBlocks));
                } else {
                    throw new NotFoundException(args[1]);
                }
            } else {
                searcher.getValidators().remove("protection");
                searcher.getValidators().add(new BlockValidator(false, unsaveBlocks));
            }
            return true;
        }));
        addOptionParser(new SimpleOptionParser(array("b", "biome"), (searcher, args) -> {
            if (args.length > 0) {
                List<Biome> biomes = new ArrayList<>();
                for (String arg : args) {
                    biomes.add(Biome.valueOf(arg.toUpperCase()));
                }
                if (!biomes.isEmpty()) {
                    searcher.getValidators().add(new BiomeValidator(biomes.toArray(new Biome[0])));
                    return true;
                }
                return false;
            }
            return false;
        }));
        addOptionParser(new SimpleOptionParser(array("g", "generated", "l", "loaded"), (searcher, args) -> {
            // loaded is removed as we load chunks async which should no longer lead to performance issues
            // now it just works like the new "generated" option where it only checks generated chunks
            searcher.searchInGeneratedOnly(true);
            return true;
        }));
        addOptionParser(new SimpleOptionParser(array("id"), (searcher, args) -> {
            if (args.length > 0) {
                searcher.setId(args[0]);
                return true;
            }
            return false;
        }));
        addOptionParser(new AdditionalOptionParser("spawnpoint", "sp"));
    }

    private void initValidators() {
        locationValidators.add(new WorldborderValidator());
        locationValidators.add(new HeightValidator());
        locationValidators.add(new ProtectionValidator());
        locationValidators.add(new BlockValidator(saveBlocks));
    }

    /**
     * Get the map of all running searchers
     * @return The map of running searchers
     */
    public Map<UUID, RandomSearcher> getRunningSearchers() {
        return runningSearchers;
    }

    /**
     * Utility method to create arrays with a nicer syntax. Seriously, why does Java not just accept {"string"} as parameters?!?
     * @param array The array values
     * @return The same array
     */
    private static <T> T[] array(T... array) {
        return array;
    }

    public boolean sendMessage(Collection<? extends CommandSender> senders, String key, String... replacements) {
        boolean r = false;
        for (CommandSender sender : senders) {
            r |= sendMessage(sender, key, replacements);
        }
        return r;
    }

    public boolean sendMessage(CommandSender sender, String key, String... replacements) {
        String message = lang.getConfig(sender).get(key, replacements);
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(message);
            return true;
        }
        return false;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public ValidatorRegistry getLocationValidators() {
        return locationValidators;
    }

    /**
     *
     * @return
     */
    public List<OptionParser> getOptionParsers() {
        return optionParsers;
    }

    /**
     * Add an option parser to this plugin
     * @param parser The parser to add
     */
    public void addOptionParser(OptionParser parser) {
        optionParsers.add(parser);
    }

    /**
     * Check whether or not a sign line matches the configured variables
     * @param line The line to match
     * @return <tt>true</tt> if it matches; <tt>false</tt> if not
     */
    public boolean matchesSignVariable(String line) {
        return signVariables.contains(line.toLowerCase());
    }

    /**
     * Create and run a searcher using specified args the same way the command does
     * @param sender    The sender of the command
     * @param center    The center location for the searcher
     * @param args      The arguments to parse
     * @return Returns the searcher that is running or null if it was stopped due to a cooldown
     * @throws IllegalArgumentException Thrown when arguments couldn't be handled properly
     */
    public RandomSearcher parseAndRun(CommandSender sender, Location center, String[] args) {
        RandomSearcher searcher = new RandomSearcher(this, sender, center, Integer.parseInt(args[0]), Integer.parseInt(args[1]));

        String[] optionArgs = Arrays.copyOfRange(args, 2, args.length);
        for (OptionParser parser : getOptionParsers()) {
            parser.parse(searcher, optionArgs);
        }

        int cooldown = 0;

        for (Entity target : searcher.getTargets()) {
            Map.Entry<Long, Integer> lastUse = cooldowns.get(searcher.getId(), target.getUniqueId());
            if (lastUse != null) {
                int targetCooldown = (int) ((System.currentTimeMillis() - lastUse.getKey()) / 1000);
                if (targetCooldown > cooldown) {
                    cooldown = targetCooldown;
                }
            }
        }

        if (cooldown > 0 && cooldown < searcher.getCooldown()) {
            sendMessage(searcher.getTargets(), "error.cooldown", "cooldown_text", (searcher.getCooldown() - cooldown) + "s");
            return null;
        }
        sendMessage(searcher.getTargets(), "search", "worldname", searcher.getCenter().getWorld().getName());
        searcher.search().thenApply(targetLoc -> {
            targetLoc.add(0, 1, 0);
            searcher.getTargets().forEach(e -> {
                cooldowns.put(searcher.getId(), e.getUniqueId(), new AbstractMap.SimpleImmutableEntry<>(System.currentTimeMillis(), searcher.getCooldown()));
                e.teleport(targetLoc);
                sendMessage(e, "teleport",
                        "worldname", targetLoc.getWorld().getName(),
                        "x", String.valueOf(targetLoc.getBlockX()),
                        "y", String.valueOf(targetLoc.getBlockY()),
                        "z", String.valueOf(targetLoc.getBlockZ())
                );
                if (searcher.getOptions().containsKey("spawnpoint") && e instanceof Player) {
                    if (((Player) e).getBedSpawnLocation() == null || "force".equalsIgnoreCase(searcher.getOptions().get("spawnpoint"))){
                        ((Player) e).setBedSpawnLocation(targetLoc, true);
                        sendMessage(e, "setspawnpoint",
                                "worldname", targetLoc.getWorld().getName(),
                                "x", String.valueOf(targetLoc.getBlockX()),
                                "y", String.valueOf(targetLoc.getBlockY()),
                                "z", String.valueOf(targetLoc.getBlockZ())
                        );
                    }
                }
            });
            return true;
        }).exceptionally(ex -> {
            sendMessage(sender, "error.location");
            sender.sendMessage(ex.getMessage());
            searcher.getTargets().forEach(e -> sendMessage(e, "error.location"));
            return true;
        });
        return searcher;
    }

    /**
     * Run a preset
     * @param sender The sender that executed the preset
     * @param preset The preset ID to run
     * @param target The player targeted by the teleporter
     * @param center The center for the search
     * @return The RandomSearcher instance that is searching
     */
    public RandomSearcher runPreset(CommandSender sender, String preset, Player target, Location center) {
        String cmd = getConfig().getString("presets." + preset) + " -p " + target.getName();
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }
        if (cmd.startsWith("rtp ")) {
            cmd = cmd.substring(4);
        }
        return parseAndRun(sender, center, cmd.split(" "));
    }

    @Override
    public CompletableFuture<Location> getRandomLocation(Player player, Location origin, int minRange, int maxRange, LocationValidator... validators) {
        return getRandomSearcher(player, origin, minRange, maxRange, validators).search();
    }

    @Override
    public CompletableFuture<Boolean> teleportToRandomLocation(Player player, Location origin, int minRange, int maxRange, LocationValidator... validators) {
        return getRandomLocation(player, origin, minRange, maxRange, validators).thenApply(player::teleport);
    }

    @Override
    public RandomSearcher getRandomSearcher(Player player, Location origin, int minRange, int maxRange, LocationValidator... validators) {
        return new RandomSearcher(this, player, origin, minRange, maxRange, validators);
    }
}
