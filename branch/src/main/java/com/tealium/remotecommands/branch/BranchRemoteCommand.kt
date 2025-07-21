package com.tealium.remotecommands.branch

import android.app.Application
import android.util.Log
import com.tealium.remotecommands.RemoteCommand
import com.tealium.remotecommands.RemoteCommandContext
import io.branch.indexing.BranchUniversalObject
import org.json.JSONObject
import java.util.*

class BranchRemoteCommand(
    private val application: Application,
    private val branchKey: String,
    commandId: String = DEFAULT_COMMAND_ID,
    description: String = DEFAULT_COMMAND_DESCRIPTION
) : RemoteCommand(commandId, description, BuildConfig.TEALIUM_BRANCH_VERSION) {

    lateinit var branchInstance: BranchCommand

    public override fun onInvoke(response: Response) {
        val payload = response.requestPayload
        val commands = splitCommands(payload)
        parseCommands(commands, payload)
    }

    fun parseCommands(commands: Array<String>, payload: JSONObject) {
        commands.forEach { command ->
            when (command) {
                Commands.INITIALIZE -> handleInitialize(payload)
                Commands.SET_USER_ID -> handleSetUserId(payload)
                Commands.CREATE_DEEP_LINK -> handleCreateDeepLink(payload)
                Commands.LOGOUT -> handleLogout()
                else -> handleCustomEvent(command, payload)
            }
        }
    }

    private fun handleInitialize(payload: JSONObject) {
        val devKey = payload.optString(Config.DEV_KEY, "")
        val actualBranchKey = if (devKey.isNotEmpty()) devKey else branchKey
        
        if (actualBranchKey.isNullOrEmpty()) {
            Log.e(BuildConfig.TAG, "${Config.DEV_KEY} not found. Initializing without branch key.")
        }
        
        val settings = payload.optJSONObject(Config.SETTINGS)
        val enableLogging = settings?.optBoolean(Config.ENABLE_LOGGING, false) ?: false
        val collectDeviceId = settings?.optBoolean(Config.COLLECT_DEVICE_ID, false) ?: false
        
        branchInstance.initialize(actualBranchKey, enableLogging, collectDeviceId)
    }

    private fun handleSetUserId(payload: JSONObject) {
        val id = payload.optString(EventKey.USER_ID, "")
        if (id.isNotEmpty()) {
            branchInstance.setIdentity(id)
        } else {
            Log.e(BuildConfig.TAG, "${EventKey.USER_ID} is a required key")
        }
    }

    private fun handleCreateDeepLink(payload: JSONObject) {
        val linkProperties = payload.optJSONObject(EventKey.LINK_PROPERTIES)
        linkProperties?.let { props ->
            val buo = props.optJSONObject(EventKey.BUO_PROPERTIES)?.let { buoProps ->
                BranchEventBuilder.buildBranchUniversalObject(buoProps, payload)
            } ?: BranchUniversalObject()
            val linkProps = BranchEventBuilder.buildLinkProperties(props)
            
            branchInstance.createDeepLink(buo, linkProps)
        } ?: Log.e(
            BuildConfig.TAG,
            "${EventKey.LINK_PROPERTIES} is a required object with link properties"
        )
    }

    private fun handleLogout() {
        branchInstance.logout()
    }

    private fun handleCustomEvent(command: String, payload: JSONObject) {
        val event = BranchEventBuilder.buildEvent(command, payload)
        branchInstance.sendEvent(event)
    }

    fun splitCommands(payload: JSONObject): Array<String> {
        val command = payload.optString(EventKey.COMMAND_KEY)
        return command.split(EventKey.SEPARATOR.toRegex())
            .map { it.trim().lowercase(Locale.ROOT) }
            .toTypedArray()
    }

    override fun setContext(context: RemoteCommandContext?) {
        context?.let {
            branchInstance = BranchInstance(application, branchKey, it)
        }
    }

    companion object {
        const val DEFAULT_COMMAND_ID = "branch"
        const val DEFAULT_COMMAND_DESCRIPTION = "Tealium-Branch Remote Command"
    }
}