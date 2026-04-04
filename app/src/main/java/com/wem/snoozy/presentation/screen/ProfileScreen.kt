package com.wem.snoozy.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Text
import coil3.compose.AsyncImage
import com.wem.snoozy.R
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import com.wem.snoozy.ui.theme.SnoozyTheme

@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(top=32.dp)
        ) {
            UserMainInfo(
                modifier = Modifier.padding(bottom = 16.dp)
            )
            GroupTitle(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp),
                title = "Достижения"
            )
            AchievementsRow(
                modifier = Modifier.padding(bottom = 16.dp)
            )
            GroupTitle(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 4.dp),
                title = "Статистика"
            )
            UserStatistics()
        }
    }
}

@Composable
fun UserMainInfo(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(color= MaterialTheme.colorScheme.onSurface)
            .height(160.dp),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(50)),
                painter = painterResource(R.drawable.ic_no_avatar),
                tint = Color.Unspecified,
                contentDescription = null
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "Пользователь",
                fontSize = 16.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight(900),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun AchievementsRow(
    modifier: Modifier = Modifier
){
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(color= MaterialTheme.colorScheme.onSurface)
            .height(90.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Пока нет достижений",
            fontSize = 16.sp,
            fontFamily = myTypeFamily,
            fontWeight = FontWeight(900),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f)
        )
    }
}

@Composable
fun GroupTitle(
    modifier: Modifier = Modifier,
    title: String
){
    Text(
        modifier = modifier,
        text = title,
        fontSize = 16.sp,
        fontFamily = myTypeFamily,
        fontWeight = FontWeight(900),
        color = MaterialTheme.colorScheme.tertiary
    )
}

@Composable
fun UserStatistics(
    modifier: Modifier = Modifier
){
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(color= MaterialTheme.colorScheme.onSurface)
            .height(370.dp),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = "Пока нет статистики",
            fontSize = 16.sp,
            fontFamily = myTypeFamily,
            fontWeight = FontWeight(900),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f)
        )
    }
}

@Composable
@Preview(
    showBackground = true
)
fun ProfileScreenPreview() {
    SnoozyTheme() {
        ProfileScreen()
    }
}