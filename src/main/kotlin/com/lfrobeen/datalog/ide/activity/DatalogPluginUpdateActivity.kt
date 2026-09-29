package com.lfrobeen.datalog.ide.activity

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.util.io.StreamUtil
import com.lfrobeen.datalog.ide.component.DatalogComponent
import com.lfrobeen.datalog.ide.icons.DatalogIcons


class DatalogPluginUpdateActivity : ProjectActivity {
    private val notificationGroupId = "Datalog Plugin"

    private val notificationHeading = "Greetings from Datalog Plugin!"
    private val notificationContent by lazy {
        val stream = javaClass.classLoader.getResourceAsStream("datalog/ide/notifications/update.html")
        StreamUtil.convertSeparators(StreamUtil.readText(stream!!, "UTF-8"))
    }

    override suspend fun execute(project: Project) {
        if (!DatalogComponent.getInstance().updated) {
            return
        }

        @Suppress("DEPRECATION")
        NotificationGroupManager.getInstance()
            .getNotificationGroup(notificationGroupId)
            .createNotification(notificationHeading, notificationContent, NotificationType.INFORMATION)
            .setIcon(DatalogIcons.MAIN)
            .setListener(NotificationListener.URL_OPENING_LISTENER)
            .notify(project)
    }
}
