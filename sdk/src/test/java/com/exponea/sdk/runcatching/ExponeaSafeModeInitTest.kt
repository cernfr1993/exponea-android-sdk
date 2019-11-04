package com.exponea.sdk.runcatching

import androidx.test.core.app.ApplicationProvider
import com.exponea.sdk.Exponea
import com.exponea.sdk.testutil.ExponeaSDKTest
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.paperdb.Paper
import kotlin.reflect.KFunction
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
internal class ExponeaSafeModeInitTest(
    method: KFunction<Any>,
    val lambda: () -> Any
) : ExponeaSDKTest() {
    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun data(): List<Array<out Any?>> {
            return PublicApiTestCases.initMethods.map { arrayOf(it.first, it.second) }
        }
    }

    @After
    fun after() {
        unmockkStatic(Paper::class)
    }

    @Test
    fun callInitWithoutExceptionWithSafeModeEnabled() {
        Exponea.safeModeEnabled = true
        lambda()
        assertTrue(Exponea.isInitialized)
        waitUntilFlushed()
    }

    @Test
    fun callInitWithExceptionWithSafeModeEnabled() {
        mockkStatic(Paper::class)
        every {
            Paper.init(ApplicationProvider.getApplicationContext())
        } throws ExponeaExceptionThrowing.TestPurposeException()
        Exponea.safeModeEnabled = true
        lambda()
        assertFalse(Exponea.isInitialized)
        waitUntilFlushed()
    }

    @Test(expected = ExponeaExceptionThrowing.TestPurposeException::class)
    fun callInitWithExceptionWithSafeModeDisabled() {
        mockkStatic(Paper::class)
        every {
            Paper.init(ApplicationProvider.getApplicationContext())
        } throws ExponeaExceptionThrowing.TestPurposeException()
        Exponea.safeModeEnabled = false
        assertFalse(Exponea.isInitialized)
        lambda()
        assertFalse(Exponea.isInitialized)
        waitUntilFlushed()
    }
}
