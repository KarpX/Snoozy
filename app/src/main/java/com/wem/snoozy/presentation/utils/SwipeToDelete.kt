package com.wem.snoozy.presentation.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.SwipeToDismissBox
import com.wem.snoozy.domain.entity.AlarmItem
import com.wem.snoozy.presentation.itemCard.AlarmItemCard

@Composable
fun SwipeToDeleteAlarmItem(
    alarmItem: AlarmItem,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
    onEditClick: (AlarmItem) -> Unit
) {
    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
           if (it == SwipeToDismissBoxValue.EndToStart) onDelete()
            it != SwipeToDismissBoxValue.StartToEnd
        }
    )

    SwipeToDismissBox(
        state = swipeToDismissBoxState,
        modifier = Modifier.fillMaxSize(),
        backgroundContent = {
            when (swipeToDismissBoxState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove item",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(20))
                                .background(Color.Red)
                                .wrapContentSize(Alignment.CenterEnd)
                                .padding(12.dp),
                            tint = Color.White
                        )
                    }
                }
                SwipeToDismissBoxValue.Settled -> {}
                SwipeToDismissBoxValue.StartToEnd -> {}
            }
        }
    ) {
        AlarmItemCard(
            alarmItem = alarmItem,
            onToggle = onToggle,
            onEditClick = { onEditClick(alarmItem) }
        )
    }
}