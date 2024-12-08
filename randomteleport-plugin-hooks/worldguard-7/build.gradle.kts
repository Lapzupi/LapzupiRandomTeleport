plugins {
    id("de.themoep.randomteleport.java-conventions")
}

dependencies {
    implementation(project(":randomteleport-hook"))
    compileOnly(libs.bundles.worldguard)
    compileOnly(libs.spigot.api)
}

group = "de.themoep.randomteleport.pluginhook"
description = "worldguard-7"
