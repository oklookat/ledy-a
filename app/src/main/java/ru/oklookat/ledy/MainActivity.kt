package ru.oklookat.ledy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import ru.oklookat.ledy.ui.theme.LedyTheme


lateinit var cl: Client

class MainActivity : ComponentActivity() {

    private lateinit var sf: ServerFinder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            val ctx = this
            var isConnected by remember {
                mutableStateOf(false)
            }

            val onError: (Throwable) -> Unit = {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        it.message.orEmpty(),
                        withDismissAction = true,
                    )
                }
            }

            LedyTheme {
                TheScaffold(snackbarHostState) {
                    LaunchedEffect(Unit) {
                        cl = Client(ctx, {
                            isConnected = true
                        }, {
                            isConnected = false
                        })
                    }

                    if (!isConnected) {
                        ServerFinderScreen()
                        return@TheScaffold
                    }

                    NavHost(navController, startDestination = "root") {
                        composable("root") {
                            SetColorsScreen()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TheScaffold(
    snackbarHostState: SnackbarHostState,
    content: @Composable() (ColumnScope.() -> Unit)
) {
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
//                NavigationBarItem(
//                    icon = { Icon(Icons.Filled.Favorite, contentDescription = "item") },
//                    label = { Text("item") },
//                    selected = true,
//                    onClick = {  }
//                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding),
        ) {
            content(this)
        }
    }
}


