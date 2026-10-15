package com.dlcode.chelasapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dlcode.chelasapp.ui.navigation.Screen
import com.dlcode.chelasapp.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    val duration = 2600
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = duration, easing = LinearEasing)
        )
        delay(300) // Un pequeño respiro al final
        navController.navigate(Screen.Inicio.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    val progress = animationProgress.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreen),
        contentAlignment = Alignment.Center
    ) {
        // Ambient Light
        val ambientScale by animateFloatAsState(
            targetValue = if (progress > 0.1f) 1.1f else 0.8f,
            animationSpec = tween(2600, easing = EaseOut), label = ""
        )
        val ambientAlpha by animateFloatAsState(
            targetValue = if (progress > 0.7f) 0.6f else 1f,
            animationSpec = tween(2600), label = ""
        )
        
        Canvas(modifier = Modifier.size(340.dp).scale(ambientScale).alpha(ambientAlpha)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Amber.copy(alpha = 0.08f), Amber.copy(alpha = 0.02f), Color.Transparent),
                    center = center,
                    radius = size.minDimension / 2
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Main Identity SVG Area
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .scale(lerp(0.92f, 1f, progress)), // Settle effect
                contentAlignment = Alignment.Center
            ) {
                IdentityAnimation(progress)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Wordmark
            val wordmarkAlpha = calculateAlpha(progress, 0.48f, 0.70f)
            val wordmarkTranslation = lerp(12f, 0f, calculateAlpha(progress, 0.48f, 0.70f))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(wordmarkAlpha)
                    .offset(y = wordmarkTranslation.dp)
            ) {
                Text(
                    text = "SAN JUAN",
                    color = LightBg,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 3.sp
                )
                Text(
                    text = "MI NEGOCIO",
                    color = Color(0xFFD0DEBC),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Loading Area
            Box(modifier = Modifier.width(140.dp).height(50.dp), contentAlignment = Alignment.Center) {
                // Progress Bar
                val loadingAlpha = calculateAlpha(progress, 0.15f, 0.25f) * (1f - calculateAlpha(progress, 0.78f, 0.91f))
                if (loadingAlpha > 0f) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.alpha(loadingAlpha)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        ) {
                            val fillProgress = calculateAlpha(progress, 0.20f, 0.80f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fillProgress)
                                    .fillMaxHeight()
                                    .background(Amber, RoundedCornerShape(8.dp))
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Preparando tu negocio",
                            color = Color(0xFFC8D9B6),
                            fontSize = 11.sp
                        )
                    }
                }

                // Ready Text
                val readyAlpha = calculateAlpha(progress, 0.84f, 1.0f)
                if (readyAlpha > 0f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.alpha(readyAlpha).offset(y = lerp(5f, 0f, readyAlpha).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFFD7E8BD),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Todo listo",
                            color = Color(0xFFD7E8BD),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Footer
        val footerAlpha = calculateAlpha(progress, 0.55f, 0.85f)
        Text(
            text = "VENTAS · STOCK · CAJA",
            color = Color(0xFFADC5A7),
            fontSize = 10.sp,
            letterSpacing = 1.5.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(footerAlpha)
        )
    }
}

