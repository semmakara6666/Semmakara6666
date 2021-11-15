package com.ivy.wallet.ui.theme.modal.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ivy.wallet.base.*
import com.ivy.wallet.ui.IvyAppPreview
import com.ivy.wallet.ui.theme.*
import com.ivy.wallet.ui.theme.modal.IvyModal
import com.ivy.wallet.ui.theme.modal.ModalSet
import com.ivy.wallet.ui.theme.modal.ModalTitle
import com.notkamui.keval.Keval
import java.util.*

@Composable
fun BoxWithConstraintsScope.CalculatorModal(
    id: UUID = UUID.randomUUID(),
    initialAmount: Double?,
    visible: Boolean,
    currency: String,

    dismiss: () -> Unit,
    onCalculation: (Double) -> Unit
) {
    var expression by remember(id, initialAmount) {
        mutableStateOf(initialAmount?.format(currency) ?: "")
    }

    IvyModal(
        id = id,
        visible = visible,
        dismiss = dismiss,
        PrimaryAction = {
            ModalSet {
                val result = calculate(expression)
                if (result != null) {
                    onCalculation(result)
                    dismiss()
                }
            }
        }
    ) {
        Spacer(Modifier.height(32.dp))

        ModalTitle(text = "Calculator")

        Spacer(Modifier.height(32.dp))

        val isEmpty = expression.isBlank()
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            text = if (isEmpty) "Calculation (+-/*=)" else expression,
            style = Typo.numberH2.style(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isEmpty) Gray else IvyTheme.colors.pureInverse
            )
        )

        Spacer(Modifier.height(32.dp))

        AmountKeyboard(
            ZeroRow = {
                KeypadCircleButton(
                    text = "C",
                    textColor = Red
                ) {
                    expression = ""
                }

                Spacer(Modifier.width(16.dp))

                KeypadCircleButton(
                    text = "(",
                ) {
                    expression += "("
                }

                Spacer(Modifier.width(16.dp))

                KeypadCircleButton(
                    text = ")",
                ) {
                    expression += ")"
                }

                Spacer(Modifier.width(16.dp))

                KeypadCircleButton(
                    text = "/",
                ) {
                    expression += "/"
                }
            },
            FirstRowExtra = {
                KeypadCircleButton(text = "*") {
                    expression += "*"
                }
            },
            SecondRowExtra = {
                KeypadCircleButton(text = "-") {
                    expression += "-"
                }
            },
            ThirdRowExtra = {
                KeypadCircleButton(text = "+") {
                    expression += "+"
                }
            },
            FourthRowExtra = {
                KeypadCircleButton(text = "=") {
                    val result = calculate(expression)
                    if (result != null) {
                        expression = result.format(currency)
                    }
                }
            },

            onNumberPressed = {
                expression = formatExpression(
                    expression = expression + it,
                    currency = currency
                )
            },
            onDecimalPoint = {
                expression = formatExpression(
                    expression = expression + localDecimalSeparator(),
                    currency = currency
                )
            },
            onBackspace = {
                if (expression.isNotEmpty()) {
                    expression = expression.dropLast(1)
                }
            }
        )

        Spacer(Modifier.height(24.dp))
    }
}

private fun formatExpression(expression: String, currency: String): String {
    var formattedExpression = expression

    expression
        .split("(", ")", "/", "*", "-", "+")
        .ifEmpty {
            //handle only number expression formatting
            listOf(expression)
        }
        .forEach { part ->
            val numberPart = part.amountToDoubleOrNull()
            if (numberPart != null) {
                val formattedPart = formatInputAmount(
                    currency = currency,
                    amount = part,
                    newSymbol = ""
                )

                if (formattedPart != null) {
                    formattedExpression = formattedExpression.replace(part, formattedPart)
                }
            }
        }

    return formattedExpression
}

private fun calculate(expression: String): Double? {
    return try {
        Keval.eval(expression.normalizeExpression())
    } catch (e: Exception) {
        null
    }
}

@Preview
@Composable
private fun Preview() {
    IvyAppPreview {
        CalculatorModal(
            visible = true,
            initialAmount = 50.23,
            currency = "BGN",
            dismiss = { },
            onCalculation = {}
        )
    }
}