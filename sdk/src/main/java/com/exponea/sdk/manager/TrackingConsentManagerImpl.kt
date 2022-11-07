package com.exponea.sdk.manager

import com.exponea.sdk.manager.TrackingConsentManager.MODE
import com.exponea.sdk.manager.TrackingConsentManager.MODE.CONSIDER_CONSENT
import com.exponea.sdk.models.Constants
import com.exponea.sdk.models.Constants.EventTypes
import com.exponea.sdk.models.EventType
import com.exponea.sdk.models.EventType.PUSH_OPENED
import com.exponea.sdk.models.EventType.TRACK_EVENT
import com.exponea.sdk.models.InAppMessage
import com.exponea.sdk.models.NotificationAction
import com.exponea.sdk.models.NotificationData
import com.exponea.sdk.models.PropertiesList
import com.exponea.sdk.repository.CampaignRepository
import com.exponea.sdk.util.GdprTracking
import com.exponea.sdk.util.Logger
import com.exponea.sdk.util.currentTimeSeconds

internal class TrackingConsentManagerImpl(
    private val eventManager: EventManager,
    private val campaignRepository: CampaignRepository,
    private val inappMessageTrackingDelegate: InAppMessageTrackingDelegate
) : TrackingConsentManager {

    override fun trackClickedPush(
        data: NotificationData?,
        actionData: NotificationAction?,
        timestamp: Double?,
        mode: MODE
    ) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && data?.hasTrackingConsent == false && actionData?.isTrackingForced != true) {
            Logger.e(this,
                "Event for clicked notification is not tracked because consent is not given nor forced")
            trackingAllowed = false
        }
        val properties = PropertiesList(
            hashMapOf(
                "status" to "clicked",
                "platform" to "android",
                "url" to (actionData?.url ?: "app"),
                "cta" to (actionData?.actionName ?: "notification")
            )
        )
        if (data != null) {
            // we'll consider the campaign data as just created - for expiration handling
            data.campaignData.createdAt = currentTimeSeconds()
            campaignRepository.set(data.campaignData)
        }
        if (data?.getTrackingData() != null) {
            for (item in data.getTrackingData()) {
                properties[item.key] = item.value
            }
        }
        if (data?.consentCategoryTracking != null) {
            properties["consent_category_tracking"] = data.consentCategoryTracking
        }
        if (actionData?.isTrackingForced == true) {
            properties["tracking_forced"] = true
        }
        eventManager.processTrack(
            eventType = if (data?.hasCustomEventType == true) data.eventType else EventTypes.push,
            properties = properties.properties,
            type = if (data?.hasCustomEventType == true) TRACK_EVENT else PUSH_OPENED,
            timestamp = timestamp,
            trackingAllowed = trackingAllowed
        )
    }

    override fun trackDeliveredPush(data: NotificationData?, timestamp: Double, mode: MODE) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && data?.hasTrackingConsent == false) {
            Logger.e(this, "Event for delivered notification is not tracked because consent is not given")
            trackingAllowed = false
        }
        val properties = PropertiesList(
            hashMapOf("status" to "delivered", "platform" to "android")
        )
        if (data?.getTrackingData() != null) {
            for (item in data.getTrackingData()) {
                properties[item.key] = item.value
            }
        }
        if (data?.consentCategoryTracking != null) {
            properties["consent_category_tracking"] = data.consentCategoryTracking
        }
        eventManager.processTrack(
            eventType = if (data?.hasCustomEventType == true) data.eventType else Constants.EventTypes.push,
            properties = properties.properties,
            type = if (data?.hasCustomEventType == true) EventType.TRACK_EVENT else EventType.PUSH_DELIVERED,
            timestamp = timestamp,
            trackingAllowed = trackingAllowed
        )
    }

    override fun trackInAppMessageShown(message: InAppMessage, mode: MODE) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !message.hasTrackingConsent) {
            Logger.e(this, "Event for shown inAppMessage is not tracked because consent is not given")
            trackingAllowed = false
        }
        inappMessageTrackingDelegate.track(message, "show", false, trackingAllowed)
    }

    override fun trackInAppMessageClick(message: InAppMessage, buttonText: String?, buttonLink: String?, mode: MODE) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !message.hasTrackingConsent && !GdprTracking.isTrackForced(buttonLink)) {
            Logger.e(this, "Event for clicked inAppMessage is not tracked because consent is not given")
            trackingAllowed = false
        }
        inappMessageTrackingDelegate.track(
            message,
            "click",
            true,
            trackingAllowed,
            buttonText,
            buttonLink
        )
    }

    override fun trackInAppMessageClose(message: InAppMessage, mode: MODE) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !message.hasTrackingConsent) {
            Logger.e(this, "Event for closed inAppMessage is not tracked because consent is not given")
            trackingAllowed = false
        }
        inappMessageTrackingDelegate.track(message, "close", false, trackingAllowed)
    }

    override fun trackInAppMessageError(message: InAppMessage, error: String, mode: MODE) {
        var trackingAllowed = true
        if (mode == CONSIDER_CONSENT && !message.hasTrackingConsent) {
            Logger.e(this, "Event for error of inAppMessage showing is not tracked because consent is not given")
            trackingAllowed = false
        }
        inappMessageTrackingDelegate.track(message, "error", false, trackingAllowed, error = error)
    }
}