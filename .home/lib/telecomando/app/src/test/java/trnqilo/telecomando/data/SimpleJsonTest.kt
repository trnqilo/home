package trnqilo.telecomando.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SimpleJsonTest {
  @Test
  fun parsesNonEmptyFlatObject() {
    assertEquals(
      mapOf("Authorization" to "Bearer abc", "X-Note" to "a\"b"),
      "{\"Authorization\":\"Bearer abc\",\"X-Note\":\"a\\\"b\"}".parseFlatJsonObject(),
    )
  }

  @Test
  fun rejectsMalformedObject() {
    assertNull("not json".parseFlatJsonObject())
  }
}
