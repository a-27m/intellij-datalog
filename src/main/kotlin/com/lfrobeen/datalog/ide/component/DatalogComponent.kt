package com.lfrobeen.datalog.ide.component

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.lfrobeen.datalog.ide.settings.DatalogPluginSettings
import java.util.Properties

@Service(Service.Level.APP)
class DatalogComponent {

    val updated: Boolean

    init {
        val pluginSettings = DatalogPluginSettings.getInstance()

        val previousVersion = pluginSettings.version
        val currentVersion = readPluginVersion()

        updated = previousVersion != currentVersion

        if (currentVersion != null && currentVersion != previousVersion) {
            pluginSettings.version = currentVersion
        }
    }

    // The version is written into a resource at build time (see processResources in build.gradle.kts).
    private fun readPluginVersion(): String? =
        javaClass.classLoader.getResourceAsStream("datalog/version.properties")?.use { stream ->
            Properties().apply { load(stream) }.getProperty("version")
        }

    companion object {
        fun getInstance(): DatalogComponent = service()
    }
}
