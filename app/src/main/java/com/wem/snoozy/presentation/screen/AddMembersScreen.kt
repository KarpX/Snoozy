package com.wem.snoozy.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.Icons.Default
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wem.snoozy.R
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import com.wem.snoozy.ui.theme.SnoozyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMembersScreen() {
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
                .padding(paddingValues)
        ){
            MembersSearchBar()
        }
    }
}

@Composable
fun MembersSearchBar(){
    val searchText = remember{mutableStateOf("")}

    Box(
        modifier = Modifier
            .fillMaxWidth()){
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(40.dp)),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.onSurface,
                unfocusedContainerColor = MaterialTheme.colorScheme.onSurface,
                focusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
                cursorColor = MaterialTheme.colorScheme.tertiary
            ),
            value = searchText.value,
            onValueChange = {searchText.value = it},
            placeholder = {Text(
                text = "Поиск...",
                fontSize = 18.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight(900),
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f))},
            leadingIcon = { Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
            )},
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
@Preview(
    showBackground = true
)
fun AddMembersPreview() {
    SnoozyTheme() {
        AddMembersScreen()
    }
}