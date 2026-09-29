import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val ideaVersion = "2026.2"

group = "com.lfrobeen"
version = "2.1.0"

plugins {
    idea
    kotlin("jvm") version "2.4.20"
    id("org.jetbrains.intellij.platform") version "2.19.0"
    id("org.jetbrains.grammarkit") version "2023.3.0.4"
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        // Since 2025.3 IntelliJ IDEA is distributed as a single unified product.
        create(IntelliJPlatformType.IntellijIdea, ideaVersion)
        testFramework(TestFrameworkType.Platform)
    }

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.opentest4j:opentest4j:1.3.0")
}

idea {
    module {
        generatedSourceDirs.add(file("src/main/gen"))
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "262"
            untilBuild = provider { null }
        }
    }
    instrumentCode = false
    pluginVerification {
        ides {
            create(IntelliJPlatformType.IntellijIdea, ideaVersion)
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}

sourceSets {
    main {
        java.srcDirs("src/main/gen")
        resources.srcDirs("src/main/resources")
    }
    test {
        resources.srcDirs("src/test/resources")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_25)
        jvmDefault.set(JvmDefaultMode.NO_COMPATIBILITY)
    }
    sourceSets {
        main {
            kotlin.srcDirs("src/main/kotlin")
        }
        test {
            kotlin.srcDirs("src/test/kotlin")
        }
    }
}

val generateDatalogLexer = tasks.register<GenerateLexerTask>("generateDatalogLexer") {
    sourceFile.set(file("src/main/grammars/datalog.flex"))
    targetOutputDir.set(file("src/main/gen/com/lfrobeen/datalog/lang/lexer"))
    purgeOldFiles.set(true)
}

val generateDatalogParser = tasks.register<GenerateParserTask>("generateDatalogParser") {
    sourceFile.set(file("src/main/grammars/datalog.bnf"))
    targetRootOutputDir.set(file("src/main/gen"))
    pathToParser.set("/datalog/lang/parser/DatalogParser.java")
    pathToPsiRoot.set("/datalog/lang/psi")
    purgeOldFiles.set(true)
}

tasks.compileKotlin {
    dependsOn(generateDatalogLexer, generateDatalogParser)
}

tasks.compileJava {
    dependsOn(generateDatalogLexer, generateDatalogParser)
}

// Expose the plugin version at runtime without touching internal PluginManager APIs.
tasks.processResources {
    filesMatching("datalog/version.properties") {
        expand("version" to project.version.toString())
    }
}

tasks.test {
    useJUnit()
    // The Soufflé cross-check tests fail (instead of being skipped) when this is set and `souffle` is missing.
    environment("REQUIRE_SOUFFLE", System.getenv("REQUIRE_SOUFFLE") ?: "")
    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
