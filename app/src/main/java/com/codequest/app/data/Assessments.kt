package com.codequest.app.data

data class AssessmentQuestion(
    val prompt: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String
)

fun chapterAssessmentKey(language: Language, chapterIndex: Int): String =
    "${language.key}-chapter-$chapterIndex"

fun finalAssessmentKey(language: Language): String = "${language.key}-final"

fun buildAssessmentQuestions(
    lessons: List<Lesson>,
    chapterIndex: Int? = null,
    questionCount: Int = if (chapterIndex == null) 20 else 10
): List<AssessmentQuestion> {
    require(questionCount > 0) { "An assessment must contain at least one question." }
    val chapters = lessons.map { it.chapter }.distinct()
    val selectedChapter = chapterIndex?.let { chapters.getOrNull(it) }
        ?: if (chapterIndex == null) null else return emptyList()
    val chapterLessons = if (selectedChapter == null) lessons else lessons.filter { it.chapter == selectedChapter }
    if (chapterLessons.isEmpty()) return emptyList()

    val pool = mutableListOf<AssessmentQuestion>()
    chapterLessons.forEach { lesson ->
        pool += lesson.toAssessmentQuestion()

        val correctIdea = lesson.breakdown
        val distractors = chapterLessons.asSequence()
            .filterNot { it.id == lesson.id }
            .map { it.breakdown }
            .filter { it != correctIdea }
            .distinct()
            .take(3)
            .toMutableList()
        listOf(
            "It sends every value to a remote server automatically.",
            "It changes the device settings without being asked.",
            "It is a picture that has no relationship to instructions."
        ).forEach { fallback ->
            if (distractors.size < 3 && fallback != correctIdea && fallback !in distractors) {
                distractors += fallback
            }
        }
        if (distractors.size == 3) {
            val options = (distractors + correctIdea).shuffled()
            pool += AssessmentQuestion(
                prompt = "Which statement best explains “${lesson.title}”?",
                options = options,
                correctAnswer = options.indexOf(correctIdea),
                explanation = correctIdea
            )
        }

        val correctReminder = lesson.memory
        val reminderDistractors = chapterLessons.asSequence()
            .filterNot { it.id == lesson.id }
            .map { it.memory }
            .filter { it != correctReminder }
            .distinct()
            .take(3)
            .toMutableList()
        listOf(
            "The code should be memorized without understanding it.",
            "Every programming task needs an internet connection.",
            "A computer can guess missing instructions."
        ).forEach { fallback ->
            if (reminderDistractors.size < 3 && fallback != correctReminder && fallback !in reminderDistractors) {
                reminderDistractors += fallback
            }
        }
        if (reminderDistractors.size == 3) {
            val options = (reminderDistractors + correctReminder).shuffled()
            pool += AssessmentQuestion(
                prompt = "What is a useful reminder about “${lesson.title}”?",
                options = options,
                correctAnswer = options.indexOf(correctReminder),
                explanation = correctReminder
            )
        }
    }

    return pool.distinctBy { it.prompt }.shuffled().take(questionCount)
}

private fun Lesson.toAssessmentQuestion(): AssessmentQuestion {
    val shuffledOptions = answers.shuffled()
    val correctText = answers[correctAnswer]
    return AssessmentQuestion(
        prompt = question,
        options = shuffledOptions,
        correctAnswer = shuffledOptions.indexOf(correctText),
        explanation = quizExplanation
    )
}
