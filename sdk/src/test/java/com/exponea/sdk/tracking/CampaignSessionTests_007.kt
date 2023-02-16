package com.exponea.sdk.tracking

import android.app.Activity
import android.os.Bundle
import androidx.test.platform.app.InstrumentationRegistry
import com.exponea.sdk.Exponea
import com.exponea.sdk.models.Constants
import com.exponea.sdk.repository.ExponeaConfigRepository
import com.exponea.sdk.testutil.componentForTesting
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class CampaignSessionTests_007 : CampaignSessionTests_Base() {

    /**
     * Cold start, Campaign click start, SDK init after onResume
     */
    @Test
    fun testBehavior_007() {
        ExponeaConfigRepository.set(InstrumentationRegistry.getInstrumentation().context, configuration)
        val campaignIntent = createDeeplinkIntent()
        val controller = Robolectric.buildActivity(TestActivity::class.java, campaignIntent)
        controller.create()
        assertFalse(Exponea.isInitialized)

        controller.start()
        assertTrue(Exponea.isInitialized)

        val campaignEvent = Exponea.componentForTesting.campaignRepository.get()
        assertNotNull(campaignEvent)
        assertTrue(Exponea.componentForTesting.eventRepository.all().any { it.type == Constants.EventTypes.push })

        controller.resume()
        Thread.sleep(1000)
        assertNull(Exponea.componentForTesting.campaignRepository.get())
        val sessionEvent = Exponea.componentForTesting.eventRepository.all().find {
            it.type == Constants.EventTypes.sessionStart
        }
        assertNotNull(sessionEvent)
        assertNotNull(sessionEvent.properties)
        assertEquals(campaignEvent.completeUrl, sessionEvent.properties!!["location"])
        assertEquals(campaignEvent.source, sessionEvent.properties!!["utm_source"])
        assertEquals(campaignEvent.campaign, sessionEvent.properties!!["utm_campaign"])
        assertEquals(campaignEvent.content, sessionEvent.properties!!["utm_content"])
        assertEquals(campaignEvent.term, sessionEvent.properties!!["utm_term"])
    }

    /**
     * Used by test testBehavior_007 (Cold start, Campaign click start, SDK init after onResume)
     */
    class TestActivity : Activity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            Exponea.handleCampaignIntent(intent, applicationContext)
        }

        override fun onStart() {
            super.onStart()
            initExponea(applicationContext)
        }
    }
}
