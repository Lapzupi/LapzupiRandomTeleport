rootProject.name = "randomteleport-parent"

include(":randomteleport-hook")
include(":randomteleport-plugin")
include(":randomteleport-plugin-hooks")
include(":worldguard-7")
include(":chunkyborder")
include(":griefdefender")
include(":griefprevention")
include(":randomteleport-api")

project(":worldguard-7").projectDir = file("randomteleport-plugin-hooks/worldguard-7")
project(":chunkyborder").projectDir = file("randomteleport-plugin-hooks/chunkyborder")
project(":griefdefender").projectDir = file("randomteleport-plugin-hooks/griefdefender")
project(":griefprevention").projectDir = file("randomteleport-plugin-hooks/griefprevention")


dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("worldguard-core","com.sk89q.worldguard:worldguard-core:7.1.0-SNAPSHOT")
            library("worldguard-bukkit","com.sk89q.worldguard:worldguard-bukkit:7.1.0-SNAPSHOT")
            bundle("worldguard", listOf("worldguard-core", "worldguard-bukkit"))
            library("spigot-api","org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
            library("griefprevention", "com.github.TechFortress:GriefPrevention:16.12.0")
            library("griefdefender", "com.griefdefender:api:2.1.0-20220122.032038-5")
            library("placeholder-api", "me.clip:placeholderapi:2.11.6")

            library("chunkyborder-common", "org.popcraft:chunkyborder-common:1.1.34")
            library("chunkyborder-bukkit", "org.popcraft:chunkyborder-bukkit:1.1.34")
            library("chunky", "org.popcraft:chunky-common:1.3.38")
            bundle("chunky", listOf("chunky", "chunkyborder-common", "chunkyborder-bukkit"))

            plugin("shadow", "com.gradleup.shadow").version("9.0.0-beta4")
            plugin("bukkit-yml", "net.minecrell.plugin-yml.bukkit").version("0.6.0")
            plugin("paper-userdev", "io.papermc.paperweight.userdev").version("1.7.7")
            plugin("run-paper", "xyz.jpenilla.run-paper").version("2.3.1")

            library("commons-lang3", "org.apache.commons:commons-lang3:3.12.0")
        }
    }
}