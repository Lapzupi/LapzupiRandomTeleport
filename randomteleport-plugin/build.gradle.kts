import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("de.themoep.randomteleport.java-conventions")
    alias(libs.plugins.shadow)
    alias(libs.plugins.bukkit.yml)
}

dependencies {
    implementation(project(":randomteleport-api"))
    implementation(project(":randomteleport-hook"))
    implementation(project(":chunkyborder"))
    implementation(project(":worldguard-7"))
    implementation(project(":griefdefender"))
    compileOnly(libs.spigot.api)
    compileOnly(libs.placeholder.api)
    library(libs.commons.lang3)
}

group = "de.themoep.randomteleport.randomteleport-plugin"
description = "randomteleport-plugin"

tasks {
    shadowJar {
        archiveBaseName.set("RandomTeleport")
        archiveClassifier.set("")
        archiveVersion.set("")
        relocate("io.papermc.lib", "de.themoep.randomteleport.libs.paperlib")
        relocate("de.themoep.utils.lang", "de.themoep.randomteleport.libs.lang")
    }
}

bukkit {
    name = "RandomTeleport"
    version = "2.1.0"
    apiVersion = "1.21"
    description = project.description.toString()
    author = "Phoenix616"
    website = "https://github.com/Phoenix616/RandomTeleport/"
    main = "de.themoep.randomteleport.RandomTeleport"
    softDepend = listOf(
        "WorldGuard",
        "Factions",
        "GriefPrevention",
        "GriefDefender",
        "RedProtect",
        "WorldBorder",
        "PlaceholderAPI",
        "ChunkyBorder"
    )

    commands {
        create("randomteleport") {
            description = "RandomTeleport command."
            aliases = listOf("randomtp", "rtp")
            permission = "randomteleport.use"
            usage = """
                /<command> - uses the default preset
                /<command> <preset> [<playername>] - uses a specific preset
                /<command> <minRange> <maxRange> [-p, -w, -x, -z, -c, -f]
                minRange - minimum distance to the center point (square shaped)
                maxRange - maximum distance to the center point (square shaped)
                Options:
                > -p,-player <playername> - teleports other players
                > -w,-world <worldname> - teleports the player in a specific world
                > -b,-biome <biomename> [<biome 2> ...] - only teleport to this biome (multiple allowed, Bukkit biome names!)
                > -x,-xPos <x value> - x axis of the center point, if not set the player's x axis is used
                > -z,-zPos <z value> - z axis of the center point, if not set the player's z axis is used
                > -minY <y value> - minimum y value that the random location should have (default: 0)
                > -maxY <y value> - maximum y value that the random location should have (default: world height, half in nether)
                > -l,-loaded - only search loaded chunks for possible locations (might fail more often)
                > -g,-generated - only search generated chunks for possible locations
                > -c, -cooldown <seconds> - cooldown in seconds after which the player can use this teleporter again
                > -id <id> - The ID to use for the cooldown, uses automatically generated one if not provided
                > -f,-force - teleport even if there is no dirt/grass/sand/gravel, only checks for lava/water/cactus, ignores WorldGuard/Faction regions
                > -f,-force [<blocks|regions>] - only ignore blocks or regions
                > -t,-tries <amount> - the amount of times the plugin should try to find a random location before giving up
                > -sp,spawnpoint [force] - set the respawn point of the player to the location he teleported to (force overrides existing spawnpoint)
                /<command> --stat - shows a statistic of the teleports since the last restart
                /<command> --reload - reloads the config
            """
        }
    }

    permissions {
        register("randomteleport.use") {
            description = "Gives permission to the command"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.manual") {
            description = "Gives permission to manually specify parameters in the command"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.manual.option.*") {
            description = "Gives permission to use certain options in the command"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.tpothers") {
            description = "Gives permission to teleport other players"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.cooldownexempt") {
            description = "Teleport cooldown does not affect these players"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.stat") {
            description = "Permission for showing the teleport statistic"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.reload") {
            description = "Permission to use the reload command"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.presets.default") {
            description = "Gives permission to use the default random teleport preset"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.presets.*") {
            description = "Gives permission to use all random teleport presets"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.sign.preset.default") {
            description = "Gives permission to use the default preset with a right-click on a preset sign"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.sign.preset.*") {
            description = "Gives permission to use all presets with a right-click on a preset sign"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.sign.create") {
            description = "Allows creating preset signs ([rtp] or [RandomTP] on the 2nd line and the preset name on the 3rd)"
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("randomteleport.sign.destroy") {
            description = "Allows destroying preset signs ([rtp] or [RandomTP] on the 2nd line and the preset name on the 3rd)"
            default = BukkitPluginDescription.Permission.Default.OP
        }
    }
}


