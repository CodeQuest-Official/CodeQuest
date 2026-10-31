package com.codequest.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codequest.app.data.ExerciseMode
import com.codequest.app.data.Language
import com.codequest.app.data.Lesson
import com.codequest.app.data.chapterAssessmentKey
import com.codequest.app.data.finalAssessmentKey
import com.codequest.app.data.englishLessonsFor
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal enum class CodeQuestScreen { NAME, COURSE_PICKER, HOME, ROADMAP, SETTINGS, LESSON, ASSESSMENT }

internal data class AppPalette(
    val background: Color = Color(0xFFF5F5F2),
    val surface: Color = Color.White,
    val ink: Color = Color(0xFF20272D),
    val muted: Color = Color(0xFF68737D),
    val accent: Color = Color(0xFF395F91),
    val accentWash: Color = Color(0xFFE7EDF4),
    val success: Color = Color(0xFF3D795C),
    val codeBackground: Color = Color(0xFF18212B),
    val codeText: Color = Color(0xFFE7EEF5)
)

internal fun appPalette(darkMode: Boolean): AppPalette = if (darkMode) {
    AppPalette(
        background = Color(0xFF111820),
        surface = Color(0xFF1D2833),
        ink = Color(0xFFE8EDF3),
        muted = Color(0xFFA8B5C4),
        accent = Color(0xFF91B7E5),
        accentWash = Color(0xFF29394A),
        success = Color(0xFF8BC99D),
        codeBackground = Color(0xFF182330),
        codeText = Color(0xFFE0E9F2)
    )
} else {
    AppPalette()
}

@Composable
internal fun MainNavigation(
    selected: CodeQuestScreen,
    colors: AppPalette,
    onNavigate: (CodeQuestScreen) -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    Scaffold(
        containerColor = colors.background,
        bottomBar = {
            if (selected == CodeQuestScreen.HOME || selected == CodeQuestScreen.ROADMAP || selected == CodeQuestScreen.SETTINGS) {
                NavigationBar(containerColor = colors.surface) {
                    listOf(
                        Triple(CodeQuestScreen.HOME, "⌂", "Home"),
                        Triple(CodeQuestScreen.ROADMAP, "↗", "Learning path"),
                        Triple(CodeQuestScreen.SETTINGS, "⋯", "Settings")
                    ).forEach { (destination, mark, label) ->
                        NavigationBarItem(
                            selected = selected == destination,
                            onClick = { onNavigate(destination) },
                            icon = { Text(mark, fontSize = 20.sp) },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    ) { padding -> content(padding) }
}

@Composable
internal fun NameScreen(colors: AppPalette, onContinue: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val nameScrollState = rememberScrollState()
    val nameBringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(nameScrollState).padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(
                if (colors.background.red < .2f) {
                    com.codequest.app.R.drawable.codequest_logo_dark
                } else {
                    com.codequest.app.R.drawable.codequest_logo
                }
            ),
            contentDescription = "CodeQuest logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().widthIn(max = 400.dp).aspectRatio(972f / 553f)
        )
        Spacer(Modifier.height(20.dp))
        Text("A good place to start.", color = colors.ink, fontSize = 31.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Text("Learn to code one small step at a time. First, what should we call you?", color = colors.muted, fontSize = 16.sp, lineHeight = 24.sp)
        Spacer(Modifier.height(25.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(nameBringIntoViewRequester)
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        coroutineScope.launch {
                            delay(300)
                            nameBringIntoViewRequester.bringIntoView()
                        }
                    }
                },
            singleLine = true,
            label = { Text("Your name") },
            placeholder = { Text("For example, Alex") },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                if (name.isNotBlank()) onContinue(name)
            })
        )
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = { onContinue(name) },
            enabled = name.trim().isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
        ) { Text("Continue") }
        Spacer(Modifier.height(14.dp))
        Text("Your name and learning progress stay on this device.", color = colors.muted, fontSize = 12.sp)
    }
}

