package com.lfrobeen.datalog.ide.activity

import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.lfrobeen.datalog.ide.component.DatalogComponent
import com.lfrobeen.datalog.ide.icons.DatalogIcons


class DatalogPluginUpdateActivity : ProjectActivity {
    private val notificationGroupId = "Datalog Plugin"

    private val notificationHeading = "Greetings from Datalog Plugin!"
    private val notificationContent by lazy {
        val stream = requireNotNull(javaClass.classLoader.getResourceAsStream("datalog/ide/notifications/update.html"))
        stream.bufferedReader(Charsets.UTF_8).use { it.readText() }.replace("\r\n", "\n")
    }

    private val links = listOf(
        "Star on GitHub" to "https://github.com/lfrobeen/intellij-datalog",
        "Rate the plugin" to "https://plugins.jetbrains.com/plugin/13056-datalog-language-support/",
        "Changelog" to "https://github.com/lfrobeen/intellij-datalog/blob/master/CHANGELOG.md",
        "Report an issue" to "https://github.com/lfrobeen/intellij-datalog/issues",
    )

    override suspend fun execute(project: Project) {
        if (!DatalogComponent.getInstance().updated) {
            return
        }

        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup(notificationGroupId)
            .createNotification(notificationHeading, notificationContent, NotificationType.INFORMATION)
            .setIcon(DatalogIcons.MAIN)

        links.forEach { (title, url) ->
            notification.addAction(NotificationAction.createSimple(title) { BrowserUtil.browse(url) })
        }

        notification.notify(project)
    }
}
