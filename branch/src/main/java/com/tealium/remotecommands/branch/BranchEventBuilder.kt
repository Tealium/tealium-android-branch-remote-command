package com.tealium.remotecommands.branch

import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.*
import org.json.JSONArray
import org.json.JSONObject

object BranchEventBuilder {
    
    fun buildEvent(eventName: String, payload: JSONObject): BranchEvent {
        val event = when (val standardEvent = StandardEvents.names[eventName]) {
            null -> BranchEvent(eventName)
            else -> BranchEvent(standardEvent)
        }
        
        // Event properties
        payload.optJSONObject(EventKey.EVENT)?.let { props ->
            applyEventProperties(event, props)
        }
        
        // BUO
        payload.optJSONObject(EventKey.BUO_PROPERTIES)?.let { buoProps ->
            val buo = buildBranchUniversalObject(buoProps, payload)
            event.addContentItems(buo)
        }
        
        return event
    }
    
    fun buildBranchUniversalObject(buoProps: JSONObject, fullPayload: JSONObject): BranchUniversalObject {
        val buo = BranchUniversalObject()
        
        // Basic properties
        buoProps.optString(BuoProperties.CANONICAL_IDENTIFIER).takeIf { it.isNotEmpty() }?.let { 
            buo.canonicalIdentifier = it 
        }
        buoProps.optString(BuoProperties.CANONICAL_URL).takeIf { it.isNotEmpty() }?.let { 
            buo.canonicalUrl = it 
        }
        buoProps.optString(BuoProperties.TITLE).takeIf { it.isNotEmpty() }?.let { 
            buo.title = it 
        }
        buoProps.optString(BuoProperties.DESCRIPTION).takeIf { it.isNotEmpty() }?.let { 
            buo.setContentDescription(it) 
        }
        buoProps.optString(BuoProperties.IMAGE_URL).takeIf { it.isNotEmpty() }?.let { 
            buo.setContentImageUrl(it) 
        }
        
        // Metadata
        fullPayload.optJSONObject(EventKey.METADATA_PROPERTIES)?.let { metadata ->
            buo.contentMetadata = buildContentMetadata(metadata)
        }
        
        return buo
    }
    
    fun buildLinkProperties(properties: JSONObject): LinkProperties {
        val linkProps = LinkProperties()
        
        properties.optString(DeepLinkProperties.CHANNEL).takeIf { it.isNotEmpty() }?.let { 
            linkProps.channel = it 
        }
        properties.optString(DeepLinkProperties.FEATURE).takeIf { it.isNotEmpty() }?.let { 
            linkProps.feature = it 
        }
        properties.optString(DeepLinkProperties.CAMPAIGN).takeIf { it.isNotEmpty() }?.let { 
            linkProps.campaign = it 
        }
        properties.optString(DeepLinkProperties.STAGE).takeIf { it.isNotEmpty() }?.let { 
            linkProps.stage = it 
        }
        properties.optInt(DeepLinkProperties.DURATION).takeIf { it > 0 }?.let { 
            linkProps.setDuration(it) 
        }
        
        // Control parameters
        properties.optString(DeepLinkProperties.CONTROL_PARAMETERS).takeIf { it.isNotEmpty() }?.let { controlParams ->
            JSONObject(controlParams).toMap().forEach { (k, v) ->
                linkProps.addControlParameter(k, v as String?)
            }
        }
        
        return linkProps
    }
    
    private fun applyEventProperties(event: BranchEvent, props: JSONObject) {
        props.optString(BranchEventProperties.AFFILIATION).takeIf { it.isNotEmpty() }?.let { 
            event.setAffiliation(it) 
        }
        props.optString(BranchEventProperties.COUPON).takeIf { it.isNotEmpty() }?.let { 
            event.setCoupon(it) 
        }
        props.optString(BranchEventProperties.CURRENCY).takeIf { it.isNotEmpty() }?.let { 
            CurrencyType.getValue(it)?.let { currency -> event.setCurrency(currency) }
        }
        props.optDouble(BranchEventProperties.TAX).takeIf { it > 0 }?.let { 
            event.setTax(it) 
        }
        props.optDouble(BranchEventProperties.REVENUE).takeIf { it > 0 }?.let { 
            event.setRevenue(it) 
        }
        props.optString(BranchEventProperties.DESCRIPTION).takeIf { it.isNotEmpty() }?.let { 
            event.setDescription(it) 
        }
        props.optString(BranchEventProperties.SEARCH_QUERY).takeIf { it.isNotEmpty() }?.let { 
            event.setSearchQuery(it) 
        }
        
        // Custom properties
        val standardKeys = setOf(
            BranchEventProperties.AFFILIATION,
            BranchEventProperties.COUPON,
            BranchEventProperties.CURRENCY,
            BranchEventProperties.TAX,
            BranchEventProperties.REVENUE, 
            BranchEventProperties.DESCRIPTION,
            BranchEventProperties.SEARCH_QUERY
        )
        
        props.keys().asSequence()
            .filter { it !in standardKeys }
            .forEach { key ->
                props.optString(key).takeIf { it.isNotEmpty() }?.let { value ->
                    event.addCustomDataProperty(key, value)
                }
            }
    }
    
