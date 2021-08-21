package com.tealium.remotecommands.branch

import android.app.Application

import io.branch.indexing.BranchUniversalObject
import io.branch.referral.Branch
import io.branch.referral.util.*
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21, 28])
class BranchInstanceTests {
    @RelaxedMockK
    lateinit var mockApplication: Application

    @RelaxedMockK
    lateinit var mockBranch: Branch

    lateinit var branchInstance: BranchInstance

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Branch::class)
        every { Branch.getInstance() } returns mockBranch

        branchInstance = BranchInstance(mockApplication, "testKey")
    }

    @Test
    fun sendStandardEventWithEventData() {
        val payload = JSONObject()
        val eventData = JSONObject()
        eventData.put("affiliation", "testAffiliation")
        eventData.put("coupon", "testCoupon")
        eventData.put("currency", "USD")
        eventData.put("tax", 1.00)
        eventData.put("revenue", 2.00)
        eventData.put("description", "testDescription")
        eventData.put("search_query", "testSearchQuery")
        payload.put("event", eventData)

        val event = BranchEvent(BRANCH_STANDARD_EVENT.START_TRIAL)
        event.setAffiliation("testAffiliation")
        event.setCoupon("testCoupon")
        event.setCurrency(CurrencyType.getValue("USD"))
        event.setTax(1.00)
        event.setRevenue(2.00)
        event.setDescription("testDescription")
        event.setSearchQuery("testSearchQuery")

        mockkConstructor(BranchEvent::class)
        every { anyConstructed<BranchEvent>() } returns event
        every { anyConstructed<BranchEvent>().setAffiliation("testAffiliation") } returns event
        every { anyConstructed<BranchEvent>().setCoupon("testCoupon") } returns event
        every { anyConstructed<BranchEvent>().setCurrency(CurrencyType.getValue("USD")) } returns event
        every { anyConstructed<BranchEvent>().setTax(1.00) } returns event
        every { anyConstructed<BranchEvent>().setRevenue(2.00) } returns event
        every { anyConstructed<BranchEvent>().setDescription("testDescription") } returns event
        every { anyConstructed<BranchEvent>().setSearchQuery("testSearchQuery") } returns event

        branchInstance.sendEvent("starttrial", payload)

        verify {
            BranchEvent(BRANCH_STANDARD_EVENT.START_TRIAL)
            event.setAffiliation("testAffiliation")
            event.setCoupon("testCoupon")
            event.setCurrency(CurrencyType.getValue("USD"))
            event.setTax(1.00)
            event.setRevenue(2.00)
            event.setDescription("testDescription")
            event.setSearchQuery("testSearchQuery")
        }
    }

    @Test
    fun sendStandardEventWithBuoData() {
        val payload = JSONObject()
        val buoData = JSONObject()
        buoData.put("canonical_identifier", "testIdentifier")
        buoData.put("canonical_url", "testUrl")
        buoData.put("title", "testTitle")
        buoData.put("description", "testDescription")
        buoData.put("image_url", "testImageUrl")
        payload.put("buo", buoData)

        val buo = spyk<BranchUniversalObject>()
        buo.canonicalIdentifier = "testIdentifier"
        buo.canonicalUrl = "testUrl"
        buo.title = "testTitle"
        buo.setContentDescription("testDescription")
        buo.setContentImageUrl("testImageUrl")

        mockkConstructor(BranchUniversalObject::class)
        every { anyConstructed<BranchUniversalObject>().setCanonicalIdentifier("testIdentifier") } returns buo
        every { anyConstructed<BranchUniversalObject>().setCanonicalUrl("testUrl") } returns buo
        every { anyConstructed<BranchUniversalObject>().setTitle("testTitle") } returns buo
        every { anyConstructed<BranchUniversalObject>().setContentDescription(any()) } returns buo
        every { anyConstructed<BranchUniversalObject>().setContentImageUrl(any()) } returns buo

        branchInstance.sendEvent("addtocart", payload)

        verify {
            BranchEvent(BRANCH_STANDARD_EVENT.ADD_TO_CART)
            buo.canonicalIdentifier = "testIdentifier"
            buo.canonicalUrl = "testUrl"
            buo.title = "testTitle"
            buo.setContentDescription("testDescription")
            buo.setContentImageUrl("testImageUrl")
        }
    }

    @Test
    fun sendStandardEventWithMetadata() {
        val payload = JSONObject()
        val metadataObj = JSONObject()
        metadataObj.put("quantity", 1.00)
        metadataObj.put("sku", "testSku")
        metadataObj.put("product_name", "testProductName")
        metadataObj.put("product_brand", "testProductBrand")
        metadataObj.put("product_category", "software")
        metadataObj.put("condition", "good")
        metadataObj.put("product_variant", "testProductVariant")
        metadataObj.put("rating", 3.00)
        metadataObj.put("rating_average", 4.00)
        metadataObj.put("rating_count", 5)
        metadataObj.put("rating_max", 6.00)
        metadataObj.put("address_street", "testAddressStreet")
        metadataObj.put("address_city", "testAddressCity")
        metadataObj.put("address_region", "testAddressRegion")
        metadataObj.put("address_country", "testAddressCountry")
        metadataObj.put("address_postal_code", "testAddressPostalCode")
        metadataObj.put("latitude", 7.00)
        metadataObj.put("longitude", 8.00)
        metadataObj.put("image_captions", "testImageCaptions")
        payload.put("metadata", metadataObj)

        val contentMetadata = spyk<ContentMetadata>()
        contentMetadata.setQuantity(1.00)
        contentMetadata.setSku("testSku")
        contentMetadata.setProductName("testProductName")
        contentMetadata.setProductBrand("testProductBrand")
        contentMetadata.setProductCategory(ProductCategory.SOFTWARE)
        contentMetadata.setProductCondition(ContentMetadata.CONDITION.GOOD)
        contentMetadata.setProductVariant("testProductVariant")

        mockkConstructor(ContentMetadata::class)
        every { anyConstructed<ContentMetadata>().setQuantity(1.00) } returns contentMetadata
        every { anyConstructed<ContentMetadata>().setSku("testSku") } returns contentMetadata
        every { anyConstructed<ContentMetadata>().setProductName("testProductName") } returns contentMetadata
        every { anyConstructed<ContentMetadata>().setProductBrand("testProductBrand") } returns contentMetadata
        every { anyConstructed<ContentMetadata>().setProductCategory(ProductCategory.getValue("software")) } returns contentMetadata
        every { anyConstructed<ContentMetadata>().setProductCondition(ContentMetadata.CONDITION.GOOD) } returns contentMetadata
        every { anyConstructed<ContentMetadata>().setProductVariant("testProductVariant") } returns contentMetadata

        branchInstance.sendEvent("addtocart", payload)

        verify {
            BranchEvent(BRANCH_STANDARD_EVENT.ADD_TO_CART)
            contentMetadata.setQuantity(1.00)
            contentMetadata.setSku("testSku")
            contentMetadata.setProductName("testProductName")
            contentMetadata.setProductBrand("testProductBrand")
            contentMetadata.setProductCategory(ProductCategory.SOFTWARE)
            contentMetadata.setProductCondition(ContentMetadata.CONDITION.GOOD)
            contentMetadata.setProductVariant("testProductVariant")
        }
    }
}