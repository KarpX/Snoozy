package com.wem.snoozy.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import com.wem.snoozy.ui.theme.SnoozyTheme

@Composable
fun GroupsScreen(
    onAddGroupClick: () -> Unit = {}
) {
    // В будущем здесь будет список групп из ViewModel
    val groupsCount = 0 

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (groupsCount == 0) {
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Здесь будет отрисовка групп
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

@Composable
@Preview(
    showBackground = true
)
fun GroupScreenPreview() {
    SnoozyTheme {
        GroupsScreen()
    }
}
