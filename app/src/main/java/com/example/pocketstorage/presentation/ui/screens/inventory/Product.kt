package com.example.pocketstorage.presentation.ui.screens.inventory

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Checkbox
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.pocketstorage.R
import com.example.pocketstorage.components.DialogWithImage
import com.example.pocketstorage.components.SnackBarToast
import com.example.pocketstorage.components.bottomBorder
import com.example.pocketstorage.domain.model.Inventory
import com.example.pocketstorage.graphs.BottomBarScreen
import com.example.pocketstorage.graphs.HomeNavGraph
import com.example.pocketstorage.presentation.ui.screens.inventory.event.ProductEvent
import com.example.pocketstorage.presentation.ui.screens.inventory.viewmodel.InventoryViewModel
import com.example.pocketstorage.utils.SnackbarManager
import com.example.pocketstorage.utils.SnackbarMessage
import com.example.pocketstorage.utils.SnackbarMessage.Companion.toMessage
import com.google.firebase.auth.FirebaseAuth
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavHostController = rememberNavController()) {
    Scaffold(
        bottomBar = {
            BottomBar(navController = navController)
        }
    ) {
        HomeNavGraph(navController = navController)
    }
}

@Composable
fun CameraPermission(viewModel: InventoryViewModel, onEvent: (ProductEvent) -> Unit) {
    val state by viewModel.state.collectAsState()
    val launcherCamera = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            onEvent(ProductEvent.PermissionCamera(isGranted))
        }
    )

    if (!state.permissionCamera) {
        SideEffect {
            launcherCamera.launch(android.Manifest.permission.CAMERA)
        }
    }
}

