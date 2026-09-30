package com.lfrobeen.datalog.parser

import com.intellij.lang.LanguageASTFactory
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.ParsingTestCase
import com.lfrobeen.datalog.lang.DatalogASTFactory
import com.lfrobeen.datalog.lang.DatalogLanguage
import com.lfrobeen.datalog.lang.parser.DatalogParserDefinition

/**
 * Golden-file parser tests.
 *
 * Every test `testFooBar` parses `src/test/resources/parser/fooBar.dl` and compares the PSI tree against
 * `fooBar.txt`. To (re)generate a golden file, delete it and run the test once; review the diff before committing.
 */
class DatalogParsingTest : ParsingTestCase("", "dl", DatalogParserDefinition()) {

    override fun setUp() {
        super.setUp()
        addExplicitExtension(LanguageASTFactory.INSTANCE, DatalogLanguage, DatalogASTFactory())
    }

    override fun getTestDataPath(): String = "src/test/resources/parser"

    override fun includeRanges(): Boolean = true

    /** The file must match its golden tree and contain no error elements. */
    private fun assertParsesCleanly() = doTest(true, true)

    /** The file must match its golden tree, and the tree must contain at least one error element. */
    private fun assertParseErrors() {
        doTest(true, false)
        assertTrue(
            "Expected at least one PsiErrorElement",
            PsiTreeUtil.findChildOfType(myFile, PsiErrorElement::class.java) != null
        )
    }

    fun testDeclarations() = assertParsesCleanly()

    fun testTypes() = assertParsesCleanly()

    fun testRules() = assertParsesCleanly()

    fun testExpressions() = assertParsesCleanly()

    fun testAggregates() = assertParsesCleanly()

    fun testInputOutput() = assertParsesCleanly()

    fun testComponents() = assertParsesCleanly()

    fun testPreprocessor() = assertParsesCleanly()

    fun testComments() = assertParsesCleanly()

    fun testFamilyExample() = assertParsesCleanly()

    fun testSouffleExtensions() = assertParsesCleanly()

    fun testErrorRecovery() = assertParseErrors()
}
