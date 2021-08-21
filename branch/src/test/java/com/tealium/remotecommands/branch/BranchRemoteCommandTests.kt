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
            BranchRemoteCommand(mockApplication, "testKey", branchInstance = mockBranchInstance)
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
            mockBranchInstance.initialize(any())
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
    fun onInvokeInvalidIdentity() {
        val mockResponse = mockk<RemoteCommand.Response>()
        val payload = JSONObject()

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
        link.put("testLinkProperties", "value1")
        payload.put("link", link)

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify {
            mockBranchInstance.createDeepLink(any())
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
            mockBranchInstance.createDeepLink(any())
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

        every { mockResponse.requestPayload } returns payload

        branchRemoteCommand.onInvoke(mockResponse)

        verify {
            mockBranchInstance.sendEvent("test_event", payload)
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
            mockBranchInstance.sendEvent("addtocart", payload)
            BranchEvent(BRANCH_STANDARD_EVENT.ADD_TO_CART)
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
            mockBranchInstance.sendEvent("test_event", payload)
            BranchEvent("test_event")
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
            mockBranchInstance.sendEvent("test_event", payload)
            BranchEvent("test_event")
        }
    }
}