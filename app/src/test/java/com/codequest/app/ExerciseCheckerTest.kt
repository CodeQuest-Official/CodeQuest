package com.codequest.app

import com.codequest.app.data.ExerciseMode
import com.codequest.app.data.Language
import com.codequest.app.data.Lesson
import com.codequest.app.data.checkExerciseAnswer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseCheckerTest {
    private val sampleLesson = Lesson(
        id = "python-test",
        language = Language.PYTHON,
        chapter = "Python basics",
        title = "Printing text",
        explanation = "A sufficiently long explanation for a small test lesson.",
        example = "print(\"Hello\")",
        breakdown = "print displays text.",
        mistake = "Remember the parentheses.",
        memory = "print displays a message.",
        exercise = "Display Hello.",
        solution = "print(\"Hello\")",
        mode = ExerciseMode.WRITE,
        question = "Which command displays text?",
        answers = listOf("print()", "if", "for", "class"),
        correctAnswer = 0,
        quizExplanation = "print displays text."
    )

    @Test
    fun exactAnswerPassesLocalTextCheck() {
        assertTrue(checkExerciseAnswer(sampleLesson, "  print(\"Hello\")  ").isCorrect)
    }

    @Test
    fun missingRequiredCodeStructureDoesNotPass() {
        assertFalse(checkExerciseAnswer(sampleLesson, "Hello").isCorrect)
    }

    @Test
    fun blankAnswerGetsAnActionableMessage() {
        val result = checkExerciseAnswer(sampleLesson, "   ")
        assertFalse(result.isCorrect)
        assertTrue(result.feedback.contains("Write an answer"))
    }

    @Test
    fun outputPredictionCanBeCheckedWithoutExecutingCode() {
        val prediction = sampleLesson.copy(mode = ExerciseMode.PREDICT, solution = "12")
        assertTrue(checkExerciseAnswer(prediction, "12").isCorrect)
        assertFalse(checkExerciseAnswer(prediction, "13").isCorrect)
    }

    @Test
    fun similarLookingAnswerWithWrongRequiredValueDoesNotPass() {
        assertFalse(checkExerciseAnswer(sampleLesson, "print(\"Goodbye\")").isCorrect)
    }

    @Test
    fun linesMustRemainInTheCorrectOrder() {
        val ordered = sampleLesson.copy(
            mode = ExerciseMode.ORDER,
            solution = "name = \"Alex\"\nprint(name)"
        )
        val result = checkExerciseAnswer(ordered, "print(name)\nname = \"Alex\"")
        assertFalse(result.isCorrect)
        assertTrue(result.feedback.contains("order"))
    }

    @Test
    fun pythonIndentationIsPartOfTheAnswerStructure() {
        val nested = sampleLesson.copy(
            solution = "if True:\n    print(\"Hello\")"
        )
        assertTrue(checkExerciseAnswer(nested, "if True:\n    print('Hello')").isCorrect)
        assertFalse(checkExerciseAnswer(nested, "if True:\nprint(\"Hello\")").isCorrect)
    }

    @Test
    fun javascriptAllowsQuoteStyleAndOptionalTrailingSemicolon() {
        val javascript = sampleLesson.copy(
            language = Language.JAVASCRIPT,
            solution = "console.log(\"Hello\");"
        )
        assertTrue(checkExerciseAnswer(javascript, "console.log('Hello')").isCorrect)
    }

    @Test
    fun checkerClearlyStatesThatCodeIsNotExecuted() {
        val result = checkExerciseAnswer(sampleLesson, sampleLesson.solution)
        assertTrue(result.feedback.contains("does not run code"))
    }
}