    private fun buildContentMetadata(metadata: JSONObject): ContentMetadata {
        val contentMetadata = ContentMetadata()
        
        metadata.optDouble(MetadataProperties.QUANTITY).takeIf { it > 0 }?.let { 
            contentMetadata.setQuantity(it) 
        }
        metadata.optDouble(MetadataProperties.PRICE).takeIf { it > 0 }?.let { 
            contentMetadata.price = it 
        }
        metadata.optString(MetadataProperties.CURRENCY_TYPE).takeIf { it.isNotEmpty() }?.let { 
            CurrencyType.getValue(it)?.let { currency -> 
                contentMetadata.currencyType = currency 
            } ?: contentMetadata.addCustomMetadata("currencyType", it)
        }
        metadata.optString(MetadataProperties.SKU).takeIf { it.isNotEmpty() }?.let { 
            contentMetadata.sku = it 
        }
        metadata.optString(MetadataProperties.PRODUCT_NAME).takeIf { it.isNotEmpty() }?.let { 
            contentMetadata.productName = it 
        }
        metadata.optString(MetadataProperties.PRODUCT_BRAND).takeIf { it.isNotEmpty() }?.let { 
            contentMetadata.productBrand = it 
        }
        metadata.optString(MetadataProperties.PRODUCT_CATEGORY).takeIf { it.isNotEmpty() }?.let { 
            ProductCategory.getValue(it)?.let { category -> 
                contentMetadata.productCategory = category 
            } ?: contentMetadata.addCustomMetadata("productCategory", it)
        }
        metadata.optString(MetadataProperties.CONDITION).takeIf { it.isNotEmpty() }?.let { 
            ContentMetadata.CONDITION.getValue(it)?.let { condition -> 
                contentMetadata.condition = condition 
            } ?: contentMetadata.addCustomMetadata("condition", it)
        }
        metadata.optString(MetadataProperties.PRODUCT_VARIANT).takeIf { it.isNotEmpty() }?.let { 
            contentMetadata.productVariant = it 
        }
        metadata.optDouble(MetadataProperties.RATING).takeIf { it > 0 }?.let { 
            contentMetadata.rating = it 
        }
        metadata.optDouble(MetadataProperties.RATING_AVERAGE).takeIf { it > 0 }?.let { 
            contentMetadata.ratingAverage = it 
        }
        metadata.optInt(MetadataProperties.RATING_COUNT).takeIf { it > 0 }?.let { 
            contentMetadata.ratingCount = it 
        }
        metadata.optDouble(MetadataProperties.RATING_MAX).takeIf { it > 0 }?.let { 
            contentMetadata.ratingMax = it 
        }
        metadata.optString(MetadataProperties.ADDRESS_STREET).takeIf { it.isNotEmpty() }?.let { 
            contentMetadata.addressStreet = it 
        }
        metadata.optString(MetadataProperties.ADDRESS_CITY).takeIf { it.isNotEmpty() }?.let { 
            contentMetadata.addressCity = it 
        }
        metadata.optString(MetadataProperties.ADDRESS_REGION).takeIf { it.isNotEmpty() }?.let { 
            contentMetadata.addressRegion = it 
        }
        metadata.optString(MetadataProperties.ADDRESS_COUNTRY).takeIf { it.isNotEmpty() }?.let { 
            contentMetadata.addressCountry = it 
        }
        metadata.optString(MetadataProperties.ADDRESS_POSTAL_CODE).takeIf { it.isNotEmpty() }?.let { 
            contentMetadata.addressPostalCode = it 
        }
        metadata.optDouble(MetadataProperties.LATITUDE).takeIf { it != 0.0 }?.let { 
            contentMetadata.latitude = it 
        }
        metadata.optDouble(MetadataProperties.LONGITUDE).takeIf { it != 0.0 }?.let { 
            contentMetadata.longitude = it 
        }
        metadata.optString(MetadataProperties.IMAGE_CAPTIONS).takeIf { it.isNotEmpty() }?.let { 
            contentMetadata.addImageCaptions(it) 
        }
        
        // Custom metadata
        val standardKeys = setOf(
            MetadataProperties.QUANTITY, 
            MetadataProperties.PRICE, 
            MetadataProperties.CURRENCY_TYPE,
            MetadataProperties.SKU, 
            MetadataProperties.PRODUCT_NAME, 
            MetadataProperties.PRODUCT_BRAND,
            MetadataProperties.PRODUCT_CATEGORY, 
            MetadataProperties.CONDITION, 
            MetadataProperties.PRODUCT_VARIANT,
            MetadataProperties.RATING, 
            MetadataProperties.RATING_AVERAGE, 
            MetadataProperties.RATING_MAX, 
            MetadataProperties.ADDRESS_STREET, 
            MetadataProperties.ADDRESS_CITY,
            MetadataProperties.ADDRESS_REGION, 
            MetadataProperties.ADDRESS_COUNTRY, 
            MetadataProperties.ADDRESS_POSTAL_CODE,
            MetadataProperties.LATITUDE, 
            MetadataProperties.LONGITUDE, 
            MetadataProperties.IMAGE_CAPTIONS
        )
        
        metadata.keys().asSequence()
            .filter { it !in standardKeys }
            .forEach { key ->
                metadata.optString(key).takeIf { it.isNotEmpty() }?.let { value ->
                    contentMetadata.addCustomMetadata(key, value)
                }
            }
        
        return contentMetadata
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
} 