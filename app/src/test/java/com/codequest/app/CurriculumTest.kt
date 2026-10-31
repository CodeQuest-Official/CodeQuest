package com.codequest.app

import com.codequest.app.data.Language
import com.codequest.app.data.allLessons
import com.codequest.app.data.buildAssessmentQuestions
import com.codequest.app.data.englishLesson
import com.codequest.app.data.englishLessonsFor
import com.codequest.app.data.javascriptLessons
import com.codequest.app.data.pythonLessons
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumTest {
    @Test
    fun bothLearningPathsContainAtLeastThirtyLessonsAcrossSevenChapters() {
        assertTrue(pythonLessons.size >= 30)
        assertTrue(javascriptLessons.size >= 30)
        assertEquals(7, pythonLessons.map { it.chapter }.distinct().size)
        assertEquals(7, javascriptLessons.map { it.chapter }.distinct().size)
        assertEquals(pythonLessons.size + javascriptLessons.size, allLessons.size)
    }

    @Test
    fun everyLessonHasAUsableExerciseAndQuiz() {
        allLessons.forEach { lesson ->
            assertTrue(lesson.title.isNotBlank())
            assertTrue(lesson.explanation.length > 180)
            assertTrue(lesson.example.isNotBlank())
            assertTrue(lesson.exercise.isNotBlank())
            assertTrue(lesson.solution.isNotBlank())
            assertEquals(4, lesson.answers.size)
            assertTrue(lesson.correctAnswer in lesson.answers.indices)
            assertTrue(lesson.quizExplanation.isNotBlank())
        }
    }

    @Test
    fun lessonsAreAssignedToTheirOwnLanguage() {
        assertTrue(pythonLessons.all { it.language == Language.PYTHON })
        assertTrue(javascriptLessons.all { it.language == Language.JAVASCRIPT })
        assertTrue(pythonLessons.map { it.id }.intersect(javascriptLessons.map { it.id }.toSet()).isEmpty())
    }

    @Test
    fun englishCurriculumCoversEveryLessonWithoutChangingItsStableIdOrAnswer() {
        allLessons.forEach { german ->
            val english = englishLesson(german)
            assertEquals(german.id, english.id)
            assertEquals(german.language, english.language)
            assertNotEquals(german.chapter, english.chapter)
            assertNotEquals(german.explanation, english.explanation)
            assertNotEquals(german.exercise, english.exercise)
            assertNotEquals(german.question, english.question)
            assertTrue(english.chapter.isNotBlank())
            assertTrue(english.title.isNotBlank())
            assertTrue(english.explanation.length > 35)
            assertFalse(english.explanation.contains("Read the example one line at a time"))
            assertTrue(english.exercise.isNotBlank())
            assertTrue(english.question.isNotBlank())
            assertEquals(4, english.answers.size)
            assertTrue(english.correctAnswer in english.answers.indices)
            assertTrue(english.quizExplanation.isNotBlank())
            val visibleCopy = listOf(
                english.chapter, english.title, english.explanation, english.example,
                english.breakdown, english.mistake, english.memory, english.exercise,
                english.solution, english.question, english.quizExplanation
            ).plus(english.answers).joinToString(" ")
            assertFalse("German characters remain in ${english.id}", Regex("[äöüÄÖÜß]").containsMatchIn(visibleCopy))
            val code = "${english.example}\n${english.solution}".lowercase()
            val germanCodeWords = Regex("\\b(hallo|guten|stadt|alter|temperatur|farbe|farben|punkte|punktzahl|platz|schritt|zeile|spalte|einkauf|einkaufsliste|getraenke|fruechte|produkt|artikel|tiere|begruessung|begruesse|sage_hallo|addiere|verdopple|quadrat|spielername|spieler|leben|antwort|ergebnis|anzahl|muede|ist_offen|ist_tag|mein_hund|mein_buch|koordinaten|nachricht)\\b")
            assertFalse("German words remain in code for ${english.id}", germanCodeWords.containsMatchIn(code))
        }
        assertEquals(41, pythonLessons.size)
        assertEquals(41, javascriptLessons.size)
    }

    @Test
    fun englishCodeExamplesAlsoTranslateVisibleStringsAndTemplateLiterals() {
        val javascriptStringsLesson = englishLesson(javascriptLessons[8])
        assertTrue(javascriptStringsLesson.example.contains("Hello, \${name}!"))
        assertTrue(englishLesson(pythonLessons[7]).example.contains("I live in "))
    }

    @Test
    fun pythonStartsWithAConcisePrintLesson() {
        val firstLesson = englishLessonsFor(Language.PYTHON).first()
        assertEquals("Your first print()", firstLesson.title)
        assertTrue(firstLesson.example.startsWith("print("))
        assertTrue(firstLesson.explanation.length < 180)
    }

    @Test
    fun everyChapterHasTenAssessmentQuestionsAndEachCourseHasTwentyFinalQuestions() {
        listOf(Language.PYTHON, Language.JAVASCRIPT).forEach { language ->
            val lessons = englishLessonsFor(language)
            val chapterCount = lessons.map { it.chapter }.distinct().size
            repeat(chapterCount) { chapterIndex ->
                val questions = buildAssessmentQuestions(lessons, chapterIndex)
                assertEquals("$language chapter $chapterIndex", 10, questions.size)
                assertValidAssessment(questions)
            }
            val finalQuestions = buildAssessmentQuestions(lessons)
            assertEquals(20, finalQuestions.size)
            assertValidAssessment(finalQuestions)
        }
    }

    private fun assertValidAssessment(questions: List<com.codequest.app.data.AssessmentQuestion>) {
        questions.forEach { question ->
            assertTrue(question.prompt.isNotBlank())
            assertEquals(4, question.options.size)
            assertEquals(4, question.options.distinct().size)
            assertTrue(question.correctAnswer in question.options.indices)
            assertTrue(question.explanation.isNotBlank())
        }
    }
}
