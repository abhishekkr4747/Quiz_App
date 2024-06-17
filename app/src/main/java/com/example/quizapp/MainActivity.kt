package com.example.quizapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quizapp.Widgets.QuestionCard
import com.example.quizapp.ui.theme.QuizAppTheme
import com.example.quizapp.viewModel.QuizViewModel
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    private val quizViewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizAppTheme {
                QuizApp(quizViewModel)
            }
        }
        Log.d("MainActivity", "onCreate called")
        quizViewModel.loadState()
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "onPause called")
        quizViewModel.saveState()
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume called")
        quizViewModel.loadState()
    }
}

@Composable
fun QuizApp(quizViewModel: QuizViewModel) {
    val questions by quizViewModel.questions
    val currentQuestionIndex by quizViewModel.currentQuestionIndex
    val timer by quizViewModel.timer
    val isQuizFinished by quizViewModel.isQuizFinished
    var userName by quizViewModel.userName
    var isQuizStarted by quizViewModel.isQuizStarted
    var selectedOption by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isQuizStarted) {
        if (isQuizStarted) {
            while (timer > 0 && !isQuizFinished) {
                delay(1000L)
                quizViewModel.timer.value -= 1
            }
            if (timer == 0) {
                quizViewModel.isQuizFinished.value = true
            }
        }
    }
    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()
        .background(color = Color.White),
        verticalArrangement = Arrangement.Center
    ) {
        if (isQuizStarted) {
            if (isQuizFinished) {
                val score = quizViewModel.calculateScore()

                Text(text = "Hey, Congratulations!", fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = userName, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "Quiz Finished! Your score is $score/${questions.size}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        quizViewModel.resetQuiz()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2100a6)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "FINISH", fontSize = 16.sp)
                }
            } else {
                if (questions.isNotEmpty()) {
                    val question = questions[currentQuestionIndex]
                    val progressValue = (currentQuestionIndex + 1) / 10f

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "Question",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${currentQuestionIndex + 1}/10",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        CircularTimer(timer = timer, totalTime = 600)

                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = {  progressValue },
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF2100a6)
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    QuestionCard(question.question)

                    Spacer(modifier = Modifier.height(25.dp))

                    question.options.forEach { option ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = if (selectedOption == option) CardDefaults.cardColors(containerColor = Color(0xFFe4e0f4)) else CardDefaults.cardColors(containerColor = Color(0xFFf5f5f5)),
                            border = if (selectedOption == option) BorderStroke(1.dp, Color(0xFF543bbb)) else BorderStroke(0.dp, Color(0xFFf5f5f5))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clickable { selectedOption = option },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedOption == option,
                                    onClick = { selectedOption = option }
                                )
                                Text(text = option, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            quizViewModel.submitAnswer(selectedOption)
                            quizViewModel.nextQuestion()
                            selectedOption = null
                        },
                        enabled = selectedOption != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2100a6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Next", fontSize = 16.sp)
                    }
                }
            }

        } else {
            Text(
                text = "Quiz App!",
                fontSize = 32.sp,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFe4e0f4)),
                border = BorderStroke(2.dp, Color(0xFF543bbb))
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Welcome",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Please enter your name",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it},
                        placeholder = { Text(text = "e.g. SmartHire") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                        )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            isQuizStarted = true
                        },
                        enabled = userName.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2100a6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "START", fontSize = 16.sp)
                    }
                }
            }
        }

    }
}

@Composable
fun CircularTimer(
    timer: Int,
    totalTime: Int,
    modifier: Modifier = Modifier
) {
    val progress = (timer / totalTime.toFloat())
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.size(80.dp),
            color = Color(0xFF2100a6),
            strokeWidth = 6.dp,
            trackColor = Color(0xFFe3dff3)
        )
        Text(
            text = String.format("%02d:%02d", timer / 60, timer % 60),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}



