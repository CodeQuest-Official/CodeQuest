package com.codequest.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.codequest.app.data.AssessmentQuestion
import com.codequest.app.data.buildAssessmentQuestions
import com.codequest.app.data.chapterAssessmentKey
import com.codequest.app.data.checkExerciseAnswer
import com.codequest.app.data.finalAssessmentKey
import com.codequest.app.data.Language
import com.codequest.app.data.englishLessonsFor
import com.codequest.app.ui.CodeQuestScreen
import com.codequest.app.ui.AssessmentScreen
import com.codequest.app.ui.CoursePickerScreen
import com.codequest.app.ui.HomeScreen
import com.codequest.app.ui.LessonScreen
import com.codequest.app.ui.NameScreen
import com.codequest.app.ui.RoadmapScreen
import com.codequest.app.ui.SettingsScreen
import com.codequest.app.ui.MainNavigation
import com.codequest.app.ui.appPalette

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("codequest_english_v2", Context.MODE_PRIVATE)
        if (!preferences.getBoolean("old_state_removed", false)) {
            getSharedPreferences("codequest_local", Context.MODE_PRIVATE).edit().clear().apply()
            preferences.edit().putBoolean("old_state_removed", true).apply()
        }
        setContent { CodeQuestApp(preferences) }
    }
}

