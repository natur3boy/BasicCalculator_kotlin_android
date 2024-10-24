package com.example.basiccal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.basiccal.ui.theme.BasicCalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BasicCalTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CalculatorUI()
                }
            }
        }
    }
}

@Composable
fun CalculatorUI() {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display
        Text(
            text = if (result.isEmpty()) input else result,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.End)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Calculator Buttons
        Column {
            val buttons = listOf(
                listOf("7", "8", "9", "/"),
                listOf("4", "5", "6", "*"),
                listOf("1", "2", "3", "-"),
                listOf("C", "0", "=", "+")
            )

            for (row in buttons) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (label in row) {
                        Button(
                            onClick = { onButtonClick(label, ::calculateResult, input, { input = it }, { result = it }) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp)
                        ) {
                            Text(label, fontSize = 24.sp)
                        }
                    }
                }
            }
        }
    }
}

fun onButtonClick(
    label: String,
    calculateResult: (String) -> String,
    currentInput: String,
    updateInput: (String) -> Unit,
    updateResult: (String) -> Unit
) {
    when (label) {
        "=" -> {
            try {
                val calculationResult = calculateResult(currentInput)
                updateResult(calculationResult)
            } catch (e: ArithmeticException) {
                updateResult("Error")
            }
        }
        "C" -> {
            updateInput("")
            updateResult("")
        }
        else -> {
            updateInput(currentInput + label)
            updateResult("")
        }
    }
}

// Basic Calculation Logic
fun calculateResult(input: String): String {
    return try {
        val result = evaluateExpression(input)
        result.toString()
    } catch (e: Exception) {
        "Error"
    }
}

// Simple custom expression evaluator using MutableList instead of Stack
fun evaluateExpression(expression: String): Double {
    val tokens = expression.toCharArray()
    val values = mutableListOf<Double>()
    val operators = mutableListOf<Char>()

    var i = 0
    while (i < tokens.size) {
        if (tokens[i] == ' ') {
            i++
            continue
        }

        if (tokens[i] in '0'..'9') {
            val sb = StringBuilder()
            while (i < tokens.size && tokens[i] in '0'..'9') {
                sb.append(tokens[i++])
            }
            values.add(sb.toString().toDouble())
            i--
        } else if (tokens[i] == '(') {
            operators.add(tokens[i])
        } else if (tokens[i] == ')') {
            while (operators.last() != '(') {
                values.add(applyOp(operators.removeAt(operators.lastIndex), values.removeAt(values.lastIndex), values.removeAt(values.lastIndex)))
            }
            operators.removeAt(operators.lastIndex)
        } else if (tokens[i] == '+' || tokens[i] == '-' || tokens[i] == '*' || tokens[i] == '/') {
            while (operators.isNotEmpty() && hasPrecedence(tokens[i], operators.last())) {
                values.add(applyOp(operators.removeAt(operators.lastIndex), values.removeAt(values.lastIndex), values.removeAt(values.lastIndex)))
            }
            operators.add(tokens[i])
        }
        i++
    }

    while (operators.isNotEmpty()) {
        values.add(applyOp(operators.removeAt(operators.lastIndex), values.removeAt(values.lastIndex), values.removeAt(values.lastIndex)))
    }

    return values.last()
}

fun hasPrecedence(op1: Char, op2: Char): Boolean {
    if (op2 == '(' || op2 == ')') {
        return false
    }
    return !(op1 == '*' || op1 == '/') || (op2 != '+' && op2 != '-')
}

fun applyOp(op: Char, b: Double, a: Double): Double {
    return when (op) {
        '+' -> a + b
        '-' -> a - b
        '*' -> a * b
        '/' -> {
            if (b == 0.0) throw ArithmeticException("Cannot divide by zero")
            a / b
        }
        else -> 0.0
    }
}

@Preview(showBackground = true)
@Composable
fun Basic() {
    BasicCalTheme {
        CalculatorUI()
    }
}
