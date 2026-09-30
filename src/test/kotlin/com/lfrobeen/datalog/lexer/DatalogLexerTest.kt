package com.lfrobeen.datalog.lexer

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.lfrobeen.datalog.lang.lexer.DatalogLexer
import com.lfrobeen.datalog.lang.psi.DatalogTypes
import junit.framework.TestCase

class DatalogLexerTest : TestCase() {

    /** Token types by the name of their constant in [DatalogTypes] (their debug names are the raw token text). */
    private val tokenNames: Map<IElementType, String> =
        DatalogTypes::class.java.fields
            .filter { IElementType::class.java.isAssignableFrom(it.type) }
            .associate { (it.get(null) as IElementType) to it.name }

    /** Lexes [text] and returns `TOKEN(text)` for each non-whitespace token. */
    private fun lex(text: String): List<String> {
        val lexer = DatalogLexer()
        lexer.start(text)
        val tokens = mutableListOf<String>()
        while (true) {
            val type = lexer.tokenType ?: break
            if (type != TokenType.WHITE_SPACE) {
                val name = tokenNames[type] ?: type.toString()
                tokens += "$name(${lexer.tokenText})"
            }
            lexer.advance()
        }
        return tokens
    }

    private fun assertTokens(text: String, vararg expected: String) =
        assertEquals(expected.toList(), lex(text))

    fun testDirectives() = assertTokens(
        ".decl .type .symbol_type .number_type .comp .init .input .output .printsize .pragma .functor",
        "RELATION_DIRECTIVE(.decl)", "TYPE_DIRECTIVE(.type)", "TYPE_SYM_DIRECTIVE(.symbol_type)",
        "TYPE_NUM_DIRECTIVE(.number_type)", "COMP_DIRECTIVE(.comp)", "INIT_DIRECTIVE(.init)",
        "INPUT_DIRECTIVE(.input)", "OUTPUT_DIRECTIVE(.output)", "PRINTSIZE_DIRECTIVE(.printsize)",
        "PRAGMA_DIRECTIVE(.pragma)", "FUNCTOR_DIRECTIVE(.functor)",
    )

    fun testPrimitiveTypes() = assertTokens(
        "number symbol unsigned float",
        "NUMBER_TYPE(number)", "SYMBOL_TYPE(symbol)", "UNSIGNED_TYPE(unsigned)", "FLOAT_TYPE(float)",
    )

    fun testRelationQualifiers() = assertTokens(
        "output input printsize overridable inline brie btree eqrel",
        "OUTPUT_QUALIFIER(output)", "INPUT_QUALIFIER(input)", "PRINTSIZE_QUALIFIER(printsize)",
        "OVERRIDABLE_QUALIFIER(overridable)", "INLINE_QUALIFIER(inline)", "BRIE_QUALIFIER(brie)",
        "BTREE_QUALIFIER(btree)", "EQREL_QUALIFIER(eqrel)",
    )

    fun testNumbers() = assertTokens(
        "0 42 0x1F 0b101",
        "NUMBER_DEC(0)", "NUMBER_DEC(42)", "NUMBER_HEX(0x1F)", "NUMBER_BIN(0b101)",
    )

    fun testStringLiteral() = assertTokens("\"hello world\"", "STRING(\"hello world\")")

    fun testRuleOperators() = assertTokens(
        "a(x) :- b(x), !c(x); x != 1, x <= 2, x >= 3.",
        "IDENTIFIER(a)", "LPARENTH(()", "IDENTIFIER(x)", "RPARENTH())", "IF(:-)",
        "IDENTIFIER(b)", "LPARENTH(()", "IDENTIFIER(x)", "RPARENTH())", "COMMA(,)",
        "NOT(!)", "IDENTIFIER(c)", "LPARENTH(()", "IDENTIFIER(x)", "RPARENTH())", "SEMICOLON(;)",
        "IDENTIFIER(x)", "NOT_EQUAL(!=)", "NUMBER_DEC(1)", "COMMA(,)",
        "IDENTIFIER(x)", "LESS_OR_EQUAL(<=)", "NUMBER_DEC(2)", "COMMA(,)",
        "IDENTIFIER(x)", "MORE_OR_EQUAL(>=)", "NUMBER_DEC(3)", "DOT(.)",
    )

    fun testSubtypeOperator() = assertTokens(
        ".type A <: number",
        "TYPE_DIRECTIVE(.type)", "IDENTIFIER(A)", "LESS_COLON(<:)", "NUMBER_TYPE(number)",
    )

    fun testAggregateKeywordsUseTheirOwnTokens() = assertTokens(
        "count sum mean min max",
        "COUNT(count)", "SUM(sum)", "MEAN(mean)", "MIN(min)", "MAX(max)",
    )

    fun testWildcardAndAutoIncrement() = assertTokens("_ $ @f", "UNDERSCORE(_)", "DOLLAR($)", "AT(@)", "IDENTIFIER(f)")

