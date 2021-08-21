package com.tealium.remotecommands.branch

import android.app.Application
import android.util.Log
import com.tealium.remotecommands.RemoteCommand
import org.json.JSONObject
import java.util.*

class BranchRemoteCommand(
    private val application: Application,
    private val branchKey: String,
    commandId: String = DEFAULT_COMMAND_ID,
    description: String = DEFAULT_COMMAND_DESCRIPTION,
    private val branchInstance: BranchCommand = BranchInstance(application, branchKey)
) : RemoteCommand(commandId, description) {

    public override fun onInvoke(response: Response) {
        val payload = response.requestPayload
        val commands = splitCommands(payload)
        parseCommands(commands, payload)
    }

    fun parseCommands(commands: Array<String>, payload: JSONObject) {
        commands.forEach { command ->
            when (command) {
                Commands.INITIALIZE -> {
                    val config = payload.optJSONObject(EventKey.CONFIG)
                    branchInstance.initialize(config)
                }
                Commands.SET_USER_ID -> {
                    val id = payload.optString(EventKey.USER_ID, "")
                    if (id.isNotEmpty()) {
                        branchInstance.setIdentity(id)
                    } else {
                        Log.e(BuildConfig.TAG, "${EventKey.USER_ID} is a required key")
                    }
                }
                Commands.CREATE_DEEP_LINK -> {
                    val linkProperties = payload.optJSONObject(EventKey.LINK_PROPERTIES)
                    linkProperties?.let {
                        branchInstance.createDeepLink(it)
                    } ?: Log.e(
                        BuildConfig.TAG,
                        "${EventKey.LINK_PROPERTIES} is a required object with link properties"
                    )
                }
                Commands.LOGOUT -> {
                    branchInstance.logout()
                }
                else -> {
                    branchInstance.sendEvent(command, payload)
                }
            }
        }
    }

    fun splitCommands(payload: JSONObject): Array<String> {
        val command = payload.optString(EventKey.COMMAND_KEY)
        return command.split(EventKey.SEPARATOR.toRegex())
            .map { it.trim().toLowerCase(Locale.ROOT) }
            .toTypedArray()
    }

    companion object {
        const val DEFAULT_COMMAND_ID = "branch"
        const val DEFAULT_COMMAND_DESCRIPTION = "Tealium-Branch Remote Command"
    }
}