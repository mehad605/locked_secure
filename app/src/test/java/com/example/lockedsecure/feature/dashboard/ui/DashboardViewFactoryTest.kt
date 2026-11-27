package com.example.lockedsecure.feature.dashboard.ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.lockedsecure.feature.dashboard.domain.ProtectionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests for [DashboardViewFactory] verifying Core Contract C7 visual text/states and
 * button click dispatch across all protection combinations.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [30])
class DashboardViewFactoryTest {

    @Test
    fun rendersAllProtectionStatesAndDispatchesButtonClicks() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        var accessibilityClicks = 0
        var adminClicks = 0

        val ui = DashboardViewFactory.create(
            context = activity,
            onOpenAccessibility = { accessibilityClicks++ },
            onOpenAdmin = { adminClicks++ },
        )

        // State 1: Neither enabled
        ui.render(ProtectionStatus(isAccessibilityEnabled = false, isAdminEnabled = false))
        val textsOff = collectTexts(ui.rootView)
        assertTrue(textsOff.contains("LOCKEDSECURE"))
        assertTrue(textsOff.contains("ANTI-THEFT SHIELD"))
        assertTrue(textsOff.contains("Shield Offline"))
        assertTrue(textsOff.contains("Power Blocker: Unprotected"))
        assertTrue(textsOff.contains("Uninstall Protection: Unprotected"))
        assertTrue(textsOff.contains("Enable Power Blocker"))
        assertTrue(textsOff.contains("Enable Admin Protection"))

        // State 2: Accessibility only
        ui.render(ProtectionStatus(isAccessibilityEnabled = true, isAdminEnabled = false))
        val textsAcc = collectTexts(ui.rootView)
        assertTrue(textsAcc.contains("Shield Offline"))
        assertTrue(textsAcc.contains("Power Blocker: Protected"))
        assertTrue(textsAcc.contains("Uninstall Protection: Unprotected"))
        assertTrue(textsAcc.contains("Blocker Service Enabled"))
        assertTrue(textsAcc.contains("Enable Admin Protection"))

        // State 3: Fully protected
        ui.render(ProtectionStatus(isAccessibilityEnabled = true, isAdminEnabled = true))
        val textsFull = collectTexts(ui.rootView)
        assertTrue(textsFull.contains("Shield Active"))
        assertTrue(textsFull.contains("Power Blocker: Protected"))
        assertTrue(textsFull.contains("Uninstall Protection: Protected"))
        assertTrue(textsFull.contains("Blocker Service Enabled"))
        assertTrue(textsFull.contains("Admin Protection Enabled"))

        // Button click dispatch
        val buttons = collectButtons(ui.rootView)
        assertEquals(2, buttons.size)
        buttons[0].performClick()
        buttons[1].performClick()
        assertEquals(1, accessibilityClicks)
        assertEquals(1, adminClicks)
    }

    private fun collectTexts(root: View): Set<String> {
        val result = mutableSetOf<String>()
        if (root is TextView && root.text != null) {
            result.add(root.text.toString())
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                result.addAll(collectTexts(root.getChildAt(i)))
            }
        }
        return result
    }

    private fun collectButtons(root: View): List<Button> {
        val result = mutableListOf<Button>()
        if (root is Button) {
            result.add(root)
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                result.addAll(collectButtons(root.getChildAt(i)))
            }
        }
        return result
    }
}
