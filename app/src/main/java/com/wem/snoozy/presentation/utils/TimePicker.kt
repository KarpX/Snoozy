package com.wem.snoozy.presentation.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.wem.snoozy.R
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
    onCancelClick: () -> Unit
) {
    val timePickerState = rememberTimePickerState()

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors().copy(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors().copy(
                        selectorColor = MaterialTheme.colorScheme.primary,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.tertiary,
                        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.surface,
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                        clockDialColor = MaterialTheme.colorScheme.surface,
                        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.tertiary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onCancelClick() },
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            stringResource(R.string.cancel).uppercase(),
                            fontSize = 15.sp,
                            fontFamily = myTypeFamily,
                            fontWeight = FontWeight(900),
                            color = Color(0xffEC625F)
                        )
                    }
                    Button(
                        onClick = {
                            onConfirm(
                                LocalTime.of(timePickerState.hour, timePickerState.minute)
                            )
                        },
                        colors = ButtonDefaults.buttonColors().copy(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            stringResource(R.string.save).uppercase(),
                            fontSize = 15.sp,
                            fontFamily = myTypeFamily,
                            fontWeight = FontWeight(900),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}