package com.tealium.example

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tealium.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        val view = activityMainBinding.root
        setContentView(view)

        activityMainBinding.trackCardAdd.setOnClickListener {
            trackAddToCartEvent()
        }

        activityMainBinding.trackPurchase.setOnClickListener {
            trackPurchaseEvent()
        }

        activityMainBinding.trackSearch.setOnClickListener {
            trackSearchEvent()
        }

        activityMainBinding.trackCustom.setOnClickListener {
            trackCustomEvent()
        }

        activityMainBinding.createLink.setOnClickListener {
            createDeepLink()
        }

        activityMainBinding.updateUserId.setOnClickListener {
            updateIdentity()
        }
    }

    private fun trackAddToCartEvent() {
        TealiumHelper.trackEvent(
            "cart_add", mapOf(
                "product_name" to "Tealium Subscription",
                "product_id" to "sku123",
            )
        )
    }

    private fun trackPurchaseEvent() {
        TealiumHelper.trackEvent(
            "order", mapOf(
                "product_name" to "Tealium Beast",
                "product_id" to "sku123",
                "product_category" to "toys",
                "currency_type" to "USD",
                "product_unit_price" to 1.99,
            )
        )
    }

    private fun trackSearchEvent() {
        TealiumHelper.trackEvent(
            "search", mapOf(
                "product_name" to "Tealium Beast",
                "product_id" to "sku123",
                "product_category" to "toys",
                "currency_type" to "USD",
                "product_unit_price" to 1.99,
            )
        )
    }

    private fun trackCustomEvent() {
        TealiumHelper.trackEvent("teal_custom_event", emptyMap())
        TealiumHelper.trackEvent("record_score", emptyMap())
    }

    private fun createDeepLink() {
        TealiumHelper.trackEvent(
            "create_deep_link", mapOf(
                "channel" to "TealiumTV",
                "feature" to "Mobile Highlights",
                "campaign" to "mobile",
            )
        )
    }

    private fun updateIdentity() {
        TealiumHelper.trackEvent("user_login", mapOf("customer_id" to "tealUser123"))
    }
}