@Composable
fun InventoryScreen(
    onClick: (String) -> Unit,
    onClickAdd: () -> Unit,
    onClickLogOut: () -> Unit,
    sendIdToProductPage: (String) -> Unit,
    onEvent: (ProductEvent) -> Unit
) {
    val viewModel = hiltViewModel<InventoryViewModel>()
    val stateProduct by viewModel.state.collectAsState()
    val gmail = FirebaseAuth.getInstance().currentUser?.email
    val activity = (LocalContext.current as? Activity)

    val startScan by viewModel.scannerState.collectAsState()

    val openScan = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val snackbarMessage by SnackbarManager.snackbarMessages.collectAsState()


    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            onEvent(ProductEvent.PermissionExternalStorage(isGranted = isGranted))

            if (!isGranted) {
                Toast.makeText(
                    context,
                    "You can change storage permissions in application properties",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
    )

    val launcherContent =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
            Log.d("uri", "$uri")
            onEvent(ProductEvent.ImportInventoriesFromExcel(uri.toString()))
        }

    LaunchedEffect(stateProduct.selectedIdBuilding) {
        onEvent(ProductEvent.StartLoading)
        viewModel.getLocationIdFromDataStore()
        if (stateProduct.selectedIdBuilding.isNotEmpty()) {
            onEvent(ProductEvent.StopLoading)
            onEvent(ProductEvent.ShowProductSelectedBuilding)
        }

    }

    if (stateProduct.toastNotification.isActivated) {
        Toast.makeText(context, stateProduct.toastNotification.message, Toast.LENGTH_SHORT).show()
        viewModel.resetToastNotificationState()
    }


    val shouldShowDialog = remember { mutableStateOf(false) }
    val shouldShowDialogDeleteItems = remember { mutableStateOf(false) }

    BackHandler {
        if (stateProduct.showCheckbox) {
            viewModel.showCheckbox(false)
            viewModel.update(false)
        } else {
            shouldShowDialog.value = true
        }

    }

    LaunchedEffect(startScan.data) {
        if (startScan.data.isNotEmpty()) {
            sendIdToProductPage.invoke(startScan.data)
            onEvent(ProductEvent.CleanerScannerState)
        }
    }


    if (shouldShowDialog.value) {
        DialogWithImage(
            onDismissRequest = { shouldShowDialog.value = false },
            onConfirmation = { activity?.finish() },
            painter = painterResource(id = R.drawable.cat_dialog),
            imageDescription = "cat",
            text = "Вы точно хотите завершить работу?"
        )
    }

    if (shouldShowDialogDeleteItems.value) {
        DialogWithImage(
            onDismissRequest = { shouldShowDialogDeleteItems.value = false },
            onConfirmation = {
                    viewModel.deleteItems {
                        viewModel.update(false)
                    }
                    Toast.makeText(context, "${stateProduct.isSelectedList} delete",Toast.LENGTH_LONG).show()

                shouldShowDialogDeleteItems.value = false
                             },
            painter = painterResource(id = R.drawable.cat_dialog),
            imageDescription = "cat",
            text = "Вы точно хотите удалить объекты?"
        )
        Log.d("spisokSelectedList", "${stateProduct.isSelectedList}")
        Log.d("spisokProduct", "${stateProduct.products}")
    }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f)) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Acc"
                )
            }
            Text(text = "Почта: $gmail")
            Spacer(modifier = Modifier.padding(8.dp))
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "exist",
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clickable {
                        onEvent(ProductEvent.LogOutProfile)
                        onClickLogOut()
                    }
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 56.dp, start = 24.dp, bottom = 24.dp, end = 24.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(Modifier.weight(1f)) {
                TextFieldSearchInventoryNameOrId(
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .fillMaxWidth(),
                    label = {
                        Text(
                            text = "product name or id",
                            color = colorResource(id = R.color.SpanishGrey)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "SearchById"
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorResource(id = R.color.SpanishGrey),
                        unfocusedBorderColor = colorResource(id = R.color.SpanishGrey),
                    ),
                    value = stateProduct.searchText,
                    onValueChange = { onEvent(ProductEvent.SetSearchText(it)) }
                )
            }

            ImageQRScanner {
                openScan.value = !openScan.value
            }

            CameraPermission(viewModel = viewModel, onEvent = onEvent)


        }


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {


            ButtonInventoryScreen(
                modifier = Modifier.size(width = 150.dp, height = 48.dp),
                rowContent = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "addProduct",
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    Text(text = "Product", fontSize = 16.sp)
                },
                onClick = { onClickAdd() }
            )


        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            ButtonInventoryScreen(modifier = Modifier
                .padding(end = 16.dp)
                .size(width = 150.dp, height = 48.dp),
                rowContent = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Export",
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    Text(text = "Export", fontSize = 16.sp)
                },
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        onEvent(ProductEvent.PermissionExternalStorage(isGranted = true))
                    } else {
                        requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                }
            )

            ButtonInventoryScreen(modifier = Modifier
                .size(width = 150.dp, height = 48.dp),
                rowContent = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Import",
                        modifier = Modifier.padding(end = 15.dp)
                    )
                    Text(text = "Import", fontSize = 16.sp)
                }) {
                launcherContent.launch("*/*")
            }

        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Recently added",
                fontSize = 24.sp,
                modifier = Modifier
                    .padding(start = 24.dp, top = 24.dp, bottom = 16.dp)
            )
            if (stateProduct.isSelectedList.isNotEmpty()){
                Text(
                    text = "Delete",
                    fontSize = 16.sp,
                    color = Color.Red,
                    modifier = Modifier
                        .padding(top = 6.dp,end = 30.dp)
                        .bottomBorder(2.dp)
                        .clickable {
                            shouldShowDialogDeleteItems.value = true
                        }
                )
            }
        }


        LazyColumn(
            modifier = Modifier
                .padding(start = 24.dp, end = 24.dp)
                .background(Color.White)
        ) {
            items(stateProduct.products,
                key = { item ->
                    // Ваша логика для генерации ключа на основе элемента `item`
                    item!!.id // Например, если `item` имеет поле `id`, можно использовать его как ключ
                }) { inventories ->
                if (inventories != null) {
                    ListRow(
                        {
                            onClick.invoke(it)
                            Log.d("inventories.id", "${inventories.id}")
                        },
                        inventory = inventories,
                        stateProduct.loading,
                        viewModel,
                    )
                }
            }
        }

    }

    if (openScan.value) {
        onEvent(ProductEvent.StartScan)
        openScan.value = !openScan.value
        Log.d("scannerLogUi", "${startScan.data}")
    }

    SnackBarToast(snackbarMessage = snackbarMessage, context = context)
}

