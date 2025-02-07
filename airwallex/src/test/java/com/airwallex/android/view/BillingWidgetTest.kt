package com.airwallex.android.view

import android.util.AttributeSet
import androidx.appcompat.view.ContextThemeWrapper
import androidx.test.core.app.ApplicationProvider
import com.airwallex.android.R
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.*

@RunWith(RobolectricTestRunner::class)
class BillingWidgetTest {

    private var attributes: AttributeSet? = null
    private lateinit var billingWidget: BillingWidget

    private val context = ContextThemeWrapper(
        ApplicationProvider.getApplicationContext(),
        R.style.AirwallexDefaultTheme
    )

    @BeforeTest
    fun setup() {
        billingWidget = BillingWidget(context, attributes)
    }

    @Test
    fun isNotNull() {
        assertNotNull(context)
    }

    @Test
    fun isValidTest() {
        assertEquals(true, billingWidget.isValid(false, "neil", "Nie", "china", "jiangsu", "suzhou", "lianhuastrict", "94706078@qq.com"))
    }
}
