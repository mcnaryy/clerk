plugins {
    kotlin("jvm") version "2.0.21"
}

group = "net.hellz"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {

    // SLF4J Logging
    implementation("org.slf4j:slf4j-simple:2.0.14")

    // Minestom Library
    implementation("net.minestom:minestom-snapshots:32735340d7")

    // MongoDB Driver (Reactive Streams + Multithreading)
    implementation("org.mongodb:mongodb-driver-reactivestreams:4.10.2")

    // Lamp Command Framework
    implementation("io.github.revxrsal:lamp.common:4.0.0-rc.4")
    implementation("io.github.revxrsal:lamp.minestom:4.0.0-rc.4")

    // Kotlin Reflections
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")

    // MCCoroutine
    implementation("com.github.shynixn.mccoroutine:mccoroutine-minestom-api:2.20.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-minestom-core:2.20.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.6.4")



}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}