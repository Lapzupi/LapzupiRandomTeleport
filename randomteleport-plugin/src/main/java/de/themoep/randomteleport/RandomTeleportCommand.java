package de.themoep.randomteleport;

/*
 * RandomTeleport - randomteleport-plugin - $project.description
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

import de.themoep.randomteleport.searcher.RandomSearcher;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.logging.Level;

public class RandomTeleportCommand implements CommandExecutor {
    private final RandomTeleport plugin;

    public RandomTeleportCommand(RandomTeleport plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,@NotNull  Command command,@NotNull  String label, String @NotNull [] args) {
        if (args.length == 0) {
            if (sender instanceof Player player) {
                String preset = "default";
                if (plugin.getConfig().getBoolean("use-player-world-as-preset", false)) {
                    String worldName = player.getWorld().getName().toLowerCase();
                    if (presetExistsInConfig(worldName))
                        preset = worldName;
                }
                runPreset(preset, sender, player, player.getLocation());
                return true;
            }
            return false;
        }
        if (args.length == 1) {
            if ("--reload".equalsIgnoreCase(args[0]) && sender.hasPermission("randomteleport.reload")) {
                plugin.loadConfig();
                plugin.sendMessage(sender, "reloaded");
                return true;
            } else if ("--stat".equalsIgnoreCase(args[0]) && sender.hasPermission("randomteleport.stat")) {
                plugin.getLogger().info("Unimplemented.");
                //TODO: teleporter and searcher statistics
            } else if (sender instanceof Player player) {
                runPreset(args[0].toLowerCase(), sender, player, player.getLocation());
                return true;
            }
            return false;
        }

        try {
            if (sender.hasPermission("randomteleport.manual")) {
                plugin.parseAndRun(sender, getLocation(sender), args);
            } else {
                plugin.sendMessage(sender, "error.no-permission.general", Map.of("perm", "randomteleport.manual"));
            }
            return true;
        } catch (IllegalArgumentException e) {
            if (args.length == 2) {
                Player target = plugin.getServer().getPlayer(args[1]);
                if (target == null) {
                    plugin.sendMessage(sender, "error.player-not-found", Map.of("what", args[1]));
                    return true;
                }
                runPreset(args[0].toLowerCase(), sender, target, target.getLocation());
                return true;
            }
            sender.sendMessage(e.getMessage());
        }

        return false;
    }

    private void runPreset(String preset, @NotNull CommandSender sender, Player target, Location center) {
        if (!sender.hasPermission("randomteleport.presets." + preset)) {
            plugin.sendMessage(sender, "error.no-permission.preset",
                    Map.of("preset", preset, "perm",
                    "randomteleport.presets." + preset)
            );
            return;
        }
        if (sender != target && !sender.hasPermission("randomteleport.tpothers")) {
            plugin.sendMessage(sender, "error.no-permission.tp-others", Map.of("perm", "randomteleport.tpothers"));
            return;
        }
        if (!presetExistsInConfig(preset)) {
            plugin.sendMessage(sender, "error.preset-doesnt-exist", Map.of("preset", preset));
            return;
        }

        if (sender == target) {
            for (RandomSearcher searcher : plugin.getRunningSearchers().values()) {
                if (searcher.getTargets().contains(target)) {
                    plugin.sendMessage(sender, "error.already-searching", Map.of("preset", preset));
                    return;
                }
            }
        }

        try {
            plugin.runPreset(plugin.getServer().getConsoleSender(), preset, target, center);
        } catch (IllegalArgumentException e) {
            plugin.sendMessage(sender, "error.preset-invalid", Map.of("preset", preset));
            plugin.getLogger().log(Level.SEVERE, "Error while parsing preset " + preset, e);
        }

    }

    private boolean presetExistsInConfig(String preset) {
        return plugin.getConfig().getString("presets." + preset) != null;
    }

    private static @NotNull Location getLocation(CommandSender sender) {
        if (sender instanceof Entity entity) {
            return entity.getLocation();
        } else if (sender instanceof BlockCommandSender blockCommandSender) {
            return blockCommandSender.getBlock().getLocation();
        }
        return new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
    }
}
