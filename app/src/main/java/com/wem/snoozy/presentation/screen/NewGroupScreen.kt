package com.wem.snoozy.presentation.screen

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.wem.snoozy.R
import com.wem.snoozy.domain.entity.ContactItem
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import com.wem.snoozy.presentation.viewModel.AddMembersViewModel
import com.wem.snoozy.ui.theme.SnoozyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGroupScreen(
    onBackClick: () -> Unit = {},
    viewModel: AddMembersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val selectedContacts = remember(state.selectedContactIds) {
        viewModel.getSelectedContacts()
    }

    var groupName by remember { mutableStateOf("") }
    var isNameManuallyChanged by remember { mutableStateOf(false) }

    // Автоматически заполняем имя группы списком имен участников, если оно не изменено вручную
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
                    onValueChange = {
                        groupName = it
                        isNameManuallyChanged = true
                    },
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
                        viewModel.createGroup(groupName) {
                            onBackClick()
                        }
                    }
                )
            }
        }
    }
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
    onValueChange: (String) -> Unit,
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
                    .clickable { /* TODO */ },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(R.drawable.ic_add_image),
                    tint = Color.Unspecified,
                    contentDescription = null
                )
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
                    modifier = Modifier.fillMaxWidth().height(100.dp),
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
