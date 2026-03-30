package com.wem.snoozy.presentation.utils

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wem.snoozy.R
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    onCancelClick: () -> Unit
) {
    val todayStart = LocalDate.now().toEpochDay() * 24 * 60 * 60 * 1000

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 24 * 60 * 60 * 1000,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis >= todayStart
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                        onConfirm(date)
                    }
                },
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            ) {
                Text(
                    stringResource(R.string.save).uppercase(),
                    fontSize = 15.sp,
                    fontFamily = myTypeFamily,
                    fontWeight = FontWeight(900),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        },
        dismissButton = {
            Button(
                onClick = { onCancelClick() },
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
            ) {
                Text(
                    stringResource(R.string.cancel).uppercase(),
                    fontSize = 15.sp,
                    fontFamily = myTypeFamily,
                    fontWeight = FontWeight(900),
                    color = Color(0xffEC625F)
                )
            }
        },
        colors = DatePickerDefaults.colors().copy(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        DatePicker(
            showModeToggle = false,
            state = datePickerState,
            colors = DatePickerDefaults.colors().copy(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.tertiary,
                headlineContentColor = MaterialTheme.colorScheme.tertiary,
                weekdayContentColor = MaterialTheme.colorScheme.tertiary,
                navigationContentColor = MaterialTheme.colorScheme.tertiary,
                yearContentColor = MaterialTheme.colorScheme.tertiary,
                dayContentColor = MaterialTheme.colorScheme.tertiary,
                todayContentColor = MaterialTheme.colorScheme.tertiary
            )
        )
    }
}

fun formatDateWithRelative(date: LocalDate): String {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(Locale.ENGLISH)

    return when (date) {
        today -> "Сегодня"
        today.plusDays(1) -> "Завтра"
        today.plusDays(2) -> "Послезавтра"
        else -> date.format(formatter)
    }
}

fun String.formatStringToDate(): LocalDate {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy").withLocale(Locale.ENGLISH)

    return when (this) {
        "Сегодня" -> today
        "Завтра" -> today.plusDays(1)
        "Послезавтра" -> today.plusDays(2)
        else -> LocalDate.parse(this, formatter)
    }
}