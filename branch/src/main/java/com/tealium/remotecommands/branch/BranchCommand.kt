package com.tealium.remotecommands.branch

import io.branch.referral.Branch
import org.json.JSONObject

interface BranchCommand : Branch.BranchReferralInitListener {
    fun initialize(config: JSONObject?)
    fun sendEvent(eventName: String, payload: JSONObject)
    fun setIdentity(id: String)
    fun setOptOut(opt: Boolean)
    fun createDeepLink(properties: JSONObject)
    fun logout()
}