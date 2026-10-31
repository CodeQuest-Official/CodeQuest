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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codequest.app.data.AssessmentQuestion

@Composable
internal fun AssessmentScreen(
    title: String,
    questions: List<AssessmentQuestion>,
    questionIndex: Int,
    selectedAnswer: Int?,
    answers: List<Int?>,
    finished: Boolean,
    score: Int,
    passed: Boolean,
    rewardMessage: String,
    colors: AppPalette,
    onSelectAnswer: (Int) -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit,
    onDone: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
        Spacer(Modifier.height(18.dp))
        Text("← Learning path", color = colors.accent, fontSize = 14.sp, modifier = Modifier.clickable(onClick = onDone).padding(vertical = 8.dp))
        if (finished) {
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(if (passed) "Assessment passed" else "Keep practicing", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                Spacer(Modifier.height(10.dp))
                Text(title, color = colors.ink, fontSize = 27.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))
                Text("$score / ${questions.size} correct", color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(7.dp))
                LinearProgressIndicator(
                    progress = { score.toFloat() / questions.size.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    color = if (passed) colors.success else colors.accent,
                    trackColor = colors.accentWash
                )
                Spacer(Modifier.height(14.dp))
                Text(if (passed) "You need 70% to pass. Nice work." else "You need 70% to pass. Review the lessons and try again.", color = colors.muted, fontSize = 14.sp, lineHeight = 21.sp)
                if (rewardMessage.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(rewardMessage, color = colors.success, fontSize = 13.sp, lineHeight = 20.sp)
                }
                Spacer(Modifier.height(24.dp))
                questions.forEachIndexed { index, question ->
                    Text("${index + 1}. ${question.prompt}", color = colors.ink, fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
                    val userAnswer = answers.getOrNull(index)?.let(question.options::getOrNull)
                    val correctAnswer = question.options.getOrNull(question.correctAnswer).orEmpty()
                    Text(
                        buildString {
                            append("Your answer: ")
                            append(userAnswer ?: "Not answered")
                            if (userAnswer != correctAnswer) {
                                append("\nCorrect answer: ")
                                append(correctAnswer)
                            }
                            append("\n")
                            append(question.explanation)
                        },
                        color = colors.muted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onRestart, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text(if (passed) "Practice again" else "Try again")
                }
                Spacer(Modifier.height(9.dp))
                Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(49.dp), colors = ButtonDefaults.buttonColors(containerColor = colors.accent)) {
                    Text("Back to learning path")
                }
                Spacer(Modifier.height(22.dp))
            }
        } else {
            val question = questions.getOrNull(questionIndex)
            if (question == null) {
                Text("This assessment is not available yet.", color = colors.ink)
            } else {
                Text(title, color = colors.ink, fontSize = 23.sp, lineHeight = 29.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(13.dp))
                LinearProgressIndicator(
                    progress = { (questionIndex + 1f) / questions.size.coerceAtLeast(1) },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                    color = colors.accent,
                    trackColor = colors.accentWash
                )
                Text("QUESTION ${questionIndex + 1} OF ${questions.size}", color = colors.muted, fontSize = 10.sp, letterSpacing = 1.sp, modifier = Modifier.padding(top = 10.dp))
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
                    Text(question.prompt, color = colors.ink, fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(19.dp))
                    question.options.forEachIndexed { index, option ->
                        val isChosen = selectedAnswer == index
                        val isCorrect = selectedAnswer != null && index == question.correctAnswer
                        val background = when {
                            isCorrect -> colors.success.copy(alpha = .14f)
                            isChosen -> colors.accentWash
                            else -> colors.surface
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 9.dp).clip(RoundedCornerShape(10.dp))
                                .background(background)
                                .clickable(enabled = selectedAnswer == null) { onSelectAnswer(index) }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(22.dp).clip(CircleShape)
                                    .background(if (isChosen || isCorrect) colors.accent else colors.background),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (selectedAnswer != null && isCorrect) "✓" else ('A' + index).toString(),
                                    color = if (isChosen || isCorrect) Color.White else colors.muted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(option, color = colors.ink, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.padding(start = 11.dp))
                        }
                    }
                    if (selectedAnswer != null) {
                        val answeredCorrectly = selectedAnswer == question.correctAnswer
                        Text(
                            (if (answeredCorrectly) "That's right. " else "Not quite. ") + question.explanation,
                            color = if (answeredCorrectly) colors.success else colors.muted,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 5.dp, bottom = 12.dp)
                        )
                    }
                }
                Button(
                    onClick = onNext,
                    enabled = selectedAnswer != null,
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 18.dp).height(49.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) { Text(if (questionIndex == questions.lastIndex) "See results" else "Next question") }
            }
        }
    }
}
