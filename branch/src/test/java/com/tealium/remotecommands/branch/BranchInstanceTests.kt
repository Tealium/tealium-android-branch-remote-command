package com.tealium.remotecommands.branch

import android.app.Application
import com.tealium.remotecommands.RemoteCommandContext

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
    lateinit var mockRemoteCommandContext: RemoteCommandContext

    @RelaxedMockK
    lateinit var mockBranch: Branch

    lateinit var branchInstance: BranchInstance
    lateinit var mockBuo: BranchUniversalObject
    lateinit var mockContentMetadata: ContentMetadata

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Branch::class)
        every { Branch.getInstance() } returns mockBranch

        mockBuo = spyk()
        mockBuo.canonicalIdentifier = "testIdentifier"
        mockBuo.canonicalUrl = "testUrl"
        mockBuo.title = "testTitle"
        mockBuo.setContentDescription("testDescription")
        mockBuo.setContentImageUrl("testImageUrl")

        mockkConstructor(BranchUniversalObject::class)
        every { anyConstructed<BranchUniversalObject>().setCanonicalIdentifier(any()) } returns mockBuo
        every { anyConstructed<BranchUniversalObject>().setCanonicalUrl(any()) } returns mockBuo
        every { anyConstructed<BranchUniversalObject>().setTitle(any()) } returns mockBuo
        every { anyConstructed<BranchUniversalObject>().setContentDescription(any()) } returns mockBuo
        every { anyConstructed<BranchUniversalObject>().setContentImageUrl(any()) } returns mockBuo


        mockContentMetadata = spyk()
        mockContentMetadata.setQuantity(1.00)
        mockContentMetadata.setSku("testSku")
        mockContentMetadata.setProductName("testProductName")
        mockContentMetadata.setProductBrand("testProductBrand")
        mockContentMetadata.setProductCategory(ProductCategory.SOFTWARE)
        mockContentMetadata.setProductCondition(ContentMetadata.CONDITION.GOOD)
        mockContentMetadata.setProductVariant("testProductVariant")

        mockkConstructor(ContentMetadata::class)
        every { anyConstructed<ContentMetadata>().setQuantity(any()) } returns mockContentMetadata
        every { anyConstructed<ContentMetadata>().setSku(any()) } returns mockContentMetadata
        every { anyConstructed<ContentMetadata>().setProductName(any()) } returns mockContentMetadata
        every { anyConstructed<ContentMetadata>().setProductBrand(any()) } returns mockContentMetadata
        every { anyConstructed<ContentMetadata>().setProductCategory(any()) } returns mockContentMetadata
        every { anyConstructed<ContentMetadata>().setProductCondition(any()) } returns mockContentMetadata
        every { anyConstructed<ContentMetadata>().setProductVariant(any()) } returns mockContentMetadata

        branchInstance = BranchInstance(mockApplication, "testKey", mockRemoteCommandContext)
    }

    @Test
    fun standardEventWithBuoDataObject() {
        val payload = JSONObject()
        val buoData = JSONObject()
        buoData.put("canonical_identifier", "testIdentifier")
        buoData.put("canonical_url", "testUrl")
        buoData.put("title", "testTitle")
        buoData.put("description", "testDescription")
        buoData.put("image_url", "testImageUrl")
        payload.put("buo", buoData)

        branchInstance.sendEvent("addtocart", payload)

        verify {
            mockBuo.canonicalIdentifier = "testIdentifier"
            mockBuo.canonicalUrl = "testUrl"
            mockBuo.title = "testTitle"
            mockBuo.setContentDescription("testDescription")
            mockBuo.setContentImageUrl("testImageUrl")
        }
    }

    @Test
    fun standardEventWithMetadataObject() {
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

        branchInstance.sendEvent("addtocart", payload)

        verify {
            mockContentMetadata.setQuantity(1.00)
            mockContentMetadata.setSku("testSku")
            mockContentMetadata.setProductName("testProductName")
            mockContentMetadata.setProductBrand("testProductBrand")
            mockContentMetadata.setProductCategory(ProductCategory.SOFTWARE)
            mockContentMetadata.setProductCondition(ContentMetadata.CONDITION.GOOD)
            mockContentMetadata.setProductVariant("testProductVariant")
        }
    }
}