package com.wem.snoozy.presentation.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wem.snoozy.domain.entity.GroupItem
import com.wem.snoozy.presentation.itemCard.GroupItemCard
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import com.wem.snoozy.presentation.viewModel.MainState
import com.wem.snoozy.presentation.viewModel.MainViewModel
import com.wem.snoozy.presentation.itemCard.MissedAlarmItem
import com.wem.snoozy.presentation.itemCard.UpcomingAlarmItem

@Composable
fun GroupsScreen(
    onAddGroupClick: () -> Unit = {},
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val state by mainViewModel.state.collectAsState()

    val groups = (state as? MainState.Content)?.groupList ?: emptyList()

    var selectedGroupId by rememberSaveable { mutableStateOf<Int?>(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (groups.isEmpty()) {
            Text(
                text = "У вас пока нет\nгрупп",
                fontSize = 20.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight(900),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                items(groups, key = { it.id }) { group ->
                    val isExpanded = selectedGroupId == group.id
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                    ) {
                        GroupItemCard(
                            groupItem = group,
                            onClick = {
                                selectedGroupId = if (isExpanded) null else group.id
                            }
                        )
                        if (isExpanded) {
                            GroupExpandedDetails(
                                onSettingsClick = { /* TODO: Settings */ }
                            )
                        }
                    }
                }
            }
        }

        BottomGradientShadow()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 32.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AddGroupButton(onAddClick = onAddGroupClick)
        }
    }
}

@Composable
private fun GroupExpandedDetails(
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 12.dp)
            .background(
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
            )
            .padding(bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        MissedAlarmsSection()
        UpcomingAlarmsSection()
    }
}

@Composable
private fun MissedAlarmsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Пропущенные будильники",
            fontSize = 14.sp,
            fontFamily = myTypeFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primaryFixed,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.4f))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MissedAlarmItem("Alex", "07:30")
            MissedAlarmItem("Maria", "08:00")
        }
    }
}

@Composable
private fun UpcomingAlarmsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Ближайшие будильники",
            fontSize = 14.sp,
            fontFamily = myTypeFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.4f))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UpcomingAlarmItem("John", "09:15")
            UpcomingAlarmItem("Sarah", "10:30")
            UpcomingAlarmItem("Kevin", "11:00")
        }
    }
}

@Composable
fun AddGroupButton(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit = {}
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
