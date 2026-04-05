package com.wem.snoozy.presentation.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.wem.snoozy.R
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import com.wem.snoozy.presentation.viewModel.ProfileUiState
import com.wem.snoozy.presentation.viewModel.ProfileViewModel
import com.wem.snoozy.ui.theme.SnoozyTheme

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadAvatar(it) }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            is ProfileUiState.Success -> {
                val user = state.user
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                        .padding(top = 32.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    UserMainInfo(
                        username = user.username,
                        avatarUrl = user.avatarLink,
                        isUploading = state.isUploading,
                        onAvatarClick = { launcher.launch("image/*") },
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

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
            is ProfileUiState.Error -> {
                Text(
                    text = state.message,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = myTypeFamily
                )
            }
        }
        
        Box(modifier = Modifier.align(Alignment.BottomCenter)) {
            BottomGradientShadow()
        }
    }
}

@Composable
fun UserMainInfo(
    username: String,
    avatarUrl: String?,
    isUploading: Boolean,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(color = MaterialTheme.colorScheme.onSurface)
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = avatarUrl ?: R.drawable.ic_no_avatar,
                    contentDescription = "User Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_no_avatar),
                    error = painterResource(R.drawable.ic_no_avatar),
                    fallback = painterResource(R.drawable.ic_no_avatar)
                )
                
                if (isUploading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = username,
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
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(color = MaterialTheme.colorScheme.onSurface)
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
) {
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
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(color = MaterialTheme.colorScheme.onSurface)
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
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
@Preview(showBackground = true)
fun ProfileScreenPreview() {
    SnoozyTheme {
        // Mock UI for preview if needed
    }
}
