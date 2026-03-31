package com.wem.snoozy.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.wem.snoozy.R
import com.wem.snoozy.domain.entity.ContactItem
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import com.wem.snoozy.presentation.viewModel.AddMembersViewModel
import com.wem.snoozy.ui.theme.SnoozyTheme
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGroupScreen(
    onBackClick: () -> Unit = {},
    viewModel: AddMembersViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val selectedContacts = remember(state.selectedContactIds) {
        viewModel.getSelectedContacts()
    }

    var groupName by remember { mutableStateOf("") }
    var groupAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var isNameManuallyChanged by remember { mutableStateOf(false) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // Вспомогательная функция для создания Uri файла
    fun createTempPictureUri(): Uri {
        val file = File(context.cacheDir, "images/group_avatar_${System.currentTimeMillis()}.jpg")
        file.parentFile?.mkdirs()
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // Лаунчер для камеры
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) groupAvatarUri = tempPhotoUri
        }

    // Лаунчер для запроса разрешения
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                val uri = createTempPictureUri()
                tempPhotoUri = uri
                cameraLauncher.launch(uri)
            }
        }

    // Лаунчер для галереи
    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) groupAvatarUri = uri
        }

    LaunchedEffect(selectedContacts) {
        if (!isNameManuallyChanged) {
            groupName = selectedContacts.joinToString(", ") { it.name }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Новая группа",
                        fontSize = 22.sp,
                        fontFamily = myTypeFamily,
                        fontWeight = FontWeight(900),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                },
                navigationIcon = {
                    Icon(
                        modifier = Modifier.clickable { onBackClick() },
                        painter = painterResource(R.drawable.ic_back_arrow),
                        tint = MaterialTheme.colorScheme.tertiary,
                        contentDescription = null
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MainGroupInfo(
                    value = groupName,
                    avatarUri = groupAvatarUri,
                    onValueChange = {
                        groupName = it
                        isNameManuallyChanged = true
                    },
                    onAvatarClick = { showImagePickerDialog = true },
                    onClearClick = {
                        groupName = ""
                        isNameManuallyChanged = true
                    }
                )
                GroupMembersInNewGroup(members = selectedContacts)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp, horizontal = 32.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                CreateGroupButton(
                    onClick = {
                        viewModel.createGroup(groupName, groupAvatarUri?.toString()) {
                            onBackClick()
                        }
                    }
                )
            }
        }

        if (showImagePickerDialog) {
            ImageSourceDialog(
                onDismiss = { showImagePickerDialog = false },
                onGalleryClick = {
                    showImagePickerDialog = false
                    galleryLauncher.launch("image/*")
                },
                onCameraClick = {
                    showImagePickerDialog = false
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        val uri = createTempPictureUri()
                        tempPhotoUri = uri
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            )
        }
    }
}

@Composable
fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Выбрать фото группы",
                fontSize = 24.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight(600),
                color = MaterialTheme.colorScheme.tertiary
            )
        },
        text = {
            Text(
                text = "Откуда вы хотите загрузить изображение?",
                fontSize = 18.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight(600),
                color = MaterialTheme.colorScheme.tertiary
            )
        },
        confirmButton = {
            TextButton(
                onClick = onGalleryClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Галерея")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCameraClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Камера")
            }
        }
    )
}

@Composable
fun CreateGroupButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(1.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onTertiary)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Check,
            "Create Group button",
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@Composable
fun MainGroupInfo(
    value: String,
    avatarUri: Uri?,
    onValueChange: (String) -> Unit,
    onAvatarClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.onSurface)
                .padding(horizontal = 16.dp)
                .height(88.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(1.dp, RoundedCornerShape(50))
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .clickable { onAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                if (avatarUri != null) {
                    AsyncImage(
                        model = avatarUri,
                        contentDescription = "Group Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        modifier = Modifier.size(32.dp),
                        painter = painterResource(R.drawable.ic_add_image),
                        tint = Color.Unspecified,
                        contentDescription = null
                    )
                }
            }

            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp),
                value = value,
                onValueChange = onValueChange,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.tertiary
                ),
                trailingIcon = {
                    if (value.isNotEmpty()) {
                        IconButton(onClick = onClearClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                placeholder = {
                    Text(
                        text = "Название группы",
                        fontSize = 20.sp,
                        fontFamily = myTypeFamily,
                        fontWeight = FontWeight(600),
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                    )
                },
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = myTypeFamily,
                    fontWeight = FontWeight(600),
                    color = MaterialTheme.colorScheme.tertiary
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun GroupMembersInNewGroup(
    members: List<ContactItem>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.onSurface)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${members.size} members",
                fontSize = 16.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight(900),
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (members.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Тут пусто...",
                        fontSize = 18.sp,
                        fontFamily = myTypeFamily,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(members) { member ->
                        MemberItem(member)
                    }
                }
            }
        }
    }
}

@Composable
fun MemberItem(member: ContactItem) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (member.photoUri != null) {
                AsyncImage(
                    model = member.photoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = member.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = member.name,
                fontSize = 18.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight(700),
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
        )
    }
}

@Composable
@Preview(showBackground = true)
fun NewGroupPreview() {
    SnoozyTheme {
        NewGroupScreen()
    }
}
