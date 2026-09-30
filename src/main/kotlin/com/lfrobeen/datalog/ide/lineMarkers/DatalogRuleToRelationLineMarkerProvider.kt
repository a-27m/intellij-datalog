package com.lfrobeen.datalog.ide.lineMarkers

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.lfrobeen.datalog.lang.psi.DatalogClause
import com.lfrobeen.datalog.lang.psi.impl.DatalogRelDeclImpl

class DatalogRuleToRelationLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is DatalogClause)
            return

        // A rule can have several heads, e.g. `a(x), b(x) :- c(x).`
        val relations = element.clauseHeadList
            .mapNotNull { it.atom.anyReference.reference?.resolve() as? DatalogRelDeclImpl }
        if (relations.isEmpty())
            return

        val builder = NavigationGutterIconBuilder
            .create(AllIcons.Gutter.ImplementingMethod)
            .setTargets(relations)
            .setTooltipText("Rule for relation ${relations.joinToString { it.name.orEmpty() }}")

        result.add(builder.createLineMarkerInfo(element))
    }
}
