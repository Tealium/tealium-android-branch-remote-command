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

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(Branch::class)
        every { Branch.getInstance() } returns mockBranch
        every { Branch.getAutoInstance(any()) } returns mockBranch
        every { Branch.getAutoInstance(any(), any()) } returns mockBranch

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
        
        val event = BranchEventBuilder.buildEvent("addtocart", payload)
        val eventSpy = spyk(event)
        every { eventSpy.logEvent(any()) } returns true
        
        branchInstance.sendEvent(eventSpy)

        verify {
            eventSpy.logEvent(mockApplication)
        }
    }

    @Test
    fun standardEventWithMetadataObject() {
        // Given
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

        val event = BranchEventBuilder.buildEvent("addtocart", payload)
        val eventSpy = spyk(event)
        every { eventSpy.logEvent(any()) } returns true
        
        branchInstance.sendEvent(eventSpy)

        verify {
            eventSpy.logEvent(mockApplication)
        }
    }

    @Test
    fun testSetIdentity() {
        // Given
        val userId = "testUser123"

        // When
        branchInstance.setIdentity(userId)

        // Then - verify Branch.setIdentity was called
        verify {
            mockBranch.setIdentity(userId)
        }
    }

    @Test
    fun testSetOptOut() {
        // Given
        val optOut = true

        // When
        branchInstance.setOptOut(optOut)

        // Then - verify Branch.disableTracking was called
        verify {
            mockBranch.disableTracking(optOut)
        }
    }

    @Test
    fun testLogout() {
        // When
        branchInstance.logout()

        // Then - verify Branch.logout was called
        verify {
            mockBranch.logout()
        }
    }

    @Test
    fun testOnInitFinished() {
        // Given
        val referringParams = JSONObject()
        referringParams.put("deep_link_test", "test_value")
        referringParams.put("campaign", "test_campaign")

        // When
        branchInstance.onInitFinished(referringParams, null)

        // Then - verify that referring params were tracked
        verify {
            mockRemoteCommandContext.track(EventKey.BRANCH_REFERRING_PARAMS, any<Map<String, Any?>>())
        }
    }

    @Test
    fun testCreateDeepLink() {
        // Given
        val buo = BranchUniversalObject()
        buo.canonicalIdentifier = "testIdentifier"
        
        val linkProperties = LinkProperties()
        linkProperties.channel = "testChannel"
        linkProperties.feature = "testFeature"

        // Mock the BUO generateShortUrl method
        val buoSpy = spyk(buo)
        every { buoSpy.generateShortUrl(any(), any(), any()) } returns Unit

        // When
        branchInstance.createDeepLink(buoSpy, linkProperties)

        // Then - verify generateShortUrl was called
        verify {
            buoSpy.generateShortUrl(
                mockApplication.applicationContext,
                linkProperties,
                any()
            )
        }
    }
}