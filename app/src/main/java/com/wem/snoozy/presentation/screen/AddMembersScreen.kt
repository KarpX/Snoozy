package com.wem.snoozy.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.wem.snoozy.R
import com.wem.snoozy.domain.entity.ContactItem
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import com.wem.snoozy.presentation.viewModel.AddMembersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMembersScreen(
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    viewModel: AddMembersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.loadContacts()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            viewModel.loadContacts()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Добавить участников",
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
                MembersSearchBar(
                    query = state.searchText,
                    onQueryChanged = viewModel::onSearchQueryChanged
                )
                
                if (state.selectedContactIds.isNotEmpty()) {
                    SelectedMembersList(
                        selectedContacts = viewModel.getSelectedContacts(),
                        onRemoveClick = { viewModel.toggleSelection(it.id) }
                    )
                } else {
                    SelectedMembers(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(horizontal = 16.dp)
                    )
                }

                MembersList(
                    contacts = state.contacts,
                    isLoading = state.isLoading,
                    onContactClick = { viewModel.toggleSelection(it.id) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                )
            }
            BottomGradientShadow()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp, horizontal = 32.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                val isVisible = state.selectedContactIds.isNotEmpty()
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    AddMembersButton(
                        onNextClick = {
                            if (isVisible) {
                                onNextClick()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MembersSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(40.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                unfocusedContainerColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.tertiary
            ),
            value = query,
            onValueChange = onQueryChanged,
            placeholder = {
                Text(
                    text = "Поиск...",
                    fontSize = 18.sp,
                    fontFamily = myTypeFamily,
                    fontWeight = FontWeight(900),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                )
            },
            textStyle = TextStyle(
                fontSize = 18.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight(600),
                color = MaterialTheme.colorScheme.tertiary
            ),
            singleLine = true
        )
    }
}

@Composable
fun SelectedMembers(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Text(
            text = "Выберите участников",
            fontSize = 22.sp,
            fontFamily = myTypeFamily,
            fontWeight = FontWeight(900),
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun SelectedMembersList(
    selectedContacts: List<ContactItem>,
    onRemoveClick: (ContactItem) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(selectedContacts) { contact ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(60.dp)
                    .clickable { onRemoveClick(contact) }
            ) {
                ContactAvatar(contact = contact, size = 50.dp)
                Text(
                    modifier = Modifier
                        .padding(top = 8.dp),
                    text = contact.name,
                    fontSize = 10.sp,
                    maxLines = 2,
                    fontFamily = myTypeFamily,
                    color = MaterialTheme.colorScheme.tertiary,
                    softWrap = true,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MembersList(
    contacts: List<ContactItem>,
    isLoading: Boolean,
    onContactClick: (ContactItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier
        .fillMaxSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (contacts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(15.dp))
                    .background(color = MaterialTheme.colorScheme.onSurface)
                    .height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Контакты не найдены",
                    fontSize = 22.sp,
                    fontFamily = myTypeFamily,
                    fontWeight = FontWeight(600),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(contacts, key = { it.id + it.phoneNumber }) { contact ->
                    ContactRow(contact = contact, onClick = { onContactClick(contact) })
                }
            }
        }
    }
}

@Composable
fun ContactRow(
    contact: ContactItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .background(MaterialTheme.colorScheme.onSurface)
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(contact = contact, size = 48.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = myTypeFamily,
                color = MaterialTheme.colorScheme.tertiary
            )
            Text(
                text = contact.phoneNumber,
                fontSize = 14.sp,
                fontFamily = myTypeFamily,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
            )
        }
        if (contact.isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ContactAvatar(contact: ContactItem, size: androidx.compose.ui.unit.Dp) {
    if (contact.photoUri != null) {
        AsyncImage(
            model = contact.photoUri,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.take(1).uppercase(),
                fontSize = (size.value / 2.sp.value).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun AddMembersButton(
    modifier: Modifier = Modifier,
    onNextClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .shadow(1.dp, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onTertiary)
            .clickable {
                onNextClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.ArrowForward,
            "Add button",
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}
