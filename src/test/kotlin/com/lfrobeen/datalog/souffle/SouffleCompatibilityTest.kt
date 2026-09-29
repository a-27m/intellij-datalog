package com.lfrobeen.datalog.souffle

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.lfrobeen.datalog.lang.DatalogFileType
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Cross-checks the plugin's parser against the real Soufflé parser (`souffle --show=initial-datalog` parses and
 * prints the program without evaluating it).
 *
 * Fixtures live in `src/test/resources/souffle`:
 *  - `valid/`      Soufflé accepts the file; the plugin must parse it without errors.
 *  - `invalid/`    Soufflé rejects the file; the plugin must report a syntax error.
 *  - `known-gaps/` Soufflé accepts the file but the plugin still reports errors. These are unsupported Soufflé
 *                  features (currently none). When a gap gets fixed, the test fails until the file is moved to
 *                  `valid/`.
 *
 * Without a `souffle` binary on the PATH the tests are skipped, unless `REQUIRE_SOUFFLE` is set (as in CI).
 */
class SouffleCompatibilityTest : BasePlatformTestCase() {

    private val fixtureRoot = File("src/test/resources/souffle")

    private fun fixtures(dir: String): List<File> =
        File(fixtureRoot, dir).listFiles { f -> f.extension == "dl" }.orEmpty().sortedBy { it.name }

    private fun soufflePresent(): Boolean = try {
        ProcessBuilder("souffle", "--version").redirectErrorStream(true).start().let {
            it.inputStream.readBytes()
            it.waitFor(30, TimeUnit.SECONDS) && it.exitValue() == 0
        }
    } catch (_: java.io.IOException) {
        false
    }

    /** Returns false (after marking the test as skipped) when Soufflé is unavailable. */
    private fun soufflePresentOrSkip(): Boolean {
        if (soufflePresent()) return true
        if (!System.getenv("REQUIRE_SOUFFLE").isNullOrEmpty()) fail("REQUIRE_SOUFFLE is set but `souffle` was not found")
        println("SKIPPED ${name}: `souffle` binary not found on PATH")
        return false
    }

    private fun souffleAccepts(file: File): Boolean {
        val process = ProcessBuilder("souffle", "--show=initial-datalog", file.absolutePath)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        check(process.waitFor(60, TimeUnit.SECONDS)) { "souffle timed out on ${file.name}" }
        if (process.exitValue() != 0) println("souffle rejected ${file.name}:\n$output")
        return process.exitValue() == 0
    }

    private fun pluginSyntaxErrors(file: File): List<String> {
        val psi = PsiFileFactory.getInstance(project)
            .createFileFromText(file.name, DatalogFileType, file.readText())
        return PsiTreeUtil.findChildrenOfType(psi, PsiErrorElement::class.java)
            .map { it.errorDescription }
    }

    fun testFixtureDirectoriesAreNotEmpty() {
        // known-gaps/ is empty whenever the plugin supports everything that is documented as missing.
        for (dir in listOf("valid", "invalid")) {
            assertFalse("No fixtures in $dir", fixtures(dir).isEmpty())
        }
    }

    fun testValidFixturesAreAcceptedByBoth() {
        if (!soufflePresentOrSkip()) return
        val problems = fixtures("valid").flatMap { file ->
            buildList {
                if (!souffleAccepts(file)) add("Soufflé rejects valid/${file.name}")
                pluginSyntaxErrors(file).forEach { add("Plugin error in valid/${file.name}: $it") }
            }
        }
        assertEmpty(problems)
    }

    fun testInvalidFixturesAreRejectedByBoth() {
        if (!soufflePresentOrSkip()) return
        val problems = fixtures("invalid").flatMap { file ->
            buildList {
                if (souffleAccepts(file)) add("Soufflé accepts invalid/${file.name}")
                if (pluginSyntaxErrors(file).isEmpty()) add("Plugin accepts invalid/${file.name}")
            }
        }
        assertEmpty(problems)
    }

    fun testKnownGapsAreAcceptedBySouffleButNotByPlugin() {
        if (!soufflePresentOrSkip()) return
        val problems = fixtures("known-gaps").flatMap { file ->
            buildList {
                if (!souffleAccepts(file)) add("Soufflé rejects known-gaps/${file.name}")
                if (pluginSyntaxErrors(file).isEmpty()) add("Plugin now parses known-gaps/${file.name}: move it to valid/")
            }
        }
        assertEmpty(problems)
    }
}