@Composable
internal fun CoursePickerScreen(colors: AppPalette, onSelect: (Language) -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Eyebrow("CHOOSE YOUR PATH", colors)
        Spacer(Modifier.height(10.dp))
        Text("What would you like to learn?", color = colors.ink, fontSize = 28.sp, lineHeight = 35.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("You can explore the other course later.", color = colors.muted, fontSize = 15.sp)
        Spacer(Modifier.height(23.dp))
        CourseOption(
            title = "Python",
            detail = "A friendly first language for learning the basics.",
            tag = "41 LESSONS",
            colors = colors,
            enabled = true,
            onClick = { onSelect(Language.PYTHON) }
        )
        Spacer(Modifier.height(10.dp))
        CourseOption(
            title = "JavaScript",
            detail = "Learn the language that makes websites interactive.",
            tag = "41 LESSONS",
            colors = colors,
            enabled = true,
            onClick = { onSelect(Language.JAVASCRIPT) }
        )
        Spacer(Modifier.height(10.dp))
        CourseOption(
            title = "PHP",
            detail = "This course is being prepared for a future update.",
            tag = "COMING SOON",
            colors = colors,
            enabled = false,
            onClick = {}
        )
    }
}

@Composable
private fun CourseOption(
    title: String,
    detail: String,
    tag: String,
    colors: AppPalette,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (enabled) colors.surface else colors.background)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, color = if (enabled) colors.ink else colors.muted, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Text(detail, color = colors.muted, fontSize = 13.sp, lineHeight = 19.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(tag, color = if (enabled) colors.accent else colors.muted, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .7.sp)
        }
    }
}

