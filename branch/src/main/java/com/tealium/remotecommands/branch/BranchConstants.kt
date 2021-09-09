package com.tealium.remotecommands.branch

import io.branch.referral.util.BRANCH_STANDARD_EVENT

object EventKey {
    const val SEPARATOR = ","
    const val COMMAND_KEY = "command_name"

    const val USER_ID = "user_id"
    const val EVENT = "event"
    const val BUO_PROPERTIES = "buo"
    const val METADATA_PROPERTIES = "metadata"
    const val LINK_PROPERTIES = "link"

    const val BRANCH_CREATE_DEEPLINK = "branch_create_deeplink"
    const val BRANCH_REFERRING_PARAMS = "branch_referring_params"
}

object Config {
    const val SETTINGS = "settings"
    const val DEV_KEY = "branch_dev_key"
    const val ENABLE_LOGGING = "enable_logging"
    const val COLLECT_DEVICE_ID = "collect_device_id"
}

object Commands {
    const val INITIALIZE = "initialize"
    const val SET_USER_ID = "setuserid"
    const val LOGOUT = "logout"
    const val CREATE_DEEP_LINK = "createdeeplink"
}

object StandardEvents {
    val names = mapOf(
        "achievelevel" to BRANCH_STANDARD_EVENT.ACHIEVE_LEVEL,
        "addpaymentinfo" to BRANCH_STANDARD_EVENT.ADD_PAYMENT_INFO,
        "addtocart" to BRANCH_STANDARD_EVENT.ADD_TO_CART,
        "addtowishlist" to BRANCH_STANDARD_EVENT.ADD_TO_WISHLIST,
        "clickad" to BRANCH_STANDARD_EVENT.CLICK_AD,
        "completetutorial" to BRANCH_STANDARD_EVENT.COMPLETE_TUTORIAL,
        "completeregistration" to BRANCH_STANDARD_EVENT.COMPLETE_REGISTRATION,
        "completestream" to BRANCH_STANDARD_EVENT.COMPLETE_STREAM,
        "initiatepurchase" to BRANCH_STANDARD_EVENT.INITIATE_PURCHASE,
        "initiatestream" to BRANCH_STANDARD_EVENT.INITIATE_STREAM,
        "invite" to BRANCH_STANDARD_EVENT.INVITE,
        "login" to BRANCH_STANDARD_EVENT.LOGIN,
        "purchase" to BRANCH_STANDARD_EVENT.PURCHASE,
        "rate" to BRANCH_STANDARD_EVENT.RATE,
        "reserve" to BRANCH_STANDARD_EVENT.RESERVE,
        "search" to BRANCH_STANDARD_EVENT.SEARCH,
        "share" to BRANCH_STANDARD_EVENT.SHARE,
        "spendcredits" to BRANCH_STANDARD_EVENT.SPEND_CREDITS,
        "starttrial" to BRANCH_STANDARD_EVENT.START_TRIAL,
        "subscribe" to BRANCH_STANDARD_EVENT.SUBSCRIBE,
        "unlockachievement" to BRANCH_STANDARD_EVENT.UNLOCK_ACHIEVEMENT,
        "viewad" to BRANCH_STANDARD_EVENT.VIEW_AD,
        "viewcart" to BRANCH_STANDARD_EVENT.VIEW_CART,
        "viewitem" to BRANCH_STANDARD_EVENT.VIEW_ITEM,
        "viewitems" to BRANCH_STANDARD_EVENT.VIEW_ITEMS,
    )
}

object BuoProperties {
    const val CANONICAL_IDENTIFIER = "canonical_identifier"
    const val CANONICAL_URL = "canonical_url"
    const val TITLE = "title"
    const val DESCRIPTION = "description"
    const val IMAGE_URL = "image_url"
    const val CONTENT_METADATA = "content_metadata"
}

object DeepLinkProperties {
    const val CHANNEL = "channel"
    const val FEATURE = "feature"
    const val CAMPAIGN = "campaign"
    const val STAGE = "stage"
    const val DURATION = "duration"
    const val CONTROL_PARAMETERS = "control_parameters"
}

object BranchEventProperties {
    const val AFFILIATION = "affiliation"
    const val COUPON = "coupon"
    const val CURRENCY = "currency"
    const val TAX = "tax"
    const val REVENUE = "revenue"
    const val DESCRIPTION = "description"
    const val SEARCH_QUERY = "search_query"
}

object MetadataProperties {
    const val QUANTITY = "quantity"
    const val PRICE = "price"
    const val CURRENCY_TYPE = "currency_type"
    const val SKU = "sku"
    const val PRODUCT_NAME = "product_name"
    const val PRODUCT_BRAND = "product_brand"
    const val PRODUCT_CATEGORY = "product_category"
    const val CONDITION = "condition"
    const val PRODUCT_VARIANT = "product_variant"
    const val RATING = "rating"
    const val RATING_AVERAGE = "rating_average"
    const val RATING_COUNT = "rating_count"
    const val RATING_MAX = "rating_max"
    const val ADDRESS_STREET = "address_street"
    const val ADDRESS_CITY = "address_city"
    const val ADDRESS_REGION = "address_region"
    const val ADDRESS_COUNTRY = "address_country"
    const val ADDRESS_POSTAL_CODE = "address_postal_code"
    const val LATITUDE = "latitude"
    const val LONGITUDE = "longitude"
    const val IMAGE_CAPTIONS = "image_captions"
}