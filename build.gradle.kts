import org.gradle.kotlin.dsl.support.zipTo

plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.7.1"
    id("com.gradleup.shadow") version "8.3.7"
}

version = "1.0.0"
group = "dev.thorinwasher.forgery"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    implementation("dev.thorinwasher.schem:schem-reader:1.0.0")
    implementation("com.zaxxer:HikariCP:6.3.0")
    implementation("org.spongepowered:configurate-gson:4.2.0") {
        exclude("com.google.code.gson:gson")
    }
    compileOnly("org.xerial:sqlite-jdbc:3.50.3.0")
    compileOnly("org.spongepowered:configurate-yaml:4.2.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.3")
    testImplementation("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")
    testImplementation("org.xerial:sqlite-jdbc:3.50.3.0")
}

tasks {
    test {
        useJUnitPlatform()
    }


    runServer {
        minecraftVersion("1.21.8")
        downloadPlugins {
            modrinth("worldedit", "Jk1z2u7n")
        }
    }

    shadowJar {
        archiveBaseName.set(rootProject.name)
        archiveClassifier.unset()

        dependencies {
            exclude {
                it.moduleGroup == "org.jetbrains.kotlin"
                        || it.moduleGroup == "org.jetbrains.kotlinx"
                        || it.moduleGroup == "org.joml"
                        || it.moduleGroup == "org.slf4j"
            }
        }

        exclude("org/jetbrains/annotations/**")
        exclude("org/intellij/lang/annotations/**")

        listOf(
            "com.zaxxer.hikari",
            "dev.thorinwasher.schem",
            "net.kyori.adventure.nbt",
            "net.kyori.examination",
            "org.simpleyaml",
            "org.yaml.snakeyaml",
            "org.spongepowered.configurate.gson"
        ).forEach { relocate(it, "${project.group}.lib.$it") }
    }

    processResources {
        mustRunAfter("zipResources")
    }

    register("zipResources") {
        zipTo(File("./src/main/resources/exposed_resources.zip"), File("./src/main/exposed_resources"))
    }
}

bukkit {
    main = "dev.thorinwasher.forgery.Forgery"
    foliaSupported = false
    apiVersion = "1.21"
    authors = listOf("Thorinwasher")
    name = rootProject.name
}