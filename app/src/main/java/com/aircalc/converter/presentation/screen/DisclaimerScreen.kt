package com.aircalc.converter.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aircalc.converter.ui.theme.*

/**
 * First-launch disclaimer screen that must be accepted before using the app.
 *
 * This screen displays food safety disclaimers and requires user acceptance.
 * Once accepted, the screen never appears again.
 *
 * @param onAccept Callback invoked when user accepts the disclaimer
 */
@Composable
fun DisclaimerScreen(
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        // Title
        Text(
            text = "Important Notice",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Scrollable content area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Disclaimer text items
            DisclaimerTextItem(
                text = "AirCalc provides cooking time estimates only - they are guidelines, not guarantees"
            )

            Spacer(modifier = Modifier.height(16.dp))

            DisclaimerTextItem(
                text = "Always verify food is thoroughly cooked before eating"
            )

            Spacer(modifier = Modifier.height(16.dp))

            DisclaimerTextItem(
                text = "You are responsible for ensuring food is cooked safely"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Terms of Service link
            val annotatedString = buildAnnotatedString {
                pushStringAnnotation(
                    tag = "URL",
                    annotation = "https://github.com/matt99is/aircalc/blob/main/Terms_of_Service.md"
                )
                withStyle(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append("View Terms of Service")
                }
                pop()
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    annotatedString.getStringAnnotations(
                        tag = "URL",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let { annotation ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                        context.startActivity(intent)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Accept Button
        Button(
            onClick = onAccept,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "Accept",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Individual disclaimer text item with consistent styling.
 */
@Composable
private fun DisclaimerTextItem(
    text: String
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = MaterialTheme.colorScheme.onBackground
    )
}
