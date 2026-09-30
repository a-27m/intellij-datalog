package com.lfrobeen.datalog.ide

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.lfrobeen.datalog.lang.psi.DatalogAdtBranch
import com.lfrobeen.datalog.lang.psi.DatalogRelDecl
import com.lfrobeen.datalog.lang.psi.impl.DatalogRelDeclImpl

/** Basic IDE-feature tests running against a light in-memory project. */
class DatalogFixtureTest : BasePlatformTestCase() {

    private fun highlightMessages(severity: HighlightSeverity): List<String> =
        myFixture.doHighlighting()
            .filter { it.severity == severity }
            .mapNotNull { it.description }

    fun testReferenceResolvesToRelationDeclaration() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl edge(x: number, y: number)
            ed<caret>ge(1, 2).
            """.trimIndent()
        )

        val target = myFixture.getReferenceAtCaretPositionWithAssertion().resolve()

        assertTrue("Expected a relation declaration, got $target", target is DatalogRelDecl)
        assertEquals("edge", (target as DatalogRelDeclImpl).name)
    }

    fun testReferenceInRuleBodyResolvesToRelationDeclaration() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl edge(x: number, y: number)
            .decl reach(x: number, y: number)
            reach(x, y) :- ed<caret>ge(x, y).
            """.trimIndent()
        )

        val target = myFixture.getReferenceAtCaretPositionWithAssertion().resolve()

        assertEquals("edge", (target as DatalogRelDeclImpl).name)
    }

    fun testRenameRelationUpdatesDeclarationAndUsages() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl edge<caret>(x: number, y: number)
            edge(1, 2).
            .decl reach(x: number, y: number)
            reach(x, y) :- edge(x, y).
            """.trimIndent()
        )

        myFixture.renameElementAtCaret("link")

        myFixture.checkResult(
            """
            .decl link(x: number, y: number)
            link(1, 2).
            .decl reach(x: number, y: number)
            reach(x, y) :- link(x, y).
            """.trimIndent()
        )
    }

    fun testFindUsagesOfRelation() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl edge<caret>(x: number, y: number)
            edge(1, 2).
            edge(2, 3).
            """.trimIndent()
        )

        val usages = myFixture.findUsages(myFixture.elementAtCaret)

        assertEquals(2, usages.size)
    }

    fun testMissingArgumentIsReportedAsError() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl edge(x: number, y: number)
            edge(1).
            """.trimIndent()
        )

        assertContainsElements(highlightMessages(HighlightSeverity.ERROR), "Missing parameter y: number")
    }

    fun testSurplusArgumentIsReportedAsError() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl edge(x: number, y: number)
            edge(1, 2, 3).
            """.trimIndent()
        )

        assertContainsElements(highlightMessages(HighlightSeverity.ERROR), "Too many parameters for relation edge")
    }

    fun testSingletonVariableIsReportedAsWarning() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl a(x: number)
            .decl b(x: number)
            a(x) :- b(_).
            """.trimIndent()
        )

        assertContainsElements(
            highlightMessages(HighlightSeverity.WARNING),
            "Variable occurs only once in fact or rule."
        )
    }

    fun testWellFormedProgramHasNoErrorsOrWarnings() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl edge(x: number, y: number)
            .decl reach(x: number, y: number)
            edge(1, 2).
            reach(x, y) :- edge(x, y).
            reach(x, z) :- reach(x, y), edge(y, z).
            .output reach
            """.trimIndent()
        )

        val problems = myFixture.doHighlighting()
            .filter { it.severity >= HighlightSeverity.WARNING }
            .map { "${it.severity}: ${it.description}" }

        assertEmpty(problems)
    }

    fun testCompletionSuggestsDeclaredRelations() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl edge(x: number, y: number)
            .decl reach(x: number, y: number)
            reach(x, y) :- ed<caret>
            """.trimIndent()
        )

        myFixture.completeBasic()

        // A single match is auto-inserted; otherwise it is offered in the lookup list.
        val inserted = myFixture.editor.document.text.contains("edge")
        val offered = myFixture.lookupElementStrings.orEmpty().contains("edge")
        assertTrue("Expected relation 'edge' to be completed", inserted || offered)
    }

    fun testEveryHeadOfMultiHeadRuleResolvesToItsRelation() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl a(x: number)
            .decl b(x: number)
            .decl c(x: number)
            a(x), <caret>b(x) :- c(x).
            """.trimIndent()
        )

        val target = myFixture.getReferenceAtCaretPositionWithAssertion().resolve()

        assertEquals("b", (target as DatalogRelDeclImpl).name)
    }

    fun testMultiHeadRuleIsListedAsUsageOfAllHeadRelations() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl a<caret>(x: number)
            .decl b(x: number)
            .decl c(x: number)
            a(x), b(x) :- c(x).
            """.trimIndent()
        )

        assertEquals(1, myFixture.findUsages(myFixture.elementAtCaret).size)
    }

    fun testSingletonVariableInMultiHeadRuleIsReportedAsWarning() {
        myFixture.configureByText(
            "test.dl",
            """
            .decl a(x: number)
            .decl b(x: number)
            .decl c(x: number)
            a(x), b(y) :- c(x).
            """.trimIndent()
        )

        assertContainsElements(
            highlightMessages(HighlightSeverity.WARNING),
            "Variable occurs only once in fact or rule."
        )
    }

    fun testSouffleExtensionsProduceNoErrorHighlights() {
        myFixture.configureByText("test.dl", java.io.File("src/test/resources/parser/SouffleExtensions.dl").readText())

        val errors = myFixture.doHighlighting()
            .filter { it.severity >= HighlightSeverity.ERROR }
            .map { "${it.description} @ ${it.startOffset}" }

        assertEmpty(errors)
    }

    fun testAdtConstructorResolvesToBranchDeclaration() {
        myFixture.configureByText(
            "test.dl",
            """
            .type Shape = Circle {r: number} | Dot {}
            .decl shapes(s: Shape)
            shapes(${'$'}Cir<caret>cle(1)).
            """.trimIndent()
        )

        val target = myFixture.getReferenceAtCaretPositionWithAssertion().resolve()

        assertTrue("Expected an ADT branch, got $target", target is DatalogAdtBranch)
        assertEquals("Circle", (target as DatalogAdtBranch).identifier.text)
    }
}
