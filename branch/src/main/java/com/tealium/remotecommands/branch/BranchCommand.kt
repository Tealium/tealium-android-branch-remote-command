package com.tealium.remotecommands.branch

import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.util.BranchEvent
import io.branch.referral.util.LinkProperties

interface BranchCommand : Branch.BranchReferralInitListener {
    fun initialize(branchKey: String?, enableLogging: Boolean, collectDeviceId: Boolean)
    fun sendEvent(event: BranchEvent)
    fun setIdentity(id: String)
    fun setOptOut(opt: Boolean)
    fun createDeepLink(buo: BranchUniversalObject, linkProperties: LinkProperties)
    fun logout()
}