package com.example.lockedsecure

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lockedsecure.theme.*

class MainActivity : ComponentActivity() {
    private val isAccessibilityEnabled = mutableStateOf(false)
    private val isAdminEnabled = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockedSecureTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MattDarkBackground
                ) {
                    MainScreen(
                        isAccessibilityEnabled = isAccessibilityEnabled.value,
                        isAdminEnabled = isAdminEnabled.value,
                        onOpenAccessibility = { openAccessibilitySettings() },
                        onOpenAdmin = { openAdminSettings() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isAccessibilityEnabled.value = isAccessibilityServiceEnabled(this)
        isAdminEnabled.value = isDeviceAdminEnabled(this)
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val expected = ComponentName(context, PowerMenuBlockerService::class.java).flattenToString()
        val enabled = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        return enabled?.contains(expected) == true
    }

    private fun isDeviceAdminEnabled(context: Context): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, AdminReceiver::class.java)
        return dpm.isAdminActive(adminComponent)
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open Accessibility settings", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAdminSettings() {
        try {
            val adminComponent = ComponentName(this, AdminReceiver::class.java)
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Protects LockedSecure from deletion.")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open Device Admin settings", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun MainScreen(
    isAccessibilityEnabled: Boolean,
    isAdminEnabled: Boolean,
    onOpenAccessibility: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isFullyProtected = isAccessibilityEnabled && isAdminEnabled

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .safeDrawingPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "LOCKEDSECURE",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "ANTI-THEFT SHIELD",
                color = TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 2.sp
            )
        }

        // Middle Shield Container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            // Shield Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MattCardBorder,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .background(
                        color = MattCardBackground,
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(vertical = 40.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val tintColor = if (isFullyProtected) AccentActive else AccentInactive
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = tintColor.copy(alpha = 0.08f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(48.dp)) {
                            val path = Path().apply {
                                moveTo(size.width * 0.5f, size.height * 0.1f)
                                cubicTo(
                                    size.width * 0.85f, size.height * 0.1f,
                                    size.width * 0.9f, size.height * 0.25f,
                                    size.width * 0.9f, size.height * 0.45f
                                )
                                cubicTo(
                                    size.width * 0.9f, size.height * 0.72f,
                                    size.width * 0.5f, size.height * 0.92f,
                                    size.width * 0.5f, size.height * 0.92f
                                )
                                cubicTo(
                                    size.width * 0.5f, size.height * 0.92f,
                                    size.width * 0.1f, size.height * 0.72f,
                                    size.width * 0.1f, size.height * 0.45f
                                )
                                cubicTo(
                                    size.width * 0.1f, size.height * 0.25f,
                                    size.width * 0.15f, size.height * 0.1f,
                                    size.width * 0.5f, size.height * 0.1f
                                )
                                close()
                            }
                            drawPath(
                                path = path,
                                color = tintColor,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = if (isFullyProtected) "Shield Active" else "Shield Offline",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    StatusRow(label = "Power Blocker", isActive = isAccessibilityEnabled)
                    Spacer(modifier = Modifier.height(10.dp))
                    StatusRow(label = "Uninstall Protection", isActive = isAdminEnabled)
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onOpenAccessibility,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAccessibilityEnabled) AccentActive.copy(alpha = 0.12f) else AccentInactive,
                    contentColor = if (isAccessibilityEnabled) AccentActive else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = if (isAccessibilityEnabled) 1.dp else 0.dp,
                        color = if (isAccessibilityEnabled) AccentActive.copy(alpha = 0.3f) else Color.Transparent,
                        shape = RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (isAccessibilityEnabled) "Blocker Service Enabled" else "Enable Power Blocker",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }

            Button(
                onClick = onOpenAdmin,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAdminEnabled) AccentActive.copy(alpha = 0.12f) else AccentInactive,
                    contentColor = if (isAdminEnabled) AccentActive else Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(
                        width = if (isAdminEnabled) 1.dp else 0.dp,
                        color = if (isAdminEnabled) AccentActive.copy(alpha = 0.3f) else Color.Transparent,
                        shape = RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (isAdminEnabled) "Admin Protection Enabled" else "Enable Admin Protection",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun StatusRow(label: String, isActive: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(
                    color = if (isActive) AccentActive else AccentInactive,
                    shape = CircleShape
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label: ${if (isActive) "Protected" else "Unprotected"}",
            color = if (isActive) TextPrimary else TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
