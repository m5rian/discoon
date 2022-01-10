import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val jdaVersion: String by project
val jdaKtxVersion: String by project
val kotlinxVersion: String by project
val ktorVersion: String by project
val logbackVersion: String by project
val kmongoVersion: String by project
val kotlinguaVersion: String by project

plugins {
    kotlin("jvm") version "1.5.31"
    kotlin("plugin.serialization") version "1.5.30"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.github.m5rian"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven(url = "https://m5rian.jfrog.io/artifactory/java")
}

dependencies {
    val kotlinxGroup = "org.jetbrains.kotlinx"

    // Discord wrapper
    implementation("net.dv8tion:JDA:$jdaVersion")
    implementation("com.github.minndevelopment:jda-ktx:$jdaKtxVersion")
    // Coroutines
    implementation(group = kotlinxGroup, name = "kotlinx-coroutines-core", version = kotlinxVersion)
    implementation(group = kotlinxGroup, name = "kotlinx-coroutines-jdk8", version = kotlinxVersion)
    // Database
    implementation(group = "org.litote.kmongo", name = "kmongo", version = "4.3.0")
    // Serializer
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.2.2")
    // Logger
    implementation(group = "ch.qos.logback", name = "logback-classic", version = logbackVersion)
    // Multi-language handler
    implementation(group = "com.github.m5rian", name = "Kotlingua", version = kotlinguaVersion)
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Main-Class"] = "com.github.m5rian.discoon.BotKt"
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "16"
}