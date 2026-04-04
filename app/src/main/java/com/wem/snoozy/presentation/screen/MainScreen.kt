package com.wem.snoozy.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wem.snoozy.R
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.domain.entity.CycleItem
import com.wem.snoozy.presentation.itemCard.CycleItemCard
import com.wem.snoozy.presentation.utils.DatePickerDialog
import com.wem.snoozy.presentation.utils.SwipeToDeleteAlarmItem
import com.wem.snoozy.presentation.utils.TimePickerDialog
import com.wem.snoozy.presentation.utils.formatDateWithRelative
import com.wem.snoozy.presentation.viewModel.AddAlarmCommand
import com.wem.snoozy.presentation.viewModel.AddAlarmCommand.SaveAlarm
import com.wem.snoozy.presentation.viewModel.AddAlarmState
import com.wem.snoozy.presentation.viewModel.AddAlarmViewModel
import com.wem.snoozy.presentation.viewModel.EditAlarmCommand.EditAlarm
import com.wem.snoozy.presentation.viewModel.EditAlarmCommand.SelectCycle
import com.wem.snoozy.presentation.viewModel.EditAlarmCommand.SelectDate
import com.wem.snoozy.presentation.viewModel.EditAlarmCommand.SelectDay
import com.wem.snoozy.presentation.viewModel.EditAlarmCommand.SelectTime
import com.wem.snoozy.presentation.viewModel.EditAlarmState
import com.wem.snoozy.presentation.viewModel.EditAlarmViewModel
import com.wem.snoozy.presentation.viewModel.MainCommand.DeleteAlarm
import com.wem.snoozy.presentation.viewModel.MainCommand.SwitchAlarm
import com.wem.snoozy.presentation.viewModel.MainState
import com.wem.snoozy.presentation.viewModel.MainViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    val scope = rememberCoroutineScope()

    var showBottomSheetAdd by remember { mutableStateOf(false) }
    var showBottomSheetEdit by remember { mutableStateOf(false) }

    val editAlarm = remember { mutableStateOf(AlarmItem(0, "", "", "", false, "")) }

    val state = mainViewModel.state.collectAsState()
    val currentState = state.value

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (currentState) {
            MainState.Initial -> {
            }

            is MainState.Content -> {
                AlarmsList(
                    alarms = currentState.alarmList,
                    onDeleteSwipe = { alarmId ->
                        mainViewModel.processCommand(DeleteAlarm(alarmId))
                    },
                    onToggleAlarm = { alarmItem ->
                        mainViewModel.processCommand(SwitchAlarm(alarmItem))
                    },
                    onEditClick = { alarmItem ->
                        editAlarm.value = alarmItem
                        showBottomSheetEdit = true
                    }
                )
                BottomGradientShadow(
                    shadowHeight = 160.dp
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp, horizontal = 32.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AddButton {
                        showBottomSheetAdd = true
                    }
                }
            }

            MainState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        if (showBottomSheetAdd) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        showBottomSheetAdd = false
                    }
                },
                sheetState = sheetState,
                dragHandle = {
                    Box(modifier = Modifier.height(0.dp))
                },
                sheetGesturesEnabled = false,
                containerColor = MaterialTheme.colorScheme.background,
                scrimColor = Color.Black.copy(.85f),
            ) {
                BottomSheetContentAdd {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        showBottomSheetAdd = false
                    }
                }
            }
        }
        if (showBottomSheetEdit) {
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        showBottomSheetEdit = false
                    }
                },
                sheetState = sheetState,
                dragHandle = {
                    Box(modifier = Modifier.height(0.dp))
                },
                sheetGesturesEnabled = false,
                containerColor = MaterialTheme.colorScheme.background,
                scrimColor = Color.Black.copy(.75f),
            ) {
                BottomSheetContentEdit(
                    editAlarm.value
                ) {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        showBottomSheetEdit = false
                    }
                }
            }
        }
    }
}

@Composable
fun AddButton(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(1.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onTertiary)
            .clickable {
                onAddClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Add,
            "Add button",
            modifier = Modifier.size(70.dp),
            tint = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun BottomGradientShadow(
    modifier: Modifier = Modifier,
    shadowHeight: Dp = 100.dp
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        0.0f to MaterialTheme.colorScheme.background.copy(alpha = 0f),
                        0.3f to MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                        1f to MaterialTheme.colorScheme.background.copy(alpha = 1f),
                    )
                )
                .height(shadowHeight),
            contentAlignment = Alignment.Center
        ) {}
    }
}


