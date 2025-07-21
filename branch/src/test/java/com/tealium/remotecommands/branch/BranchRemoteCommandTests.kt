package com.tealium.remotecommands.branch

import android.app.Application
import com.tealium.remotecommands.RemoteCommand
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.BRANCH_STANDARD_EVENT
import io.branch.referral.util.BranchEvent
import io.branch.referral.util.CurrencyType
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [21, 28])
class BranchRemoteCommandTests {
    private val COMMAND_NAME_KEY = "command_name"

    @RelaxedMockK
    lateinit var mockApplication: Application

    @RelaxedMockK
    lateinit var mockBranchInstance: BranchCommand

    lateinit var branchRemoteCommand: BranchRemoteCommand

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)

        every { mockApplication.assets } returns mockk()

        branchRemoteCommand =
            BranchRemoteCommand(mockApplication, "testKey")
        branchRemoteCommand.branchInstance = mockBranchInstance
    }

    @Test
    fun validSplitCommands() {
        val json = JSONObject()
        json.put(COMMAND_NAME_KEY, "initialize, log_purchase, add_to_cart")
        val commands = branchRemoteCommand.splitCommands(json)

        assertEquals(3, commands.count())
        assertEquals("initialize", commands[0])
        assertEquals("log_purchase", commands[1])
        assertEquals("add_to_cart", commands[2])
    }

    @Test
    fun onInvokeValidInitialize() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, Commands.INITIALIZE)

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify {
            mockBranchInstance.initialize(any(), any(), any())
        }
    }

    @Test
    fun onInvokeValidIdentity() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, Commands.SET_USER_ID)
        payload.put("user_id", "anyId123")

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify {
            mockBranchInstance.setIdentity(any())
        }
    }

    @Test
    fun onInvokeInvalidIdentityEmptyString() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, Commands.SET_USER_ID)

        payload.put("user_id", "")

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify(exactly = 0) {
            mockBranchInstance.setIdentity(any())
        }
    }

    @Test
    fun onInvokeInvalidIdentityMissing() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, Commands.SET_USER_ID)

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify(exactly = 0) {
            mockBranchInstance.setIdentity(any())
        }
    }

    @Test
    fun onInvokeValidCreateDeepLink() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, Commands.CREATE_DEEP_LINK)
        
        val link = JSONObject()
        link.put("channel", "testChannel")
        link.put("feature", "testFeature")
        link.put("campaign", "testCampaign")
        
        val buoData = JSONObject()
        buoData.put("canonical_identifier", "testId")
        buoData.put("title", "Test Title")
        link.put("buo", buoData)
        
        payload.put("link", link)
        
        val metadata = JSONObject()
        metadata.put("sku", "testSku")
        payload.put("metadata", metadata)

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify {
            mockBranchInstance.createDeepLink(any(), any())
        }
    }

    @Test
    fun onInvokeInvalidCreateDeepLink() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, Commands.CREATE_DEEP_LINK)

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify(exactly = 0) {
            mockBranchInstance.createDeepLink(any(), any())
        }
    }

    @Test
    fun onInvokeValidLogout() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, Commands.LOGOUT)

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify {
            mockBranchInstance.logout()
        }
    }

    @Test
    fun onInvokeValidSendEvent() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, "test_event")
        
        val eventData = JSONObject()
        eventData.put("affiliation", "testAffiliation")
        eventData.put("revenue", 10.0)
        payload.put("event", eventData)

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify {
            mockBranchInstance.sendEvent(any())
        }
    }

    @Test
    fun onInvokeValidSendStandardEventWithoutData() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, "addtocart")

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify {
            mockBranchInstance.sendEvent(any())
        }
    }

    @Test
    fun onInvokeValidSendStandardEventWithFullData() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, "addtocart")
        
        // Event properties
        val eventData = JSONObject()
        eventData.put("affiliation", "testAffiliation")
        eventData.put("revenue", 15.99)
        eventData.put("currency", "USD")
        payload.put("event", eventData)
        
        // BUO properties
        val buoData = JSONObject()
        buoData.put("canonical_identifier", "product123")
        buoData.put("title", "Test Product")
        payload.put("buo", buoData)
        
        // Metadata
        val metadata = JSONObject()
        metadata.put("sku", "SKU123")
        metadata.put("product_name", "Test Product Name")
        payload.put("metadata", metadata)

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify {
            mockBranchInstance.sendEvent(any())
        }
    }

    @Test
    fun onInvokeValidSendCustomEventWithoutData() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, "test_event")

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify {
            mockBranchInstance.sendEvent(any())
        }
    }

    @Test
    fun onInvokeValidSendCustomWithEvent() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()
        payload.put(COMMAND_NAME_KEY, "test_event")

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify {
            mockBranchInstance.sendEvent(any())
        }
    }
}