@androidx.compose.runtime.Composable
private fun CodeQuestApp(preferences: android.content.SharedPreferences) {
    var name by remember { mutableStateOf(preferences.getString("name", "").orEmpty()) }
    var selectedLanguage by remember {
        mutableStateOf(
            preferences.getString("course", null)?.let { stored ->
                Language.values().firstOrNull { it.key == stored }
            }
        )
    }
    var completed by remember {
        mutableStateOf(preferences.getStringSet("completed", emptySet<String>())?.toSet().orEmpty())
    }
    var passedAssessments by remember {
        mutableStateOf(preferences.getStringSet("passed_assessments", emptySet<String>())?.toSet().orEmpty())
    }
    var xp by remember { mutableStateOf(preferences.getInt("xp", 0)) }
    var darkMode by remember { mutableStateOf(preferences.getBoolean("dark_mode", false)) }
    var selectedLessonId by remember { mutableStateOf(preferences.getString("last_lesson", "").orEmpty()) }
    var answer by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }
    var answerCorrect by remember { mutableStateOf(false) }
    var showSolution by remember { mutableStateOf(false) }
    var assessmentQuestions by remember { mutableStateOf<List<AssessmentQuestion>>(emptyList()) }
    var assessmentId by remember { mutableStateOf("") }
    var assessmentTitle by remember { mutableStateOf("") }
    var assessmentIndex by remember { mutableStateOf(0) }
    var assessmentAnswer by remember { mutableStateOf<Int?>(null) }
    var assessmentAnswers by remember { mutableStateOf<List<Int?>>(emptyList()) }
    var assessmentScore by remember { mutableStateOf(0) }
    var assessmentFinished by remember { mutableStateOf(false) }
    var assessmentPassed by remember { mutableStateOf(false) }
    var assessmentRewardMessage by remember { mutableStateOf("") }
    var screen by remember {
        mutableStateOf(
            when {
                name.isBlank() -> CodeQuestScreen.NAME
                selectedLanguage == null -> CodeQuestScreen.COURSE_PICKER
                else -> CodeQuestScreen.HOME
            }
        )
    }

    val colors = appPalette(darkMode)
    val lessons = selectedLanguage?.let(::englishLessonsFor).orEmpty()
    val selectedIndex = lessons.indexOfFirst { it.id == selectedLessonId }.coerceAtLeast(0)
    val currentLesson = lessons.getOrNull(selectedIndex)

    fun startAssessment(id: String, title: String, chapterIndex: Int?) {
        val questions = buildAssessmentQuestions(lessons, chapterIndex)
        val expectedCount = if (chapterIndex == null) 20 else 10
        check(questions.size == expectedCount) {
            "Expected $expectedCount assessment questions for $id, got ${questions.size}."
        }
        assessmentId = id
        assessmentTitle = title
        assessmentQuestions = questions
        assessmentIndex = 0
        assessmentAnswer = null
        assessmentAnswers = List(questions.size) { null }
        assessmentScore = 0
        assessmentFinished = false
        assessmentPassed = false
        assessmentRewardMessage = ""
        screen = CodeQuestScreen.ASSESSMENT
    }

    MaterialTheme(
        colorScheme = if (darkMode) {
            darkColorScheme(
                primary = colors.accent,
                background = colors.background,
                surface = colors.surface,
                onBackground = colors.ink,
                onSurface = colors.ink
            )
        } else {
            lightColorScheme(
                primary = colors.accent,
                background = colors.background,
                surface = colors.surface,
                onBackground = colors.ink,
                onSurface = colors.ink
            )
        }
    ) {
        MainNavigation(
            selected = screen,
            colors = colors,
            onNavigate = { destination ->
                if (destination == CodeQuestScreen.HOME || destination == CodeQuestScreen.ROADMAP || destination == CodeQuestScreen.SETTINGS) {
                    screen = destination
                }
            }
        ) { padding ->
        Box(Modifier.padding(padding)) {
        when (screen) {
            CodeQuestScreen.NAME -> NameScreen(
                colors = colors,
                onContinue = { enteredName ->
                    name = enteredName.trim()
                    preferences.edit().putString("name", name).apply()
                    screen = CodeQuestScreen.COURSE_PICKER
                }
            )

            CodeQuestScreen.COURSE_PICKER -> CoursePickerScreen(
                colors = colors,
                onSelect = { language ->
                    selectedLanguage = language
                    preferences.edit().putString("course", language.key).apply()
                    screen = CodeQuestScreen.HOME
                }
            )

            CodeQuestScreen.HOME -> HomeScreen(
                name = name,
                language = requireNotNull(selectedLanguage),
                completedCount = lessons.count { it.id in completed },
                totalLessons = lessons.size,
                xp = xp,
                nextLesson = nextLesson(
                    lessons,
                    completed,
                    selectedLessonId,
                    requireNotNull(selectedLanguage),
                    passedAssessments
                ),
                colors = colors,
                onContinue = { lessonId ->
                    selectedLessonId = lessonId
                    preferences.edit().putString("last_lesson", lessonId).apply()
                    answer = ""
                    feedback = null
                    answerCorrect = false
                    showSolution = false
                    screen = CodeQuestScreen.LESSON
                },
                onRoadmap = { screen = CodeQuestScreen.ROADMAP },
                onChangeCourse = { screen = CodeQuestScreen.COURSE_PICKER }
            )

            CodeQuestScreen.ROADMAP -> RoadmapScreen(
                language = requireNotNull(selectedLanguage),
                lessons = lessons,
                completed = completed,
                passedAssessments = passedAssessments,
                colors = colors,
                onHome = { screen = CodeQuestScreen.HOME },
                onChangeCourse = { screen = CodeQuestScreen.COURSE_PICKER },
                onLesson = { lessonId ->
                    selectedLessonId = lessonId
                    preferences.edit().putString("last_lesson", lessonId).apply()
                    answer = ""
                    feedback = null
                    answerCorrect = false
                    showSolution = false
                    screen = CodeQuestScreen.LESSON
                },
                onChapterAssessment = { chapterIndex ->
                    val chapterTitle = lessons.map { it.chapter }.distinct()[chapterIndex]
                    startAssessment(
                        id = chapterAssessmentKey(requireNotNull(selectedLanguage), chapterIndex),
                        title = "$chapterTitle test",
                        chapterIndex = chapterIndex
                    )
                },
                onFinalAssessment = {
                    startAssessment(
                        id = finalAssessmentKey(requireNotNull(selectedLanguage)),
                        title = "Final exam",
                        chapterIndex = null
                    )
                }
            )

            CodeQuestScreen.SETTINGS -> SettingsScreen(
                name = name,
                language = requireNotNull(selectedLanguage),
                darkMode = darkMode,
                colors = colors,
                onSaveName = { updatedName ->
                    name = updatedName.trim()
                    preferences.edit().putString("name", name).apply()
                },
                onDarkModeChange = { enabled ->
                    darkMode = enabled
                    preferences.edit().putBoolean("dark_mode", enabled).apply()
                },
                onChangeCourse = { screen = CodeQuestScreen.COURSE_PICKER },
                onResetData = {
                    preferences.edit().clear().putBoolean("old_state_removed", true).apply()
                    name = ""
                    selectedLanguage = null
                    completed = emptySet()
                    passedAssessments = emptySet()
                    xp = 0
                    darkMode = false
                    selectedLessonId = ""
                    answer = ""
                    feedback = null
                    answerCorrect = false
                    showSolution = false
                    screen = CodeQuestScreen.NAME
                }
            )

            CodeQuestScreen.ASSESSMENT -> AssessmentScreen(
                title = assessmentTitle,
                questions = assessmentQuestions,
                questionIndex = assessmentIndex,
                selectedAnswer = assessmentAnswer,
                answers = assessmentAnswers,
                finished = assessmentFinished,
                score = assessmentScore,
                passed = assessmentPassed,
                rewardMessage = assessmentRewardMessage,
                colors = colors,
                onSelectAnswer = { selected -> if (assessmentAnswer == null) assessmentAnswer = selected },
                onNext = {
                    val question = assessmentQuestions.getOrNull(assessmentIndex)
                    val selected = assessmentAnswer
                    if (question != null && selected != null) {
                        val correctThisTime = selected == question.correctAnswer
                        assessmentAnswers = assessmentAnswers.toMutableList().also {
                            it[assessmentIndex] = selected
                        }
                        val nextScore = assessmentScore + if (correctThisTime) 1 else 0
                        assessmentScore = nextScore
                        if (assessmentIndex == assessmentQuestions.lastIndex) {
                            assessmentFinished = true
                            assessmentPassed = nextScore * 10 >= assessmentQuestions.size * 7
                            val preferencesKey = "assessment_best_$assessmentId"
                            val bestScore = maxOf(preferences.getInt(preferencesKey, 0), nextScore)
                            val editor = preferences.edit().putInt(preferencesKey, bestScore)
                            if (assessmentPassed) {
                                val alreadyPassed = assessmentId in passedAssessments
                                val perfectRewardKey = "assessment_perfect_reward_$assessmentId"
                                val perfectBonusAvailable = nextScore == assessmentQuestions.size &&
                                    !preferences.getBoolean(perfectRewardKey, false)
                                var earnedXp = 0
                                if (!alreadyPassed) {
                                    passedAssessments = passedAssessments + assessmentId
                                    earnedXp += 50
                                    editor.putStringSet("passed_assessments", passedAssessments)
                                }
                                if (perfectBonusAvailable) {
                                    earnedXp += 20
                                    editor.putBoolean(perfectRewardKey, true)
                                }
                                if (earnedXp > 0) {
                                    xp += earnedXp
                                    editor.putInt("xp", xp)
                                    assessmentRewardMessage = "+$earnedXp XP earned."
                                } else {
                                    assessmentRewardMessage = "Your assessment rewards are already saved."
                                }
                            } else {
                                assessmentRewardMessage = if (assessmentId in passedAssessments) {
                                    "Your earlier pass remains saved. This attempt adds no XP."
                                } else {
                                    "Your best result is saved on this device. No XP was added."
                                }
                            }
                            editor.apply()
                        } else {
                            assessmentIndex++
                            assessmentAnswer = null
                        }
                    }
                },
                onRestart = {
                    val chapterIndex = if (assessmentId.endsWith("-final")) null
                    else assessmentId.substringAfter("-chapter-").toInt()
                    startAssessment(assessmentId, assessmentTitle, chapterIndex)
                },
                onDone = { screen = CodeQuestScreen.ROADMAP }
            )

            CodeQuestScreen.LESSON -> {
                val lesson = requireNotNull(currentLesson)
                LessonScreen(
                    lesson = lesson,
                    lessonIndex = selectedIndex,
                    lessonCount = lessons.size,
                    completed = lesson.id in completed,
                    answer = answer,
                    feedback = feedback,
                    answerCorrect = answerCorrect,
                    showSolution = showSolution,
                    colors = colors,
                    onAnswerChange = { answer = it },
                    onCheck = {
                        val result = checkExerciseAnswer(lesson, answer)
                        feedback = result.feedback
                        answerCorrect = result.isCorrect
                        if (result.isCorrect && lesson.id !in completed) {
                            completed = completed + lesson.id
                            xp += 20
                            preferences.edit()
                                .putStringSet("completed", completed)
                                .putInt("xp", xp)
                                .apply()
                        }
                    },
                    onShowSolution = { showSolution = !showSolution },
                    onBack = { screen = CodeQuestScreen.ROADMAP },
                    onNext = {
                        if (selectedIndex < lessons.lastIndex) {
                            val next = lessons[selectedIndex + 1]
                            val chapters = lessons.map { it.chapter }.distinct()
                            val previousChapterIndex = chapters.indexOf(lesson.chapter)
                            val canContinue = next.chapter == lesson.chapter ||
                                chapterAssessmentKey(requireNotNull(selectedLanguage), previousChapterIndex) in passedAssessments
                            if (canContinue) {
                                selectedLessonId = next.id
                                preferences.edit().putString("last_lesson", selectedLessonId).apply()
                                answer = ""
                                feedback = null
                                answerCorrect = false
                                showSolution = false
                            } else {
                                screen = CodeQuestScreen.ROADMAP
                            }
                        } else {
                            screen = CodeQuestScreen.ROADMAP
                        }
                    }
                )
            }
        }
        }
        }
    }
}

private fun nextLesson(
    lessons: List<com.codequest.app.data.Lesson>,
    completed: Set<String>,
    lastLessonId: String,
    language: Language,
    passedAssessments: Set<String>
): String {
    val chapters = lessons.map { it.chapter }.distinct()
    fun isUnlocked(lesson: com.codequest.app.data.Lesson): Boolean {
        val chapterIndex = chapters.indexOf(lesson.chapter)
        return chapters.take(chapterIndex).indices.all {
            chapterAssessmentKey(language, it) in passedAssessments
        }
    }
    val lastIndex = lessons.indexOfFirst { it.id == lastLessonId }
    if (lastIndex >= 0 && lessons[lastIndex].id !in completed && isUnlocked(lessons[lastIndex])) {
        return lessons[lastIndex].id
    }
    return lessons.firstOrNull { it.id !in completed && isUnlocked(it) }?.id
        ?: lessons.asReversed().firstOrNull { it.id in completed && isUnlocked(it) }?.id
        ?: lessons.firstOrNull()?.id.orEmpty()
}