@Composable
fun AlarmsList(
    modifier: Modifier = Modifier,
    alarms: List<AlarmItem>,
    onDeleteSwipe: (Int) -> Unit,
    onToggleAlarm: (AlarmItem) -> Unit,
    onEditClick: (AlarmItem) -> Unit
) {
    if (alarms.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "У вас пока нет\nбудильников",
                fontSize = 20.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight(900),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(top = 16.dp)
        ) {
            items(
                alarms,
                key = { it.id }
            ) { alarm ->
                Box(
                    modifier = Modifier.animateItem()
                ) {
                    SwipeToDeleteAlarmItem(
                        alarmItem = alarm,
                        onDelete = { onDeleteSwipe(alarm.id) },
                        onToggle = { onToggleAlarm(alarm) },
                        onEditClick = { onEditClick(alarm) }
                    )
                }
            }
            item {
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .background(Color.Transparent)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContentAdd(
    onCancelClick: () -> Unit,
) {

    val sheetId = rememberSaveable { System.currentTimeMillis().toString() }

    val addAlarmViewModel: AddAlarmViewModel = hiltViewModel(
        key = sheetId,
    )

    var showTimePicker by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }

    val selectedCycleId = addAlarmViewModel.selectedCycleId.collectAsState()

    val state = addAlarmViewModel.state.collectAsState()
    val currentState = state.value


    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = {
                showTimePicker = false
            },
            onConfirm = { time ->
                addAlarmViewModel.processCommand(AddAlarmCommand.SelectTime(time))
                showTimePicker = false
            },
            onCancelClick = {
                showTimePicker = false
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            initialDate = LocalDate.now(),
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                addAlarmViewModel.processCommand(AddAlarmCommand.SelectDate(date))
                showDatePicker = false
            },
            onCancelClick = {
                showDatePicker = false
            }
        )
    }


    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (currentState) {
            AddAlarmState.Initial -> {}

            is AddAlarmState.Content -> {
                AlarmTime(
                    modifier = Modifier.padding(vertical = 24.dp),
                    selectedTime = currentState.selectedTime,
                    onTimeClick = {
                        showTimePicker = true
                    }
                )
                CycleTable(
                    cycles = currentState.cyclesList,
                    addAlarmViewModel = addAlarmViewModel
                )
                WeekDaysRow(
                    modifier = Modifier.padding(vertical = 24.dp),
                    addViewModel = addAlarmViewModel
                )
                AlarmDate(
                    selectedDate = currentState.selectedDate,
                    onApplyDateClick = {
                        showDatePicker = true
                    }
                )
                BottomSheetCancelAndSave(
                    onSaveClick = {
                        addAlarmViewModel.processCommand(
                            SaveAlarm(
                                AlarmItem(
                                    id = 0,
                                    ringDay = formatDateWithRelative(currentState.selectedDate),
                                    ringHours = currentState.selectedTime.hour.toString()
                                        .padStart(2, '0')
                                            + ":" + currentState.selectedTime.minute.toString()
                                        .padStart(2, '0'),
                                    timeToBed = if (selectedCycleId.value != -1) {
                                        currentState.cyclesList.find { it.id == selectedCycleId.value }?.time ?: ""
                                    } else "",
                                    checked = true,
                                    repeatDays = currentState.daysList.filter { it.checked }.joinToString(",") { it.id.toString() }
                                )
                            )
                        )
                    },
                    onCancelClick = onCancelClick
                )
            }

            AddAlarmState.Loading -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContentEdit(
    alarmItem: AlarmItem,
    onCancelClick: () -> Unit
) {

    val sheetId = rememberSaveable { System.currentTimeMillis().toString() }

    val editAlarmViewModel: EditAlarmViewModel = hiltViewModel(
        key = sheetId,
        creationCallback = { factory: EditAlarmViewModel.Factory ->
            factory.create(alarmItem)
        }
    )

    var showTimePicker by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }

    val selectedCycleId = editAlarmViewModel.editSelectedCycleId.collectAsState()

    val state: State<EditAlarmState> = editAlarmViewModel.editState.collectAsState()
    val currentState = state.value


    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = {
                showTimePicker = false
            },
            onConfirm = { time ->
                editAlarmViewModel.processCommand(SelectTime(time))
                showTimePicker = false
            },
            onCancelClick = {
                showTimePicker = false
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            initialDate = LocalDate.now(),
            onDismiss = { showDatePicker = false },
            onConfirm = { date ->
                editAlarmViewModel.processCommand(SelectDate(date))
                showDatePicker = false
            },
            onCancelClick = {
                showDatePicker = false
            }
        )
    }


    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (currentState) {
            EditAlarmState.Initial -> {}

            is EditAlarmState.Content -> {
                AlarmTime(
                    modifier = Modifier.padding(vertical = 24.dp),
                    selectedTime = currentState.selectedTime,
                    onTimeClick = {
                        showTimePicker = true
                    }
                )
                CycleTable(
                    cycles = currentState.cyclesList,
                    editAlarmViewModel = editAlarmViewModel
                )
                WeekDaysRow(
                    modifier = Modifier.padding(vertical = 24.dp),
                    editViewModel = editAlarmViewModel
                )
                AlarmDate(
                    selectedDate = currentState.selectedDate,
                    onApplyDateClick = {
                        showDatePicker = true
                    }
                )
                BottomSheetCancelAndSave(
                    onSaveClick = {
                        editAlarmViewModel.processCommand(
                            EditAlarm(
                                AlarmItem(
                                    id = alarmItem.id,
                                    ringDay = formatDateWithRelative(currentState.selectedDate),
                                    ringHours = currentState.selectedTime.hour.toString()
                                        .padStart(2, '0')
                                            + ":" + currentState.selectedTime.minute.toString()
                                        .padStart(2, '0'),
                                    timeToBed = if (selectedCycleId.value != -1) {
                                        currentState.cyclesList.find { it.id == selectedCycleId.value }?.time ?: ""
                                    } else "",
                                    checked = true,
                                    repeatDays = currentState.daysList.filter { it.checked }.joinToString(",") { it.id.toString() }
                                )
                            )
                        )
                    },
                    onCancelClick = onCancelClick
                )
            }

            EditAlarmState.Loading -> {}
        }
    }
}

