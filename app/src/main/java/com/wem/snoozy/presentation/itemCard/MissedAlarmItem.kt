package com.wem.snoozy.presentation.itemCard

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.wem.snoozy.R
import com.wem.snoozy.ui.theme.SnoozyTheme

@Composable
fun MissedAlarmItem(
    name: String,
    time: String,
    avatarLink: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(20))
            .clip(RoundedCornerShape(16.dp))
            .background(color = MaterialTheme.colorScheme.onPrimaryFixed),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.secondaryFixed)
                .padding(8.dp)
                .weight(2f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Log.v("MemberAvatar", avatarLink.toString())
                AsyncImage(
                    model = avatarLink ?: R.drawable.ic_no_avatar,
                    contentDescription = "Member Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_no_avatar),
                    error = painterResource(R.drawable.ic_no_avatar),
                    fallback = painterResource(R.drawable.ic_no_avatar)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryFixed,
                modifier = Modifier.weight(1f),
                fontSize = 14.sp
            )
            Text(
                text = time,
                fontFamily = myTypeFamily,
                color = MaterialTheme.colorScheme.onPrimaryFixed.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 8.dp),
                fontSize = 13.sp
            )
        }
        Box(
            modifier = Modifier
                .weight(0.5f),
            contentAlignment = Alignment.Center
        ){
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color = MaterialTheme.colorScheme.onBackground)
                    .clickable {
                        // TODO call to friend
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_phone_call),
                    contentDescription = "Send alarm",
                    tint = MaterialTheme.colorScheme.onPrimaryFixed,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun MissedAlarmItemPreview() {
    SnoozyTheme() {
        MissedAlarmItem("Alex", "07:30")
    }
}
