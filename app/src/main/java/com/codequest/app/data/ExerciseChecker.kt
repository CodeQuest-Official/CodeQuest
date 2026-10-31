package com.codequest.app.data

data class ExerciseCheckResult(val isCorrect: Boolean, val feedback: String)

fun checkExerciseAnswer(lesson: Lesson, answer: String): ExerciseCheckResult {
    val submitted = answer.trim()
    if (submitted.isBlank()) {
        return ExerciseCheckResult(false, "Write an answer first, then check it here.")
    }

    val expected = lesson.solution.trim()
    val correct = if (lesson.mode == ExerciseMode.PREDICT) {
        submitted == expected
    } else {
        normalizeCode(submitted, lesson.language) == normalizeCode(expected, lesson.language)
    }

    if (correct) {
        val note = if (lesson.mode == ExerciseMode.PREDICT) {
            "Correct. That is the expected output."
        } else {
            "Correct. The required code structure and values match."
        }
        return ExerciseCheckResult(true, "$note This local check does not run code. +20 XP")
    }

    if (lesson.mode == ExerciseMode.ORDER) {
        val expectedLines = normalizedLines(expected, lesson.language).map { it.tokens }
        val submittedLines = normalizedLines(submitted, lesson.language).map { it.tokens }
        if (expectedLines.size == submittedLines.size && expectedLines.toSet() == submittedLines.toSet()) {
            return ExerciseCheckResult(false, "Those code lines look right, but their order is different. Try again.")
        }
    }

    val expectedTokens = normalizeCode(expected, lesson.language).toSet()
    val submittedTokens = normalizeCode(submitted, lesson.language).toSet()
    val sharedTokens = expectedTokens.intersect(submittedTokens).size
    val hasSomeExpectedParts = sharedTokens >= minOf(2, expectedTokens.size)
    return if (hasSomeExpectedParts) {
        ExerciseCheckResult(false, "Some parts match, but the code structure, order, or a required value is different. Compare it with the task.")
    } else {
        ExerciseCheckResult(false, "Not quite yet. Check the command, names, values, and punctuation, then try again.")
    }
}

private data class NormalizedLine(val indentation: Int, val tokens: List<String>)

private fun normalizedLines(code: String, language: Language): List<NormalizedLine> {
    val lines = mutableListOf<NormalizedLine>()
    var indentStack = mutableListOf(0)
    code.lineSequence().forEach { originalLine ->
        val lineWithoutComment = stripComment(originalLine, language)
        if (lineWithoutComment.isBlank()) return@forEach
        val indentationWidth = lineWithoutComment.takeWhile { it == ' ' || it == '\t' }
            .fold(0) { count, char -> if (char == '\t') count + 4 else count + 1 }
        val tokens = tokenize(lineWithoutComment.trim(), language).toMutableList()
        if (language == Language.JAVASCRIPT && tokens.lastOrNull() == ";") {
            tokens.removeAt(tokens.lastIndex)
        }
        if (tokens.isEmpty()) return@forEach
        val indentation = if (language == Language.PYTHON) {
            when {
                indentationWidth > indentStack.last() -> {
                    indentStack = (indentStack + indentationWidth).toMutableList()
                    indentStack.lastIndex
                }
                indentationWidth < indentStack.last() -> {
                    while (indentStack.size > 1 && indentationWidth < indentStack.last()) {
                        indentStack.removeAt(indentStack.lastIndex)
                    }
                    indentStack.lastIndex
                }
                else -> indentStack.lastIndex
            }
        } else {
            0
        }
        lines += NormalizedLine(indentation, tokens)
    }
    return lines
}

private fun normalizeCode(code: String, language: Language): List<String> =
    normalizedLines(code, language).flatMap { line ->
        listOf("<indent:${line.indentation}>") + line.tokens + listOf("<line>")
    }

private fun tokenize(line: String, language: Language): List<String> {
    val tokenPattern = Regex("\"(?:\\\\.|[^\"\\\\])*\"|'(?:\\\\.|[^'\\\\])*'|`(?:\\\\.|[^`\\\\])*`|[A-Za-z_$][A-Za-z0-9_$]*|\\d+(?:\\.\\d+)?|===|!==|=>|==|!=|<=|>=|\\+=|-=|\\*=|/=|\\*\\*|//|[^\\s]")
    return tokenPattern.findAll(line).map { match ->
        val value = match.value
        when {
            value.length >= 2 && value.first() in charArrayOf('"', '\'') ->
                "<string:${value.substring(1, value.lastIndex)}>"
            value.length >= 2 && value.first() == '`' -> "<template:$value>"
            language == Language.JAVASCRIPT && value == "==" -> "=="
            else -> value
        }
    }.toList()
}

private fun stripComment(line: String, language: Language): String {
    var quote: Char? = null
    var escaped = false
    var index = 0
    while (index < line.length) {
        val char = line[index]
        if (quote != null) {
            if (escaped) {
                escaped = false
            } else if (char == '\\') {
                escaped = true
            } else if (char == quote) {
                quote = null
            }
        } else {
            when {
                char == '"' || char == '\'' || (language == Language.JAVASCRIPT && char == '`') -> quote = char
                language == Language.PYTHON && char == '#' -> return line.substring(0, index)
                language == Language.JAVASCRIPT && char == '/' && line.getOrNull(index + 1) == '/' ->
                    return line.substring(0, index)
            }
        }
        index++
    }
    return line
}
