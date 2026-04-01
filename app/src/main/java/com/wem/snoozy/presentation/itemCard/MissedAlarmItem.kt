package com.wem.snoozy.presentation.itemCard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wem.snoozy.ui.theme.TaupeGray

@Composable
fun MissedAlarmItem(name: String, time: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20))
            .clip(RoundedCornerShape(16.dp))
            .background(color = MaterialTheme.colorScheme.primaryFixed)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            fontFamily = myTypeFamily,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondaryFixed,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp
        )
        Text(
            text = time,
            fontFamily = myTypeFamily,
            color = MaterialTheme.colorScheme.secondaryFixed.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 8.dp),
            fontSize = 13.sp
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .shadow(1.dp, RoundedCornerShape(50))
                .clip(RoundedCornerShape(50))
                .background(color = MaterialTheme.colorScheme.onPrimaryFixed)
                .clickable {
                    // TODO call to friend
                },
            contentAlignment = Alignment.Center
        ){
            Icon(
                imageVector = Icons.Default.PhoneInTalk,
                contentDescription = "Send alarm",
                tint = MaterialTheme.colorScheme.secondaryFixed,
                modifier = Modifier
                    .size(24.dp)
            )
        }
    }
}