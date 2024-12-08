plugins {
    id("de.themoep.randomteleport.java-conventions")
}

dependencies {
    implementation(project(":randomteleport-hook"))
    compileOnly(libs.bundles.chunky)
    compileOnly(libs.spigot.api)
}

group = "de.themoep.randomteleport.pluginhook"
description = "chunkyborder"
