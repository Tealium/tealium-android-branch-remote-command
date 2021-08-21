package com.tealium.example

import android.app.Application
import android.webkit.WebView
import com.tealium.core.*
import com.tealium.dispatcher.TealiumEvent
import com.tealium.dispatcher.TealiumView
import com.tealium.lifecycle.Lifecycle
import com.tealium.remotecommanddispatcher.RemoteCommands
import com.tealium.remotecommanddispatcher.remoteCommands
import com.tealium.remotecommands.branch.BranchRemoteCommand
import com.tealium.tagmanagementdispatcher.TagManagement

object TealiumHelper {
    // Identifier for the main Tealium instance
    val TEALIUM_MAIN = "main"
    private lateinit var tealium: Tealium
    private val branchDevKey = "key_test_pm0EAhAZdE5ShDQ8NcclNbbiuDkWj5Kt"
// TODO    private val branchDevKey = "your_dev_key"

    fun initialize(application: Application) {

        WebView.setWebContentsDebuggingEnabled(true)

        val config = TealiumConfig(
            application,
            "tealiummobile",
            "demo",
            Environment.DEV,
            dispatchers = mutableSetOf(
                Dispatchers.RemoteCommands,
                Dispatchers.TagManagement
            ),
            modules = mutableSetOf(Lifecycle)
        ).apply {
            useRemoteLibrarySettings = true
        }

        tealium = Tealium.create(TEALIUM_MAIN, config) {
            val branchRemoteCommand = BranchRemoteCommand(application, branchDevKey)

            // Remote Command Tag - requires TiQ
//            remoteCommands?.add(branchRemoteCommand)

            // JSON Remote Command - requires local filename or url to remote file
            remoteCommands?.add(branchRemoteCommand, filename = "branch.json")
        }
    }

    fun trackView(viewName: String, data: Map<String, Any>?) {
        tealium.track(TealiumView(viewName, data))

    }

    fun trackEvent(eventName: String, data: Map<String, Any>?) {
        tealium.track(TealiumEvent(eventName, data))
    }
}