    fun testComments() = assertTokens(
        "// line\na /* block */ b /** doc */ c",
        "COMMENT(// line)", "IDENTIFIER(a)", "COMMENT(/* block */)", "IDENTIFIER(b)",
        "COMMENT(/** doc */)", "IDENTIFIER(c)",
    )

    fun testPreprocessorDirectives() = assertTokens(
        "#include \"a.dl\"\n#ifdef X\n#endif",
        "INCLUDE_DIRECTIVE(#include)", "STRING(\"a.dl\")",
        "IFDEF_DIRECTIVE(#ifdef)", "IDENTIFIER(X)", "ENDIF_DIRECTIVE(#endif)",
    )

    fun testMacroDeclarationEndsAtLineBreak() = assertTokens(
        "#define ADD(a, b) a + b\nx",
        "DEFINE_DIRECTIVE(#define)", "IDENTIFIER(ADD)", "MACRO_TOKEN(()", "IDENTIFIER(a)", "MACRO_TOKEN(,)",
        "IDENTIFIER(b)", "MACRO_TOKEN())", "IDENTIFIER(a)", "MACRO_TOKEN(+)", "IDENTIFIER(b)",
        "LINE_BREAK(\n)", "IDENTIFIER(x)",
    )

    fun testMacroContinuationLine() = assertTokens(
        "#define A 1 \\\n + 2\nx",
        "DEFINE_DIRECTIVE(#define)", "IDENTIFIER(A)", "NUMBER_DEC(1)", "MACRO_CONTINUATION(\\\n)",
        "MACRO_TOKEN(+)", "NUMBER_DEC(2)", "LINE_BREAK(\n)", "IDENTIFIER(x)",
    )

    fun testUnknownCharacterIsBadCharacter() = assertTokens("`", "BAD_CHARACTER(`)")

    fun testFloatAndUnsignedLiterals() = assertTokens(
        "1.5 0.25 1u 0x1Fu 0b1u",
        "NUMBER_FLOAT(1.5)", "NUMBER_FLOAT(0.25)", "NUMBER_UNSIGNED(1u)",
        "NUMBER_UNSIGNED(0x1Fu)", "NUMBER_UNSIGNED(0b1u)",
    )

    fun testTrailingDotAfterIntegerIsNotAFloat() = assertTokens(
        "a(1).", "IDENTIFIER(a)", "LPARENTH(()", "NUMBER_DEC(1)", "RPARENTH())", "DOT(.)",
    )

    fun testStringWithEscapedQuote() = assertTokens(
        "\"say \\\"hi\\\"\" x",
        "STRING(\"say \\\"hi\\\"\")", "IDENTIFIER(x)",
    )

    fun testNewDirectives() = assertTokens(
        ".plan 0:(1) .limitsize a .override r",
        "PLAN_DIRECTIVE(.plan)", "NUMBER_DEC(0)", "COLON(:)", "LPARENTH(()", "NUMBER_DEC(1)", "RPARENTH())",
        "LIMITSIZE_DIRECTIVE(.limitsize)", "IDENTIFIER(a)", "OVERRIDE_DIRECTIVE(.override)", "IDENTIFIER(r)",
    )

    fun testPlanIsOnlyADirectiveBeforeWhitespace() = assertTokens(
        "g.plan(x)", "IDENTIFIER(g)", "DOT(.)", "IDENTIFIER(plan)", "LPARENTH(()", "IDENTIFIER(x)", "RPARENTH())",
    )

    fun testExtraQualifiersAndKeywords() = assertTokens(
        "no_inline magic no_magic btree_delete choice-domain stateful",
        "NO_INLINE_QUALIFIER(no_inline)", "MAGIC_QUALIFIER(magic)", "NO_MAGIC_QUALIFIER(no_magic)",
        "BTREE_DELETE_QUALIFIER(btree_delete)", "CHOICE_DOMAIN_KEYWORD(choice-domain)", "STATEFUL(stateful)",
    )

    fun testShiftOperators() = assertTokens(
        "bshl bshr bshru", "BSHL(bshl)", "BSHR(bshr)", "BSHRU(bshru)",
    )

    fun testConditionalPreprocessorLineIsLexedLikeMacroBody() = assertTokens(
        "#if defined(X) && 1\n#elif Y\n#else\n#endif",
        "IF_DIRECTIVE(#if)", "IDENTIFIER(defined)", "MACRO_TOKEN(()", "IDENTIFIER(X)", "MACRO_TOKEN())",
        "MACRO_TOKEN(&)", "MACRO_TOKEN(&)", "NUMBER_DEC(1)", "LINE_BREAK(\n)",
        "ELIF_DIRECTIVE(#elif)", "IDENTIFIER(Y)", "LINE_BREAK(\n)",
        "ELSE_DIRECTIVE(#else)", "ENDIF_DIRECTIVE(#endif)",
    )
}
