import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val ideaVersion = "2026.2"

group = "com.lfrobeen"
version = "2.1.0"

plugins {
    idea
    kotlin("jvm") version "2.4.0"
    id("org.jetbrains.intellij.platform") version "2.19.0"
    id("org.jetbrains.grammarkit") version "2022.3.2.2"
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
    }
}

// Grammar-Kit runs on its own (older) IDE classpath; keep it independent of the target platform.
grammarKit {
    jflexRelease.set("1.9.2")
    grammarKitRelease.set("2023.3")
    intellijRelease.set("233.15619.7")
}

idea {
    module {
        generatedSourceDirs.add(file("src/main/gen"))
    }
}

intellijPlatform {
    pluginConfiguration {
        name = "intellij-datalog"
        ideaVersion {
            sinceBuild = "262"
            untilBuild = provider { null }
        }
    }
    instrumentCode = false
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
        freeCompilerArgs.add("-Xjvm-default=all")
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

// Grammar-Kit's LightPsi bootstraps parts of the platform that expect an IDE home directory.
generateDatalogParser.configure {
    val ideaHome = layout.buildDirectory.dir("grammarkit-home").get().asFile
    doFirst { ideaHome.mkdirs() }
    systemProperty("idea.home.path", ideaHome.absolutePath)
    systemProperty("idea.config.path", File(ideaHome, "config").absolutePath)
    systemProperty("idea.system.path", File(ideaHome, "system").absolutePath)
}

tasks.compileKotlin {
    dependsOn(generateDatalogLexer, generateDatalogParser)
}

tasks.compileJava {
    dependsOn(generateDatalogLexer, generateDatalogParser)
}

tasks.withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
