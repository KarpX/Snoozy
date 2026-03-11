package com.wem.snoozy.presentation.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wem.snoozy.R
import com.wem.snoozy.data.local.UserPreferencesManager
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import com.wem.snoozy.presentation.viewModel.SettingsCommand
import com.wem.snoozy.presentation.viewModel.SettingsState
import com.wem.snoozy.presentation.viewModel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {

    // TODO: ПРИ ВЫХОДЕ ИЗ ПРИЛОЖЕНИЯ В НАСТРОЙКАХ С ПУСТЫМИ ПАРАМЕТРАМИ ОНИ ОСТАНУТСЯ ПУСТЫМИ ПРИ СЛЕД. ЗАХОДЕ

    val state = viewModel.state.collectAsState()
    val currentState = state.value

    DisposableEffect(Unit) {
        onDispose {
            viewModel.processCommand(SettingsCommand.SaveSettings)
        }
    }

    when (currentState) {
        is SettingsState.Content -> {
            Column(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        stringResource(R.string.settings),
                        fontSize = 25.sp,
                        fontFamily = myTypeFamily,
                        fontWeight = FontWeight(700),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                SettingsName(
                    name = stringResource(R.string.app_theme),
                    imageVector = Icons.Outlined.DarkMode,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ThemeSettingsBlock(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    isDarkTheme = currentState.isDarkTheme
                ) {
                    viewModel.processCommand(SettingsCommand.ToggleTheme(it))
                }
                Spacer(modifier = Modifier.height(32.dp))
                SettingsName(
                    name = stringResource(R.string.alarm_settings),
                    imageVector = Icons.Outlined.Alarm,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                AlarmSettingsBlock(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    cycleLength = currentState.cycleLength,
                    sleepStartTime = currentState.sleepStartTime,
                    onCycleLengthInput = {
                        viewModel.processCommand(SettingsCommand.InputCycleLength(it))
                    },
                    onSleepStartTimeInput = {
                        viewModel.processCommand(SettingsCommand.InputSleepStartTime(it))
                    }
                )
            }
        }

        SettingsState.Initial -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun SettingsName(
    modifier: Modifier = Modifier,
    imageVector: ImageVector,
    name: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = "theme mode icon",
            tint = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            fontSize = 15.sp,
            fontFamily = myTypeFamily,
            fontWeight = FontWeight(300),
            color = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Composable
fun ThemeSettingsBlock(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onThemeSwitch: (Boolean) -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30))
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.dark_mode),
                fontSize = 21.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight(500),
                color = MaterialTheme.colorScheme.tertiary,
            )
            Spacer(modifier = Modifier.weight(1f))
            Switch(
                checked = isDarkTheme,
                onCheckedChange = {
                    onThemeSwitch(it)
                },
                enabled = true,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xff8CD39C),
                    uncheckedThumbColor = Color(0xff79747E),
                    checkedTrackColor = Color(0xffAAFFBD),
                    uncheckedTrackColor = Color(0xffE6E0E9),
                    checkedBorderColor = Color(0xff8CD39C),
                    uncheckedBorderColor = Color(0xff79747E),
                    disabledCheckedTrackColor = Color.Gray,
                    disabledUncheckedTrackColor = Color.LightGray,
                    disabledCheckedThumbColor = Color.DarkGray,
                    disabledUncheckedThumbColor = Color.Gray,
                )
            )
        }
    }
}

@Composable
fun AlarmSettingsBlock(
    modifier: Modifier = Modifier,
    cycleLength: String,
    sleepStartTime: String,
    onCycleLengthInput: (String) -> Unit,
    onSleepStartTimeInput: (String) -> Unit
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20))
            .background(MaterialTheme.colorScheme.secondary)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.cycle_length),
                    fontSize = 21.sp,
                    fontFamily = myTypeFamily,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        cursorBrush = Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Transparent
                            )
                        ),
                        value = cycleLength,
                        onValueChange = {
                            val truncated = it.take(3)
                            onCycleLengthInput(truncated)
                        },
                        modifier = Modifier
                            .width(80.dp)
                            .height(30.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        textStyle = TextStyle(
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = myTypeFamily,
                            fontWeight = FontWeight(500),
                            color = MaterialTheme.colorScheme.tertiary,
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp)
                                    .clip(RoundedCornerShape(20))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.tertiary,
                                        RoundedCornerShape(20)
                                    )
                            ) {
                                innerTextField()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.minutes),
                        fontSize = 21.sp,
                        fontFamily = myTypeFamily,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
            HorizontalDivider(
                Modifier.padding(horizontal = 16.dp),
                1.dp,
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.sleep_start_time),
                    fontSize = 21.sp,
                    fontFamily = myTypeFamily,
                    fontWeight = FontWeight(500),
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        cursorBrush = Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Transparent
                            )
                        ),
                        value = sleepStartTime,
                        onValueChange = {
                            val truncated = it.take(2)
                            onSleepStartTimeInput(truncated)
                        },
                        modifier = Modifier
                            .width(80.dp)
                            .height(30.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        textStyle = TextStyle(
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = myTypeFamily,
                            fontWeight = FontWeight(500),
                            color = MaterialTheme.colorScheme.tertiary,
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(0.dp)
                                    .clip(RoundedCornerShape(20))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.tertiary,
                                        RoundedCornerShape(20)
                                    )
                            ) {
                                innerTextField()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.minutes),
                        fontSize = 21.sp,
                        fontFamily = myTypeFamily,
                        fontWeight = FontWeight(500),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
            }
        }
    }
}


