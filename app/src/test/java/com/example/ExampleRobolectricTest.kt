package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.AppLanguage
import com.example.data.repository.DailyRepository
import com.example.ui.util.AppStrings
import com.example.ui.util.DailyQuotes
import com.example.ui.util.DateUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context matches app name`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("My Daily", appName)
    }

    @Test
    fun `pin hashing produces secure non-plaintext hash`() {
        val repository = DailyRepository(ApplicationProvider.getApplicationContext())
        val pin = "1234"
        val hash = repository.hashPin(pin)
        assertNotEquals(pin, hash)
        assertEquals(64, hash.length) // SHA-256 hex string is 64 characters
        repository.setAppLock(true, pin)
        assertTrue(repository.verifyPin("1234"))
        assertFalse(repository.verifyPin("9999"))
    }

    @Test
    fun `localization returns proper translations for English, Pashto, and Dari`() {
        val englishTitle = AppStrings.get("app_title", AppLanguage.ENGLISH)
        val pashtoTitle = AppStrings.get("app_title", AppLanguage.PASHTO)
        val dariTitle = AppStrings.get("app_title", AppLanguage.DARI)

        assertEquals("My Daily", englishTitle)
        assertEquals("زما ورځنی", pashtoTitle)
        assertEquals("روزانه‌های من", dariTitle)
    }

    @Test
    fun `quotes and dates helper returns valid values`() {
        val quote = DailyQuotes.getTodayQuote()
        assertTrue(quote.isNotBlank())

        val greeting = DateUtils.getGreeting()
        assertTrue(greeting.startsWith("good_"))
    }
}