@Composable
internal fun HomeScreen(
    name: String,
    language: Language,
    completedCount: Int,
    totalLessons: Int,
    xp: Int,
    nextLesson: String,
    colors: AppPalette,
    onContinue: (String) -> Unit,
    onRoadmap: () -> Unit,
    onChangeCourse: () -> Unit
) {
    val courseLessons = englishLessonsFor(language)
    val lesson = courseLessons.firstOrNull { it.id == nextLesson } ?: courseLessons.first()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(24.dp))
        Eyebrow("YOUR LEARNING SPACE", colors)
        Spacer(Modifier.height(6.dp))
        Text("Hello, $name.", color = colors.ink, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric("LESSONS", "$completedCount / $totalLessons", colors, Modifier.weight(1f))
            Metric("XP EARNED", xp.toString(), colors, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(
            progress = { if (totalLessons == 0) 0f else completedCount.toFloat() / totalLessons },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
            color = colors.accent,
            trackColor = colors.accentWash
        )
        Spacer(Modifier.height(26.dp))
        Text("Your course", color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(language.label, color = colors.ink, fontSize = 15.sp)
            Spacer(Modifier.weight(1f))
            Text("Change course", color = colors.accent, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onChangeCourse).padding(8.dp))
        }
        Spacer(Modifier.height(24.dp))
        Card(
            shape = RoundedCornerShape(15.dp),
            colors = CardDefaults.cardColors(containerColor = colors.accent)
        ) {
            Column(Modifier.fillMaxWidth().padding(19.dp)) {
                Text("PICK UP WHERE YOU LEFT OFF", color = Color.White.copy(alpha = .8f), fontSize = 10.sp, letterSpacing = 1.sp)
                Spacer(Modifier.height(9.dp))
                Text(lesson.title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text(lesson.chapter, color = Color.White.copy(alpha = .8f), fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp))
                Spacer(Modifier.height(15.dp))
                OutlinedButton(onClick = { onContinue(lesson.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Continue lesson", color = Color.White)
                }
            }
        }
        Spacer(Modifier.height(13.dp))
        OutlinedButton(onClick = onRoadmap, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("View learning path")
        }
        Spacer(Modifier.height(26.dp))
        Text("Small steps add up. Learn at your own pace.", color = colors.muted, fontSize = 13.sp)
        Spacer(Modifier.height(25.dp))
    }
}

@Composable
private fun Metric(label: String, value: String, colors: AppPalette, modifier: Modifier) {
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(colors.surface).padding(14.dp)) {
        Text(label, color = colors.muted, fontSize = 10.sp, letterSpacing = .8.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, color = colors.ink, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun RoadmapScreen(
    language: Language,
    lessons: List<Lesson>,
    completed: Set<String>,
    passedAssessments: Set<String>,
    colors: AppPalette,
    onHome: () -> Unit,
    onChangeCourse: () -> Unit,
    onLesson: (String) -> Unit,
    onChapterAssessment: (Int) -> Unit,
    onFinalAssessment: () -> Unit
) {
    val firstIncomplete = lessons.indexOfFirst { it.id !in completed }.let { if (it < 0) lessons.size else it }
    val chapters = lessons.map { it.chapter }.distinct()
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
            Spacer(Modifier.height(18.dp))
            Text("← Home", color = colors.accent, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onHome).padding(bottom = 12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Eyebrow(language.label.uppercase(Locale.ROOT) + " COURSE", colors)
                    Text("Learning path", color = colors.ink, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
                }
                Text("Change", color = colors.accent, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onChangeCourse).padding(8.dp))
            }
            Spacer(Modifier.height(16.dp))
            chapters.forEachIndexed { chapterIndex, chapterTitle ->
                val chapterLessons = lessons.filter { it.chapter == chapterTitle }
                val previousTestsPassed = chapters.take(chapterIndex).all {
                    chapterAssessmentKey(language, chapters.indexOf(it)) in passedAssessments
                }
                Text(chapterTitle.uppercase(Locale.ROOT), color = colors.muted, fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(7.dp))
                chapterLessons.forEach { rawLesson ->
                    val index = lessons.indexOfFirst { it.id == rawLesson.id }
                    val done = rawLesson.id in completed
                    val unlocked = done || (index <= firstIncomplete && previousTestsPassed)
                    RoadmapItem(
                        title = rawLesson.title,
                        status = when {
                            done -> "Completed"
                            index == firstIncomplete -> "Up next"
                            unlocked -> "Ready"
                            else -> "Locked"
                        },
                        complete = done,
                        current = index == firstIncomplete,
                        enabled = unlocked,
                        colors = colors,
                        onClick = { onLesson(rawLesson.id) }
                    )
                }
                val assessmentKey = chapterAssessmentKey(language, chapterIndex)
                val chapterLessonsDone = chapterLessons.all { it.id in completed }
                val assessmentPassed = assessmentKey in passedAssessments
                AssessmentRoadmapItem(
                    title = "Chapter test",
                    detail = when {
                        assessmentPassed -> "Passed · 10 questions"
                        chapterLessonsDone && previousTestsPassed -> "10 questions · 70% to pass"
                        else -> "Complete this chapter to unlock"
                    },
                    unlocked = chapterLessonsDone && previousTestsPassed,
                    complete = assessmentPassed,
                    colors = colors,
                    onClick = { onChapterAssessment(chapterIndex) }
                )
                Spacer(Modifier.height(10.dp))
            }
            val finalUnlocked = lessons.all { it.id in completed } &&
                chapters.indices.all { chapterAssessmentKey(language, it) in passedAssessments }
            AssessmentRoadmapItem(
                title = "Final exam",
                detail = if (finalAssessmentKey(language) in passedAssessments) "Passed · 20 questions" else "20 questions · 70% to pass",
                unlocked = finalUnlocked,
                complete = finalAssessmentKey(language) in passedAssessments,
                final = true,
                colors = colors,
                onClick = onFinalAssessment
            )
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun AssessmentRoadmapItem(
    title: String,
    detail: String,
    unlocked: Boolean,
    complete: Boolean,
    final: Boolean = false,
    colors: AppPalette,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 66.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.padding(start = 2.dp).size(if (final) 27.dp else 22.dp)
                .clip(if (final) RoundedCornerShape(6.dp) else CircleShape)
                .background(if (complete) colors.success else if (unlocked) colors.accent else colors.accentWash)
                .clickable(enabled = unlocked, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(if (complete) "✓" else if (final) "★" else "Q", color = if (unlocked) Color.White else colors.muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.weight(1f).padding(start = 15.dp).clip(RoundedCornerShape(9.dp))
                .clickable(enabled = unlocked, onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(title, color = if (unlocked) colors.ink else colors.muted.copy(alpha = .6f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = if (complete) colors.success else colors.muted, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun RoadmapItem(
    title: String,
    status: String,
    complete: Boolean,
    current: Boolean,
    enabled: Boolean,
    colors: AppPalette,
    onClick: () -> Unit
) {
    Row(Modifier.fillMaxWidth().heightIn(min = 60.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.width(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(if (current) 22.dp else 17.dp).clip(CircleShape)
                    .background(if (complete) colors.success else if (current) colors.accent else colors.accentWash),
                contentAlignment = Alignment.Center
            ) {
                if (complete) Text("✓", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Box(Modifier.width(2.dp).height(35.dp).background(colors.accentWash))
        }
        Spacer(Modifier.width(9.dp))
        Column(
            Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                .background(if (current) colors.accentWash else Color.Transparent)
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 11.dp, vertical = 9.dp)
        ) {
            Text(title, color = if (enabled) colors.ink else colors.muted.copy(alpha = .55f), fontSize = 14.sp,
                fontWeight = if (current) FontWeight.SemiBold else FontWeight.Medium)
            Text(status, color = if (complete) colors.success else colors.muted, fontSize = 11.sp)
        }
    }
}

@Composable
internal fun LessonScreen(
    lesson: Lesson,
    lessonIndex: Int,
    lessonCount: Int,
    completed: Boolean,
    answer: String,
    feedback: String?,
    answerCorrect: Boolean,
    showSolution: Boolean,
    colors: AppPalette,
    onAnswerChange: (String) -> Unit,
    onCheck: () -> Unit,
    onShowSolution: () -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val lessonScrollState = remember { ScrollState(0) }
    val answerBringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(lesson.id) {
        lessonScrollState.scrollTo(0)
    }
    Column(Modifier.fillMaxSize().imePadding()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("← Learning path", color = colors.accent, modifier = Modifier.clickable(onClick = onBack))
            Text("${lessonIndex + 1} / $lessonCount", color = colors.muted, fontSize = 12.sp)
        }
        LinearProgressIndicator(
            progress = { (lessonIndex + 1f) / lessonCount.coerceAtLeast(1) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(4.dp),
            color = colors.accent,
            trackColor = colors.accentWash
        )
        Column(Modifier.weight(1f).verticalScroll(lessonScrollState).padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(19.dp))
            Text(lesson.chapter.uppercase(Locale.ROOT), color = colors.accent, fontSize = 10.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(5.dp))
            Text(lesson.title, color = colors.ink, fontSize = 26.sp, lineHeight = 33.sp, fontWeight = FontWeight.SemiBold)
            Text(lesson.explanation, color = colors.ink, fontSize = 15.sp, lineHeight = 23.sp, modifier = Modifier.padding(top = 10.dp))
            Spacer(Modifier.height(16.dp))
            Text("Example", color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            CodeBlock(lesson.language.fileExtension, lesson.example, colors)
            Text(lesson.breakdown, color = colors.muted, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 9.dp))
            Spacer(Modifier.height(20.dp))
            Text("Your turn", color = colors.ink, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Text(lesson.exercise, color = colors.ink, fontSize = 14.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 6.dp))
            if (lesson.mode == ExerciseMode.ORDER) {
                Text("Put these lines in the right order:", color = colors.muted, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp, bottom = 5.dp))
                CodeBlock(lesson.language.fileExtension, lesson.solution.lines().asReversed().joinToString("\n"), colors)
            }
            Spacer(Modifier.height(9.dp))
            OutlinedTextField(
                value = answer,
                onValueChange = onAnswerChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
                    .bringIntoViewRequester(answerBringIntoViewRequester)
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            coroutineScope.launch {
                                delay(300)
                                answerBringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
                placeholder = { Text("Write your code here…", color = Color(0xFF9AABB9)) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, color = colors.codeText, lineHeight = 21.sp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                shape = RoundedCornerShape(11.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colors.codeBackground,
                    unfocusedContainerColor = colors.codeBackground,
                    focusedTextColor = colors.codeText,
                    unfocusedTextColor = colors.codeText,
                    focusedIndicatorColor = colors.accent,
                    unfocusedIndicatorColor = colors.codeBackground,
                    cursorColor = Color.White
                )
            )
            Text("Checked on this device with simple rules. Your code is not compiled or run.", color = colors.muted, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 9.dp)) {
                OutlinedButton(onClick = { onAnswerChange("") }, modifier = Modifier.weight(1f)) { Text("Clear") }
                OutlinedButton(onClick = onShowSolution, modifier = Modifier.weight(1f)) { Text(if (showSolution) "Hide answer" else "Show answer") }
            }
            if (showSolution) {
                Spacer(Modifier.height(7.dp))
                CodeBlock(lesson.language.fileExtension, lesson.solution, colors)
            }
            Button(
                onClick = onCheck,
                enabled = answer.isNotBlank() && !completed,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(49.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) { Text(if (completed) "Lesson completed" else "Check my answer") }
            feedback?.let {
                Text(it, color = if (answerCorrect) colors.success else colors.muted, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = onNext,
                enabled = completed,
                modifier = Modifier.fillMaxWidth().height(49.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
            ) { Text(if (lessonIndex == lessonCount - 1) "Finish course" else "Next lesson  →") }
            Spacer(Modifier.height(22.dp))
        }
    }
}

@Composable
internal fun SettingsScreen(
    name: String,
    language: Language,
    darkMode: Boolean,
    colors: AppPalette,
    onSaveName: (String) -> Unit,
    onDarkModeChange: (Boolean) -> Unit,
    onChangeCourse: () -> Unit,
    onResetData: () -> Unit
) {
    var editedName by remember(name) { mutableStateOf(name) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(24.dp))
        Eyebrow("PREFERENCES & INFORMATION", colors)
        Spacer(Modifier.height(6.dp))
        Text("Settings", color = colors.ink, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(22.dp))

        Text("YOUR PROFILE", color = colors.muted, fontSize = 10.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = editedName,
            onValueChange = { editedName = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Your name") },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
        )
        Button(
            onClick = { onSaveName(editedName) },
            enabled = editedName.trim().isNotEmpty() && editedName.trim() != name,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
        ) { Text("Save name") }

        Spacer(Modifier.height(23.dp))
        androidx.compose.material3.HorizontalDivider(color = colors.accentWash)
        Spacer(Modifier.height(20.dp))
        Text("APPEARANCE", color = colors.muted, fontSize = 10.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.SemiBold)
        Row(
            Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(if (darkMode) "Dark mode" else "Light mode", color = colors.ink, fontWeight = FontWeight.Medium)
                Text("Choose the appearance used throughout the app.", color = colors.muted, fontSize = 12.sp, lineHeight = 18.sp)
            }
            Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
        }

        Spacer(Modifier.height(20.dp))
        androidx.compose.material3.HorizontalDivider(color = colors.accentWash)
        Spacer(Modifier.height(20.dp))
        Text("LEARNING", color = colors.muted, fontSize = 10.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Current course", color = colors.ink, fontWeight = FontWeight.Medium)
                Text(language.label, color = colors.muted, fontSize = 13.sp, modifier = Modifier.padding(top = 3.dp))
            }
            OutlinedButton(onClick = onChangeCourse, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Change")
            }
        }

        Spacer(Modifier.height(22.dp))
        androidx.compose.material3.HorizontalDivider(color = colors.accentWash)
        Spacer(Modifier.height(20.dp))
        Text("PRIVACY", color = colors.muted, fontSize = 10.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("CodeQuest works offline. It has no account, backend, database, ads, analytics, or network connection.", color = colors.ink, fontSize = 14.sp, lineHeight = 21.sp)
        Text("Your name, chosen course, lesson progress, and XP are saved only in private app storage on this phone so you can continue later. They are not sent to anyone and are never used for AI training.", color = colors.muted, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 8.dp))
        Text("Your code answers are checked on this device and are not compiled, executed, or uploaded.", color = colors.muted, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 8.dp))
        Text("You can erase all locally saved app data below. Uninstalling the app also removes its private app data.", color = colors.muted, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 8.dp))
        OutlinedButton(onClick = { showResetConfirmation = true }, modifier = Modifier.fillMaxWidth().padding(top = 9.dp).heightIn(min = 48.dp)) {
            Text("Delete local app data")
        }

        Spacer(Modifier.height(22.dp))
        androidx.compose.material3.HorizontalDivider(color = colors.accentWash)
        Spacer(Modifier.height(20.dp))
        Text("LEGAL INFORMATION", color = colors.muted, fontSize = 10.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text("About CodeQuest", color = colors.ink, fontWeight = FontWeight.Medium)
        Text("An independent, offline learning app for beginners. Lessons are educational examples and do not execute code.", color = colors.muted, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.padding(top = 5.dp))
        Text("Publisher contact: Glitch3.SeCU@proton.me", color = colors.ink, fontSize = 13.sp, modifier = Modifier.padding(top = 9.dp))
        Text("Designed for beginners of all ages. Text follows your Android system font-size setting.", color = colors.muted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 9.dp))
        Spacer(Modifier.height(25.dp))
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetConfirmation = false },
            title = { Text("Delete local app data?") },
            text = { Text("This removes your saved name, course choice, progress, and XP from this phone. You will start again from the welcome screen.") },
            confirmButton = {
                TextButton(onClick = {
                    showResetConfirmation = false
                    onResetData()
                }) { Text("Delete data") }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmation = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun CodeBlock(language: String, code: String, colors: AppPalette) {
    Column(
        Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(10.dp))
            .background(colors.codeBackground).padding(13.dp)
    ) {
        Text("example.$language", color = Color(0xFF9AABB9), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Spacer(Modifier.height(7.dp))
        Text(code, color = colors.codeText, fontSize = 13.sp, lineHeight = 20.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun Eyebrow(value: String, colors: AppPalette) {
    Text(value, color = colors.accent, fontSize = 10.sp, letterSpacing = 1.4.sp, fontWeight = FontWeight.SemiBold)
}
