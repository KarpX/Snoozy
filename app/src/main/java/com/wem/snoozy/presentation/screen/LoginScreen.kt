package com.wem.snoozy.presentation.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.wem.snoozy.R
import com.wem.snoozy.presentation.itemCard.myTypeFamily
import com.wem.snoozy.presentation.viewModel.AuthUiState
import com.wem.snoozy.presentation.viewModel.AuthViewModel
import com.wem.snoozy.presentation.viewModel.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    authViewModel: AuthViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isDarkTheme by settingsViewModel.themeState.collectAsState()
    val authState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    var showPhoneDialog by remember { mutableStateOf(false) }
    var googleIdTokenForDialog by remember { mutableStateOf("") }
    var googlePhoneNumber by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthUiState.Success -> {
                onLoginSuccess()
                authViewModel.resetState()
            }

            is AuthUiState.Error -> {
                Toast.makeText(
                    context,
                    (authState as AuthUiState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                authViewModel.resetState()
            }

            is AuthUiState.NeedPhone -> {
                googleIdTokenForDialog = (authState as AuthUiState.NeedPhone).idToken
                showPhoneDialog = true
            }

            else -> {}
        }
    }

    if (showPhoneDialog) {
        AlertDialog(
            onDismissRequest = {
                showPhoneDialog = false
                authViewModel.resetState()
            },
            title = { Text(text = "Введите номер телефона", fontFamily = myTypeFamily) },
            text = {
                Column {
                    Text(
                        text = "Для завершения регистрации через Google необходимо указать номер телефона.",
                        fontFamily = myTypeFamily
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = googlePhoneNumber,
                        onValueChange = { googlePhoneNumber = it },
                        label = { Text("Номер телефона", fontFamily = myTypeFamily) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (googlePhoneNumber.isNotBlank()) {
                            showPhoneDialog = false
                            authViewModel.googleAuth(googleIdTokenForDialog, googlePhoneNumber)
                        } else {
                            Toast.makeText(context, "Введите номер", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Подтвердить", fontFamily = myTypeFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPhoneDialog = false
                    authViewModel.resetState()
                }) {
                    Text("Отмена", fontFamily = myTypeFamily)
                }
            }
        )
    }

    Scaffold(

    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = innerPadding.calculateTopPadding())
                .padding(bottom = innerPadding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                painter = painterResource(id = if (isDarkTheme) R.drawable.ic_snoozy_logo_dark_theme else R.drawable.ic_snoozy_logo),
                contentDescription = "Snoozy Logo",
                tint = Color.Unspecified,
                modifier = Modifier.size(412.dp, 190.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Войти в аккаунт",
                fontSize = 28.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            LoginTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = "+123456789",
                leadingIcon = Icons.Default.Phone,
                keyboardType = KeyboardType.Phone,
                enabled = authState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Введите пароль",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                enabled = authState !is AuthUiState.Loading
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Забыли пароль?",
                    fontSize = 16.sp,
                    fontFamily = myTypeFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primaryFixed,
                    modifier = Modifier.clickable { /* TODO */ }
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            Button(
                onClick = { authViewModel.login(phone, password) },
                enabled = authState !is AuthUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (authState is AuthUiState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Войти",
                            fontSize = 24.sp,
                            fontFamily = myTypeFamily,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryFixed
                        )

                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .align(Alignment.CenterEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_back_arrow),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryFixed,
                                modifier = Modifier
                                    .size(24.dp)
                                    .rotate(180f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "или",
                fontSize = 18.sp,
                fontFamily = myTypeFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            val googleClientId = stringResource(id = R.string.default_web_client_id)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(horizontal = 8.dp)
                    .border(1.dp, MaterialTheme.colorScheme.onSecondary, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable {
                        Log.d(
                            "GoogleAuth",
                            "Starting Google Sign-In with ClientID: $googleClientId"
                        )
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(googleClientId)
                            .setAutoSelectEnabled(false)
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        scope.launch {
                            try {
                                val result = credentialManager.getCredential(
                                    context = context,
                                    request = request
                                )
                                val credential = result.credential
                                Log.d("GoogleAuth", "Got credential: ${credential.type}")

                                // Извлекаем токен через статический метод createFrom
                                if (credential is CustomCredential &&
                                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                ) {

                                    val googleIdTokenCredential =
                                        GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken
                                    Log.d("GoogleAuth", "ID Token: ${idToken.take(20)}...")
                                    authViewModel.googleAuth(idToken)

                                } else if (credential is GoogleIdTokenCredential) {
                                    val idToken = credential.idToken
                                    Log.d("GoogleAuth", "ID Token (direct): ${idToken.take(20)}...")
                                    authViewModel.googleAuth(idToken)
                                } else {
                                    Log.e(
                                        "GoogleAuth",
                                        "Unexpected credential type: ${credential.type}"
                                    )
                                }
                            } catch (e: GetCredentialException) {
                                Log.e(
                                    "GoogleAuth",
                                    "GetCredentialException: ${e.type} - ${e.message}"
                                )
                                Toast.makeText(context, "Ошибка: ${e.message}", Toast.LENGTH_LONG)
                                    .show()
                            } catch (e: Exception) {
                                Log.e("GoogleAuth", "Unknown error", e)
                                Toast.makeText(
                                    context,
                                    "Произошла неизвестная ошибка",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Вход через Google",
                        fontSize = 16.sp,
                        fontFamily = myTypeFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Нет аккаунта? ",
                    fontSize = 16.sp,
                    fontFamily = myTypeFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "Зарегистрироваться",
                    fontSize = 16.sp,
                    fontFamily = myTypeFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primaryFixed,
                    modifier = Modifier.clickable { onRegisterClick() }
                )
            }
        }
    }
}

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        enabled = enabled,
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f),
                fontFamily = myTypeFamily,
                fontSize = 16.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onPasswordToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.onSecondary,
            focusedBorderColor = MaterialTheme.colorScheme.onSecondary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSecondary,
            focusedLeadingIconColor = MaterialTheme.colorScheme.onSecondary,
            focusedTextColor = MaterialTheme.colorScheme.tertiary,
            unfocusedTextColor = MaterialTheme.colorScheme.tertiary,
            cursorColor = MaterialTheme.colorScheme.tertiary
        )
    )
}
