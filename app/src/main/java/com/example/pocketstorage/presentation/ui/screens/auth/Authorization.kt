package com.example.pocketstorage.presentation.ui.screens.auth

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import android.window.SplashScreen
import androidx.activity.compose.BackHandler
import androidx.annotation.DimenRes
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.pocketstorage.R
import com.example.pocketstorage.components.DialogWithImage
import com.example.pocketstorage.graphs.AuthScreen
import com.example.pocketstorage.graphs.Graph
import com.example.pocketstorage.presentation.ui.screens.auth.viewmodel.AuthorizationViewModel
import com.example.pocketstorage.utils.SnackbarManager
import com.example.pocketstorage.utils.SnackbarMessage
import com.example.pocketstorage.utils.SnackbarMessage.Companion.toMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*@Preview(showBackground = true)
@Composable
fun Authorization() {
    AuthorizationScreen(
        onClick = {},
        onSignUpClick = {},
        onSignInClickDone = {},
        onForgotClick = {},
        hiltViewModel()
    )
}*/

@Composable
@ReadOnlyComposable
fun fontDimensionResource(@DimenRes id: Int) = dimensionResource(id = id).value.sp

@Composable
fun TextMainApp(nameApp: String) {
    Text(
        modifier = Modifier.padding(top = 80.dp, bottom = 50.dp),
        text = nameApp,
        color = Color.Black,
        fontSize = fontDimensionResource(id = R.dimen.text_size_26sp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldAuthorizationApp(
    textHint: String,
    color: Color,
    modifier: Modifier,
    keyOption: KeyboardOptions,
    visualTransformation: PasswordVisualTransformation,
    icon: @Composable () -> Unit
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    TextField(
        modifier = modifier,
        value = text,
        onValueChange = { text = it },
        label = { Text(text = textHint, color = color) },
        leadingIcon = icon,
        keyboardOptions = keyOption,
        visualTransformation = visualTransformation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldAuthorizationApp(
    textHint: String,
    color: Color,
    modifier: Modifier,
    keyOption: KeyboardOptions,
    icon: @Composable () -> Unit,
    value: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        modifier = modifier,
        value = value,
        onValueChange = { onValueChange(it) },
        label = { Text(text = textHint, color = color) },
        leadingIcon = icon,
        keyboardOptions = keyOption
    )
}

@Composable
fun ButtonLogInAuthorizationApp(
    onClick: () -> Unit, colors: ButtonColors, text: String, iconResource: Painter
) {

    Button(
        onClick = { onClick() },
        colors = colors,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .padding(bottom = 24.dp)
            .size(height = 48.dp, width = 218.dp)
    ) {
        Icon(
            painter = iconResource,
            contentDescription = text,
            modifier = Modifier.padding(end = 10.dp)
        )
        Text(text = text, color = Color.White)

    }
}

@Composable
fun ButtonContinueApp(onClick: () -> Unit) {

    OutlinedButton(
        onClick = { onClick() },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(height = 48.dp, width = 312.dp),
        border = BorderStroke(2.dp, colorResource(id = R.color.RetroBlue))
    ) {
        Text(
            text = stringResource(id = R.string.continue_without_registration),
            color = colorResource(R.color.RetroBlue)
        )

    }
}


@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun AuthorizationScreen(
    onClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onSignInClickDone: () -> Unit,
    onForgotClick: () -> Unit,
    authorizationViewModel: AuthorizationViewModel,
    navController: NavController
) {

    val signInState by authorizationViewModel.signInState.collectAsState()
    val screenUiState by authorizationViewModel.screenState.collectAsState()
    val context = LocalContext.current
    val snackbarMessage by SnackbarManager.snackbarMessages.collectAsState()
    val scope = rememberCoroutineScope()
    val activity = (LocalContext.current as? Activity)
    val shouldShowDialog = remember { mutableStateOf(false) }
    BackHandler {
        shouldShowDialog.value = true
    }

    if (shouldShowDialog.value) {
        DialogWithImage(
            onDismissRequest = { shouldShowDialog.value = false },
            onConfirmation = { activity?.finish() },
            painter = painterResource(id = R.drawable.cat_dialog),
            imageDescription = "cat"
        )
    }



    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextMainApp(nameApp = stringResource(id = R.string.app_name))

        TextFieldAuthorizationApp(
            textHint = stringResource(id = R.string.email_or_telephone),
            color = colorResource(R.color.SpanishGrey),
            modifier = Modifier.padding(bottom = 10.dp),
            keyOption = KeyboardOptions(keyboardType = KeyboardType.Email),
            {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = stringResource(id = R.string.email_or_telephone)
                )

            },
            value = signInState.email,
            onValueChange = authorizationViewModel::onEmailChange
        )

        TextFieldAuthorizationApp(
            textHint = stringResource(id = R.string.password),
            color = colorResource(R.color.SpanishGrey),
            modifier = Modifier.padding(bottom = 36.dp),
            keyOption = KeyboardOptions(keyboardType = KeyboardType.Password),
            //visualTransformation = PasswordVisualTransformation(),
            {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(id = R.string.password)
                )
            },
            value = signInState.password,
            onValueChange = authorizationViewModel::onPasswordChange
        )
        ButtonLogInAuthorizationApp(
            onClick = {
                if (signInState.email.isEmpty() || signInState.password.isEmpty()){
                    return@ButtonLogInAuthorizationApp
                } else{
                    authorizationViewModel.signInLoginAndPassword(
                        signInState.email, signInState.password
                    )
                }

            },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.RetroBlue)),
            text = stringResource(id = R.string.log_in),
            iconResource = painterResource(id = R.drawable.baseline_login_24)
        )

        ButtonLogInAuthorizationApp(
            onClick = {
                // обработка нажатия с регистрацией
                onSignUpClick()
            },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.MildGreen)),
            text = stringResource(id = R.string.sign_up),
            iconResource = painterResource(id = R.drawable.account_plus)
        )

        ButtonContinueApp {
            // обработка нажатия без регистрации
            onClick()
        }

        Spacer(modifier = Modifier.weight(1f)) // Добавляем Spacer для занятия доступного пространства

        Text(
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 26.dp, end = 24.dp),
            text = stringResource(id = R.string.app_version),
            color = Color.Black,
            fontSize = 12.sp
        )

        if (screenUiState.success) {
            if (screenUiState.loading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                scope.launch {
                    delay(2000)
                    onSignInClickDone()
                }
            }
        }
        AuthStateUser(navController, authorizationViewModel)

        SnackBarToast(snackbarMessage, context)
    }


}