@Composable
fun IdentityAnimation(progress: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val sf = w / 320f

        // Ring
        val ringAlpha = calculateAlpha(progress, 0.67f, 0.75f) * (1f - calculateAlpha(progress, 0.75f, 0.94f))
        val ringScale = lerp(0.8f, 1.25f, calculateAlpha(progress, 0.67f, 1.0f))
        if (ringAlpha > 0f) {
            drawCircle(
                color = Amber,
                radius = 142f * sf * ringScale,
                style = Stroke(width = 1.1f * sf),
                alpha = ringAlpha * 0.2f
            )
        }

        // Bottles and Crate
        // Center Bottle
        val cbAlpha = calculateAlpha(progress, 0.0f, 0.05f)
        val cbY = lerp(-25f, 0f, calculateAlpha(progress, 0.05f, 0.25f)) * sf
        val cbScale = lerp(0.88f, 1f, calculateAlpha(progress, 0.05f, 0.25f))
        
        if (cbAlpha > 0f) {
            translate(left = 0f, top = cbY) {
                drawBottle(sf, cbScale, Amber, White, DarkGreen, 145f, 31f)
            }
        }

        // Left Bottle
        val lbAlpha = calculateAlpha(progress, 0.13f, 0.18f)
        val lbX = lerp(-15f, 0f, calculateAlpha(progress, 0.13f, 0.34f)) * sf
        val lbY = lerp(-20f, 0f, calculateAlpha(progress, 0.13f, 0.34f)) * sf
        if (lbAlpha > 0f) {
            translate(left = lbX, top = lbY) {
                drawBottle(sf, 1f, Amber, White, DarkGreen, 78f, 56f)
            }
        }

        // Right Bottle
        val rbAlpha = calculateAlpha(progress, 0.20f, 0.25f)
        val rbX = lerp(15f, 0f, calculateAlpha(progress, 0.20f, 0.41f)) * sf
        val rbY = lerp(-20f, 0f, calculateAlpha(progress, 0.20f, 0.41f)) * sf
        if (rbAlpha > 0f) {
            translate(left = rbX, top = rbY) {
                drawBottle(sf, 1f, Amber, White, DarkGreen, 214f, 56f)
            }
        }

        // Crate
        val crateAlpha = calculateAlpha(progress, 0.28f, 0.33f)
        val crateY = lerp(24f, 0f, calculateAlpha(progress, 0.28f, 0.47f)) * sf
        if (crateAlpha > 0f) {
            translate(top = crateY) {
                val cratePath = Path().apply {
                    moveTo(47f * sf, 170f * sf)
                    lineTo(34f * sf, 190f * sf)
                    lineTo(34f * sf, 279f * sf)
                    lineTo(286f * sf, 279f * sf)
                    lineTo(286f * sf, 190f * sf)
                    lineTo(273f * sf, 170f * sf)
                    close()
                }
                drawPath(cratePath, DarkGreen)
                drawPath(cratePath, Amber, style = Stroke(width = 6f * sf))
                
                drawRect(Amber, Offset(44f * sf, 185f * sf), Size(232f * sf, 17f * sf))
                drawRect(Amber, Offset(44f * sf, 270f * sf), Size(232f * sf, 12f * sf))
            }
        }

        // Check
        val checkProgress = calculateAlpha(progress, 0.48f, 0.84f)
        if (checkProgress > 0f) {
            val checkPath = Path().apply {
                moveTo(124f * sf, 233f * sf)
                lineTo(148f * sf, 256f * sf)
                lineTo(195f * sf, 210f * sf)
            }
            val outPath = Path()
            PathMeasure().apply {
                setPath(checkPath, false)
                getSegment(0f, length * checkProgress, outPath, true)
            }
            drawPath(
                path = outPath,
                color = White,
                style = Stroke(width = 14f * sf, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBottle(
    sf: Float, scale: Float, amber: Color, white: Color, green: Color, x: Float, y: Float
) {
    val bottleColor = amber
    translate(x * sf, y * sf) {
        scale(scale, Offset(15f * sf, 40f * sf)) {
            drawRoundRect(bottleColor, Offset(0f, 0f), Size(30f * sf, 9f * sf), CornerRadius(3f * sf))
            val body = Path().apply {
                moveTo(1f * sf, 14f * sf)
                lineTo(29f * sf, 14f * sf)
                lineTo(29f * sf, 55f * sf)
                quadraticTo(29f * sf, 64f * sf, 44f * sf, 75f * sf)
                lineTo(44f * sf, 155f * sf)
                lineTo(-14f * sf, 155f * sf)
                lineTo(-14f * sf, 75f * sf)
                quadraticTo(1f * sf, 64f * sf, 1f * sf, 55f * sf)
                close()
            }
            drawPath(body, bottleColor)
            drawRoundRect(white, Offset(-6f * sf, 92f * sf), Size(42f * sf, 40f * sf), CornerRadius(5f * sf))
            drawLine(green, Offset(4f * sf, 104f * sf), Offset(26f * sf, 104f * sf), strokeWidth = 3f * sf, cap = StrokeCap.Round)
            drawLine(green, Offset(8f * sf, 112f * sf), Offset(22f * sf, 112f * sf), strokeWidth = 3f * sf, cap = StrokeCap.Round)
        }
    }
}

fun calculateAlpha(progress: Float, start: Float, end: Float): Float {
    return ((progress - start) / (end - start)).coerceIn(0f, 1f)
}

fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
