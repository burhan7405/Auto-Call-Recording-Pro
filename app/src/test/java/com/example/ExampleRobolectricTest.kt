package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CallRecorder Pro", appName)
  }

  @Test
  fun `test duration formatter`() {
    assertEquals("00:00", Formatters.formatDuration(0))
    assertEquals("00:45", Formatters.formatDuration(45))
    assertEquals("02:15", Formatters.formatDuration(135))
    assertEquals("01:00:00", Formatters.formatDuration(3600))
  }
}
