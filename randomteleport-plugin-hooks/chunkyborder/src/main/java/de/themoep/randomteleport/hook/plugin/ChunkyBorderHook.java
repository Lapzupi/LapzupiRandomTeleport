package de.themoep.randomteleport.hook.plugin;

import de.themoep.randomteleport.hook.WorldborderHook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.popcraft.chunkyborder.BorderData;
import org.popcraft.chunkyborder.ChunkyBorder;
import org.popcraft.chunkyborder.ChunkyBorderProvider;

public class ChunkyBorderHook implements WorldborderHook {
    private final ChunkyBorder chunkyBorder;

    public ChunkyBorderHook() {
        this.chunkyBorder = ChunkyBorderProvider.get();

    }

    @Override
    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin("ChunkyBorder");
    }

    @Override
    public Location getCenter(@NotNull World world) {
        BorderData borderData = chunkyBorder.getBorders().get(world.getName());
        return new Location(world, borderData.getCenterX(),0D,borderData.getCenterZ());
    }

    @Override
    public double getBorderRadius(@NotNull World world) {
        BorderData borderData = chunkyBorder.getBorders().get(world.getName());
        return borderData.getRadiusX();
    }

    @Override
    public boolean isInsideBorder(@NotNull Location location) {
        BorderData borderData = chunkyBorder.getBorders().get(location.getWorld().getName());
        return borderData.getBorder().isBounding(location.getBlockX(),location.getBlockZ());
    }

    @Override
    public String getPluginName() {
        return "ChunkyBorder";
    }
}