@Composable
fun BottomSheetCancelAndSave(
    modifier: Modifier = Modifier,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = { onCancelClick() },
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = Color(0xffEC625F)
            ),
            modifier = Modifier.weight(2f)
        ) {
            Text(
                stringResource(R.string.cancel),
                fontSize = 16.sp,
                fontWeight = FontWeight(900),
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                onSaveClick()
                onCancelClick()
            },
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.weight(2f)
        ) {
            Text(
                "Сохранить",
                fontSize = 16.sp,
                fontWeight = FontWeight(900),
                color = Color.Black
            )
        }
    }
}

@Composable
fun AlarmDate(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate,
    onApplyDateClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(bottom = 32.dp),
    ) {
        Text(
            text = "Будильник сработает:",
            fontSize = 16.sp,
            fontWeight = FontWeight(900),
            color = MaterialTheme.colorScheme.tertiary
        )

        Button(
            { onApplyDateClick() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    formatDateWithRelative(selectedDate),
                    fontSize = 20.sp,
                    fontWeight = FontWeight(900),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}


@Composable
fun AlarmTime(
    modifier: Modifier = Modifier,
    selectedTime: LocalTime,
    onTimeClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onTimeClick()
            },
        horizontalArrangement = Arrangement.Center
    ) {
        TimeCard(
            time = selectedTime.hour.toString().padStart(2, '0')
        )
        Text(
            ":",
            fontSize = 60.sp,
            fontWeight = FontWeight(900),
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        TimeCard(
            time = selectedTime.minute.toString().padStart(2, '0')
        )
    }
}

@Composable
fun TimeCard(
    modifier: Modifier = Modifier,
    containerSize: Dp = 90.dp,
    fontSize: TextUnit = 60.sp,
    time: String
) {
    Card(
        modifier = modifier.size(containerSize),
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = CircleShape.copy(CornerSize(20))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = time,
                fontSize = fontSize,
                fontWeight = FontWeight(900),
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
fun WeekDaysRow(
    modifier: Modifier = Modifier,
    addViewModel: AddAlarmViewModel? = null,
    editViewModel: EditAlarmViewModel? = null
) {

    val currentState =
        addViewModel?.daysList?.collectAsState() ?: editViewModel!!.editDaysList.collectAsState()

    LazyRow(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        items(
            items = currentState.value,
            key = { it.id.toString() + it.name }
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .then(
                        if (it.checked) Modifier
                            .shadow(2.dp, RoundedCornerShape(20)) else Modifier
                    )
                    .clip(RoundedCornerShape(20))
                    .background(if (it.checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                    .padding(3.dp)
                    .clickable {
                        if (editViewModel != null) {
                            editViewModel.processCommand(SelectDay(it.id))
                        } else addViewModel?.processCommand(AddAlarmCommand.SelectDay(it.id))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    it.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight(900),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Composable
fun CycleTable(
    modifier: Modifier = Modifier,
    cycles: List<CycleItem>,
    addAlarmViewModel: AddAlarmViewModel? = null,
    editAlarmViewModel: EditAlarmViewModel? = null
) {

    val listState = rememberLazyListState()

    LaunchedEffect(cycles) {
        val selectedIndex = cycles.indexOfFirst { it.checked }
        if (selectedIndex != -1) {
            listState.animateScrollToItem(selectedIndex)
        }
    }

    Box(
        modifier = modifier
            .size(340.dp, 240.dp)
            .clip(RoundedCornerShape(10))
            .background(MaterialTheme.colorScheme.onSurface)
    ) {
        LazyColumn(
            modifier = Modifier,
            contentPadding = PaddingValues(vertical = 16.dp),
            state = listState
        ) {
            items(
                cycles,
                key = { it.id.toString() + it.time }
            ) {
                Box(
                    modifier = Modifier.animateItem()
                ) {
                    CycleItemCard(it) {
                        if (editAlarmViewModel != null) {
                            editAlarmViewModel.processCommand(SelectCycle(it.id))
                        } else addAlarmViewModel?.processCommand(AddAlarmCommand.SelectCycle(it.id))
                    }
                }
            }
        }
    }
}
