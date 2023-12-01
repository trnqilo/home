package trnqilo.telecomando.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import trnqilo.telecomando.preferences.ThemeMode

private val LightColors = lightColorScheme(
  primary = Color(0xFF006A60),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFA4F2E5),
  onPrimaryContainer = Color(0xFF00201C),
  secondary = Color(0xFF4A635E),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFCCE8E1),
  onSecondaryContainer = Color(0xFF06201C),
  tertiary = Color(0xFF755A00),
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFFFDF91),
  onTertiaryContainer = Color(0xFF241A00),
  error = Color(0xFFBA1A1A),
  background = Color(0xFFF7FAF8),
  onBackground = Color(0xFF191C1B),
  surface = Color(0xFFF7FAF8),
  onSurface = Color(0xFF191C1B),
  surfaceVariant = Color(0xFFDAE5E1),
  onSurfaceVariant = Color(0xFF3F4946),
  outline = Color(0xFF6F7976),
)

private val DarkColors = darkColorScheme(
  primary = Color(0xFF85D5C9),
  onPrimary = Color(0xFF003730),
  primaryContainer = Color(0xFF005048),
  onPrimaryContainer = Color(0xFFA4F2E5),
  secondary = Color(0xFFB0CCC5),
  onSecondary = Color(0xFF1B3530),
  secondaryContainer = Color(0xFF324B46),
  onSecondaryContainer = Color(0xFFCCE8E1),
  tertiary = Color(0xFFE9C349),
  onTertiary = Color(0xFF3D2F00),
  tertiaryContainer = Color(0xFF574500),
  onTertiaryContainer = Color(0xFFFFDF91),
  error = Color(0xFFFFB4AB),
  background = Color(0xFF0D1513),
  onBackground = Color(0xFFDDE5E1),
  surface = Color(0xFF0D1513),
  onSurface = Color(0xFFDDE5E1),
  surfaceVariant = Color(0xFF3F4946),
  onSurfaceVariant = Color(0xFFBEC9C5),
  outline = Color(0xFF89938F),
)

private val AppTypography = Typography(
  headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold),
  titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
  titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
  bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
  bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
  labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
)

private val AppShapes = androidx.compose.material3.Shapes(
  small = RoundedCornerShape(8.dp),
  medium = RoundedCornerShape(12.dp),
  large = RoundedCornerShape(20.dp),
)

@Composable
fun TelecomandoTheme(
  mode: ThemeMode,
  content: @Composable () -> Unit,
) {
  val darkTheme = when (mode) {
    ThemeMode.System -> isSystemInDarkTheme()
    ThemeMode.Light -> false
    ThemeMode.Dark -> true
  }
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window ?: return@SideEffect
      WindowCompat.getInsetsController(window, view).apply {
        isAppearanceLightStatusBars = !darkTheme
        isAppearanceLightNavigationBars = !darkTheme
      }
    }
  }

  MaterialTheme(
    colorScheme = if (darkTheme) DarkColors else LightColors,
    typography = AppTypography,
    shapes = AppShapes,
    content = content,
  )
}
