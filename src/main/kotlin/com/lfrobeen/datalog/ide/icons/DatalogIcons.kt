package com.lfrobeen.datalog.ide.icons

import com.intellij.openapi.util.IconLoader

object DatalogIcons {

    private fun load(path: String) = IconLoader.getIcon(path, DatalogIcons::class.java.classLoader)

    @JvmField
    val RELATION = load("/icons/nodes/relation.svg")

    @JvmField
    val FUNCTOR = load("/icons/nodes/functor.svg")

    @JvmField
    val TYPE = load("/icons/nodes/type.svg")

    @JvmField
    val COMP = load("/icons/nodes/component.svg")

    @JvmField
    val INST = load("/icons/nodes/component_instance.svg")

    @JvmField
    val MACRO = load("/icons/nodes/macro.svg")

    @JvmField
    val COMMENT = load("/icons/nodes/comment.svg")

    @JvmField
    val FILE = load("/icons/datalog-file.svg")

    @JvmField
    val MAIN = load("/icon.svg")

}