@Composable
fun TextFieldSearchInventoryNameOrId(
    modifier: Modifier,
    label: @Composable () -> Unit,
    leadingIcon: @Composable () -> Unit,
    colors: TextFieldColors,
    value: String,
    onValueChange: (String) -> Unit
) {

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        label = label,
        leadingIcon = leadingIcon,
        colors = colors
    )
}

@Composable
fun ImageQRScanner(onClick: () -> Unit) {
    Image(
        painter = painterResource(id = R.drawable.qr_scanner),
        contentDescription = "qr",
        modifier = Modifier
            .size(48.dp)
            .clickable {
                onClick()
            },
    )
}

@Composable
fun ButtonInventoryScreen(
    modifier: Modifier,
    rowContent: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.RetroBlue)),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            rowContent()
        }
    }
}

@Composable
fun ListRow(
    onClick: (String) -> Unit,
    inventory: Inventory,
    loading: Boolean,
    viewModel: InventoryViewModel,
) {
    val context = LocalContext.current
    val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "my_album")
    val imageFile = File(filePath, inventory.pathToImage!!)
    val placeholder = painterResource(id = R.drawable.add_photo_alternate)
    val showCheck by viewModel.state.collectAsState()
    val isSelectedd =
        remember { mutableStateOf(showCheck.isSelectedList.contains(inventory.id)) }


    if (showCheck.isSelectedList.isEmpty()) {
        isSelectedd.value = false
    }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .wrapContentHeight()
            .fillMaxWidth()
            .background(colorResource(id = R.color.AdamantineBlue))
            .pointerInput(UInt) {
                detectTapGestures(
                    onTap = {
                        onClick(inventory.id)
                    },
                    onLongPress = {
                        viewModel.showCheckbox(true)
                    }
                )
            }
            .run {
                if (loading) shimmer() else this
            }
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically) {
            if (imageFile.exists()) {
                AsyncImage(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .run {
                            if (loading) shimmer() else this
                        },
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = placeholder,
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(5.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            Text(
                text = inventory.name,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        Box(Modifier
            .padding(end = 8.dp)) {
            if (showCheck.showCheckbox) {
                Checkbox(
                    checked = isSelectedd.value,
                    onCheckedChange = {
                        isSelectedd.value = it
                        viewModel.addListSelected(inventory, it)
                    },
                )
            }
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Inventory,
        BottomBarScreen.Category,
        BottomBarScreen.Building,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomBarDestination = screens.any { it.route == currentDestination?.route }
    if (bottomBarDestination) {
        BottomNavigation(
            modifier = Modifier.border(
                border = BorderStroke(0.dp, colorResource(id = R.color.RetroBlue)),
                shape = RoundedCornerShape(8.dp)
            ),
            backgroundColor = colorResource(id = R.color.RetroBlue),
            contentColor = colorResource(id = R.color.SpanishGrey)

        ) {
            screens.forEach { screen ->
                AddItem(
                    screen = screen,
                    currentDestination = currentDestination,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    BottomNavigationItem(
        icon = {
            screen.icon()
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        unselectedContentColor = colorResource(id = R.color.SpanishGrey),
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                // Restore state when reselecting a previously selected item
                //restoreState = true
            }
        },
        selectedContentColor = Color.White
    )
}


