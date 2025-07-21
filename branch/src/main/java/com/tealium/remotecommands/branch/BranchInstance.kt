package com.tealium.remotecommands.branch

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.tealium.remotecommands.RemoteCommandContext
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.util.BranchEvent
import io.branch.referral.util.LinkProperties
import org.json.JSONArray
import org.json.JSONObject

class BranchInstance(
    private val application: Application,
    private var branchKey: String? = null,
    private val remoteCommandContext: RemoteCommandContext
) : BranchCommand, Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null

    override fun initialize(branchKey: String?, enableLogging: Boolean, collectDeviceId: Boolean) {
        branchKey?.let {
            Branch.getAutoInstance(application, it)
        } ?: run {
            Branch.getAutoInstance(application)
        }

        Branch.sessionBuilder(currentActivity).withCallback(this).init()

        if (enableLogging) {
            Branch.enableLogging()
        }

        Branch.disableDeviceIDFetch(collectDeviceId)
    }

    override fun sendEvent(event: BranchEvent) {
        event.logEvent(application)
    }

    override fun setIdentity(id: String) {
        getBranch().setIdentity(id)
    }

    override fun setOptOut(opt: Boolean) {
        getBranch().disableTracking(opt)
    }

    override fun createDeepLink(buo: BranchUniversalObject, linkProperties: LinkProperties) {
        buo.generateShortUrl(
            application.applicationContext,
            linkProperties,
            branchLinkCreateListener
        )
    }

    override fun logout() {
        getBranch().logout()
    }

    private val branchLinkCreateListener =
        Branch.BranchLinkCreateListener { url, error ->
            if (error == null) {
                remoteCommandContext.track(EventKey.BRANCH_CREATE_DEEPLINK, mapOf("branch_short_url" to url))
            }
        }

    private fun JSONObject.toMap(): Map<String, *> = keys().asSequence().associateWith { key ->
        when (val value = this[key]) {
            is JSONArray -> {
                val map = (0 until value.length()).associate { Pair(it.toString(), value[it]) }
                JSONObject(map).toMap().values.toList()
            }
            is JSONObject -> value.toMap()
            JSONObject.NULL -> null
            else -> value
        }
    }

    private fun getBranch(): Branch {
        return branchKey?.let { Branch.getAutoInstance(application, it) }
            ?: Branch.getAutoInstance(application)
    }

    override fun onInitFinished(referringParams: JSONObject?, error: BranchError?) {
        remoteCommandContext.track(EventKey.BRANCH_REFERRING_PARAMS, referringParams?.toMap())
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}