package com.lfrobeen.datalog.ide.component

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.extensions.PluginId
import com.lfrobeen.datalog.ide.settings.DatalogPluginSettings

@Service(Service.Level.APP)
class DatalogComponent {

    val updated: Boolean

    init {
        val plugin = PluginManagerCore.getPlugin(PluginId.getId("com.lfrobeen.intellij-datalog"))
        val pluginSettings = DatalogPluginSettings.getInstance()

        val previousVersion = pluginSettings.version
        val currentVersion = plugin?.version

        updated = previousVersion != currentVersion

        if (currentVersion != null && currentVersion != previousVersion) {
            pluginSettings.version = currentVersion
        }
    }

    companion object {
        fun getInstance(): DatalogComponent = service()
    }
}
