package trnqilo.telecomando.data

internal fun String.escapeJsonString(): String = buildString(length + 8) {
  for (char in this@escapeJsonString) {
    when (char) {
      '\\' -> append("\\\\")
      '"' -> append("\\\"")
      '\b' -> append("\\b")
      '\u000C' -> append("\\f")
      '\n' -> append("\\n")
      '\r' -> append("\\r")
      '\t' -> append("\\t")
      else -> append(char)
    }
  }
}

internal fun flatJsonObjectOf(vararg entries: Pair<String, String>): String = buildString {
  append('{')
  entries.forEachIndexed { index, (key, value) ->
    if (index > 0) append(',')
    append('"')
    append(key.escapeJsonString())
    append('"')
    append(':')
    append('"')
    append(value.escapeJsonString())
    append('"')
  }
  append('}')
}

internal fun String.parseFlatJsonObject(): Map<String, String>? {
  val text = trim()
  if (text.isEmpty() || text == "{}") return emptyMap()
  if (!text.startsWith('{') || !text.endsWith('}')) return null

  val values = linkedMapOf<String, String>()
  var index = 1
  val last = text.length - 1

  fun skipWhitespace() {
    while (index < last && text[index].isWhitespace()) index++
  }

  fun readString(): String? {
    if (index >= last || text[index] != '"') return null
    index++
    val out = StringBuilder()
    while (index < last) {
      val char = text[index++]
      when (char) {
        '"' -> return out.toString()
        '\\' -> {
          if (index >= last) return null
          when (val escaped = text[index++]) {
            '"', '\\', '/' -> out.append(escaped)
            'b' -> out.append('\b')
            'f' -> out.append('\u000C')
            'n' -> out.append('\n')
            'r' -> out.append('\r')
            't' -> out.append('\t')
            'u' -> {
              if (index + 4 > text.length) return null
              val hex = text.substring(index, index + 4)
              val codePoint = hex.toIntOrNull(16) ?: return null
              out.append(codePoint.toChar())
              index += 4
            }
            else -> return null
          }
        }
        else -> out.append(char)
      }
    }
    return null
  }

  skipWhitespace()
  if (index < last && text[index] == '}') return emptyMap()
  while (index < last) {
    skipWhitespace()
    val key = readString() ?: return null
    skipWhitespace()
    if (index >= last || text[index] != ':') return null
    index++
    skipWhitespace()
    val value = readString() ?: return null
    values[key] = value
    skipWhitespace()
    if (index > last) return null
    when (text[index]) {
      ',' -> {
        index++
        continue
      }
      '}' -> return values
      else -> return null
    }
  }
  return values
}
