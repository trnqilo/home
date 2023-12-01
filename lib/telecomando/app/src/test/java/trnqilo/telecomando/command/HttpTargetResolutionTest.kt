package trnqilo.telecomando.command

import org.junit.Assert.assertEquals
import org.junit.Test

class HttpTargetResolutionTest {
  @Test
  fun joinsConnectionBaseUrlAndCommandPath() {
    assertEquals(
      "https://api.example.test/v1/health",
      resolveHttpUrl("https://api.example.test/", "/v1/health"),
    )
  }

  @Test
  fun preservesMigratedConnectionUrlWhenPathIsEmpty() {
    assertEquals(
      "https://api.example.test/health?full=true",
      resolveHttpUrl("https://api.example.test/health?full=true", ""),
    )
  }

  @Test
  fun absoluteCommandUrlOverridesBaseUrl() {
    assertEquals(
      "https://other.example.test/health",
      resolveHttpUrl("https://api.example.test", "https://other.example.test/health"),
    )
  }
}
