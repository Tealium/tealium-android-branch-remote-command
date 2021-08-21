package com.tealium.remotecommands.branch

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.tealium.remotecommands.RemoteCommandContext
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.BranchError
import io.branch.referral.util.*
import org.json.JSONArray
import org.json.JSONObject

class BranchInstance(
    private val application: Application,
    private var branchKey: String? = null,
    private val remoteCommandContext: RemoteCommandContext? = null
) : BranchCommand, Application.ActivityLifecycleCallbacks {

    private var currentActivity: Activity? = null

    override fun initialize(config: JSONObject?) {
        val devKey = config?.optString(Config.DEV_KEY, "")
        devKey?.let {
            if (it.isNotEmpty()) {
                branchKey = it
            }
        }

        branchKey?.let {
            Branch.getAutoInstance(application, it)
        } ?: run {
            Log.e(BuildConfig.TAG, "${Config.DEV_KEY} not found. Initializing without branch key.")
            Branch.getAutoInstance(application)
        }

        Branch.sessionBuilder(currentActivity).withCallback(this).init()

        val settings = config?.optJSONObject(Config.SETTINGS)
        val logging = settings?.optBoolean(Config.ENABLE_LOGGING, false)

        if (logging == true) {
            Branch.enableLogging()
        }

        val deviceIdFetching = settings?.optBoolean(Config.COLLECT_DEVICE_ID, false)
        Branch.disableDeviceIDFetch(deviceIdFetching)
    }

    override fun sendEvent(eventName: String, payload: JSONObject) {
        val event = when (val standardEvent = StandardEvents.names[eventName]) {
            null -> BranchEvent(eventName) // custom event
            else -> BranchEvent(standardEvent)
        }

        val settings = payload.optJSONObject(EventKey.EVENT)
        settings?.let {
            event.addEventProperties(it)
        }

        val buo = BranchUniversalObject()
        val buoSettings = payload.optJSONObject(EventKey.BUO_PROPERTIES)
        buoSettings?.let {
            buo.addBuoProperties(it)
        }

        val metadata = payload.optJSONObject(EventKey.METADATA_PROPERTIES)
        metadata?.let {
            val contentMetadata = ContentMetadata()
            contentMetadata.addMetadataProperties(it)
            buo.contentMetadata = contentMetadata
        }

        event.addContentItems(buo)
        event.logEvent(application)
    }

    override fun setIdentity(id: String) {
        getBranch().setIdentity(id)
    }

    override fun setOptOut(opt: Boolean) {
        getBranch().disableTracking(opt)
    }

    override fun createDeepLink(properties: JSONObject) {
        val linkProperties = LinkProperties()
        linkProperties.addParameters(properties)

        val buo = BranchUniversalObject()
        val buoSettings = properties.optJSONObject(EventKey.BUO_PROPERTIES)
        buoSettings?.let {
            buo.addBuoProperties(it)
        }

        val metadata = properties.optJSONObject(EventKey.METADATA_PROPERTIES)
        metadata?.let {
            val contentMetadata = ContentMetadata()
            contentMetadata.addMetadataProperties(it)
            buo.contentMetadata = contentMetadata
        }

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
                remoteCommandContext?.track(EventKey.BRANCH_CREATE_DEEPLINK, mapOf("branch_short_url" to url))
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
        remoteCommandContext?.track(EventKey.BRANCH_REFERRING_PARAMS, referringParams?.toMap())
    }

    private fun BranchEvent.addEventProperties(eventProperties: JSONObject) {
        val keys = eventProperties.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val propertyValue = eventProperties.opt(key)

            propertyValue?.let { property ->
                val value = property.toString()
                when (key) {
                    BranchEventProperties.AFFILIATION -> setAffiliation(value)
                    BranchEventProperties.COUPON -> setCoupon(value)
                    BranchEventProperties.CURRENCY -> setCurrency(CurrencyType.getValue(value))
                    BranchEventProperties.TAX -> setTax(value.toDouble())
                    BranchEventProperties.REVENUE -> setRevenue(value.toDouble())
                    BranchEventProperties.DESCRIPTION -> setDescription(value)
                    BranchEventProperties.SEARCH_QUERY -> setSearchQuery(value)
                    else -> {
                        addCustomDataProperty(key, value)
                    }
                }
            }
        }
    }

    private fun BranchUniversalObject.addBuoProperties(buoProperties: JSONObject) {
        val keys = buoProperties.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val propertyValue = buoProperties.opt(key)

            propertyValue?.let { property ->
                val value = property.toString()
                when (key) {
                    BuoProperties.CANONICAL_IDENTIFIER -> canonicalIdentifier = value
                    BuoProperties.CANONICAL_URL -> canonicalUrl = value
                    BuoProperties.TITLE -> title = value
                    BuoProperties.DESCRIPTION -> setContentDescription(value)
                    BuoProperties.IMAGE_URL -> setContentImageUrl(value)
                    else -> {
                        // do nothing
                    }
                }
            }
        }
    }

    private fun ContentMetadata.addMetadataProperties(metadata: JSONObject) {
        val keys = metadata.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val propertyValue = metadata.opt(key)

            propertyValue?.let { property ->
                val value = property.toString()
                when (key) {
                    MetadataProperties.QUANTITY -> setQuantity(value.toDouble())
                    MetadataProperties.PRICE -> price = value.toDouble()
                    MetadataProperties.CURRENCY_TYPE -> {
                        val currency = CurrencyType.getValue(value)
                        if (currency != null) {
                            currencyType = currency
                        } else {
                            addCustomMetadata("currencyType", value)
                        }
                    }
                    MetadataProperties.SKU -> sku = value
                    MetadataProperties.PRODUCT_NAME -> productName = value
                    MetadataProperties.PRODUCT_BRAND -> productBrand = value
                    MetadataProperties.PRODUCT_CATEGORY -> {
                        val category = ProductCategory.getValue(value)
                        if (category != null) {
                            productCategory = category
                        } else {
                            addCustomMetadata("productCategory", value)
                        }
                    }
                    MetadataProperties.CONDITION -> {
                        val conditionValue = ContentMetadata.CONDITION.getValue(value)
                        if (conditionValue != null) {
                            condition = conditionValue
                        } else {
                            addCustomMetadata("condition", value)
                        }
                    }
                    MetadataProperties.PRODUCT_VARIANT -> productVariant = value
                    MetadataProperties.RATING -> rating = value.toDouble()
                    MetadataProperties.RATING_AVERAGE -> ratingAverage = value.toDouble()
                    MetadataProperties.RATING_COUNT -> ratingCount = value.toInt()
                    MetadataProperties.RATING_MAX -> ratingMax = value.toDouble()
                    MetadataProperties.ADDRESS_STREET -> addressStreet = value
                    MetadataProperties.ADDRESS_CITY -> addressCity = value
                    MetadataProperties.ADDRESS_REGION -> addressRegion = value
                    MetadataProperties.ADDRESS_COUNTRY -> addressCountry = value
                    MetadataProperties.ADDRESS_POSTAL_CODE -> addressPostalCode = value
                    MetadataProperties.LATITUDE -> latitude = value.toDouble()
                    MetadataProperties.LONGITUDE -> longitude = value.toDouble()
                    MetadataProperties.IMAGE_CAPTIONS -> addImageCaptions(value)
                    else -> {
                        addCustomMetadata(key, value)
                    }
                }
            }
        }
    }

    private fun LinkProperties.addParameters(params: JSONObject) {
        val keys = params.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val propertyValue = params.opt(key)

            propertyValue?.let { property ->
                val value = property.toString()
                when (key) {
                    DeepLinkProperties.CHANNEL -> channel = value
                    DeepLinkProperties.FEATURE -> feature = value
                    DeepLinkProperties.CAMPAIGN -> campaign = value
                    DeepLinkProperties.STAGE -> stage = value
                    DeepLinkProperties.DURATION -> setDuration(value.toInt())
                    DeepLinkProperties.CONTROL_PARAMETERS -> {
                        JSONObject(value).toMap().forEach { (k, v) ->
                            addControlParameter(k, v as String?)
                        }
                    }
                    else -> {
                        // do nothing
                    }
                }
            }
        }
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