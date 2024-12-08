plugins {
    id("de.themoep.randomteleport.java-conventions")
}

dependencies {
    implementation(project(":randomteleport-hook"))
    compileOnly(libs.griefdefender)
    compileOnly(libs.spigot.api)
}

group = "de.themoep.randomteleport.pluginhook"
description = "griefdefender"