@Composable
private fun AuthStateUser(
    navController: NavController,
    authorizationViewModel: AuthorizationViewModel
) {
    if (authorizationViewModel.getAuth()) {
        LaunchedEffect(Unit) {
            navController.navigate(route = Graph.HOME)
        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAuthorization() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextMainApp(nameApp = "Pocket Storage")

        TextFieldAuthorizationApp(textHint = "Email",
            color = colorResource(R.color.SpanishGrey),
            modifier = Modifier.padding(bottom = 10.dp),
            keyOption = KeyboardOptions(keyboardType = KeyboardType.Email),
            {
                Icon(imageVector = Icons.Default.Person, contentDescription = "emailIcon")
            },
            value = "",
            onValueChange = {})

        TextFieldAuthorizationApp(
            textHint = "Password",
            color = colorResource(R.color.SpanishGrey),
            modifier = Modifier.padding(bottom = 36.dp),
            keyOption = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation()
        ) { Icon(imageVector = Icons.Default.Lock, contentDescription = "emailIcon") }

        ButtonLogInAuthorizationApp(
            onClick = {
                // обработка нажатия с регистрацией
            },
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.RetroBlue)),
            text = "Log In / Sign up",
            iconResource = painterResource(id = R.drawable.baseline_login_24)
        )

        ButtonContinueApp {
            // обработка нажатия без регистрации
        }

        Spacer(modifier = Modifier.weight(1f)) // Добавляем Spacer для занятия доступного пространства

        Text(
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 26.dp, end = 24.dp),
            text = "v 1.0.0",
            color = Color.Black,
            fontSize = 12.sp
        )
    }


}

@Composable
private fun SnackBarToast(
    snackbarMessage: SnackbarMessage?, context: Context
) {
    snackbarMessage?.let { message ->
        Log.d("snack", "${message}")
        Snackbar(modifier = Modifier.padding(8.dp), actionOnNewLine = true, dismissAction = {
            TextButton(onClick = { SnackbarManager.clearSnackbarState() }) {
                Text(text = "Закрыть", color = colorResource(id = R.color.AdamantineBlue))
            }
        }) {
            Text(message.toMessage(context.resources), fontSize = 12.sp)
        }

    }
}



