package ru.oklookat.ledy

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ServerFinderScreen() {
    Text(
        modifier = Modifier.padding(8.dp),
        text = """
Connecting to ledy-server... 

If this message does not disappear within 10 seconds, make sure that you and ledy-server are connected to the same Wi-Fi network. Try rebooting ledy-server and/or this application.

Initial setup? Click the button below.
    """.trimIndent()
    )
}