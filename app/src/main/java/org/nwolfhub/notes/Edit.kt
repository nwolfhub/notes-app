package org.nwolfhub.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScopeInstance.weight
import androidx.compose.foundation.layout.FlowRowScopeInstance.weight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScopeInstance.weight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.nwolfhub.notes.ui.theme.NotesTheme

class Edit : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                }
            }
        }
    }

    @Composable
    @Preview
    fun mainEditField() {
        var text by rememberSaveable { mutableStateOf("Text") }
        Box() {
            Row(Modifier
                .align(Alignment.TopStart)) {
                TextField(
                    value = "",
                    onValueChange = {
                        text = it
                    },
                    placeholder = {
                        Text("And so it begins")
                    },
                    modifier = Modifier
                        .height(5.dp)
                        .fillMaxWidth()
                )
            }
            Spacer(Modifier.weight())
            Row(Modifier
                .align(Alignment.BottomStart)) {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()

                ) {
                    Text("Save")
                }
            }
        }

    }


}
