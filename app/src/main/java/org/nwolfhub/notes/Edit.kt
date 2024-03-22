package org.nwolfhub.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.gson.Gson
import org.nwolfhub.notes.model.Note
import org.nwolfhub.notes.ui.theme.NotesTheme

class Edit : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activeNotePref = getSharedPreferences("active_note", MODE_PRIVATE)
        val note = Gson().fromJson(activeNotePref.getString("active", null), Note::class.java)
        setContent {
            NotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        ActionsPanel(noteName = note.getName())
                        mainEditField()
                    }
                }
            }
        }
    }

    @Composable
    fun ActionsPanel(noteName: String) {
        Row(
            Modifier
                .background(Color.Black)
                .wrapContentHeight().fillMaxWidth()) {
            TextButton(onClick = { /*TODO*/ }, Modifier.wrapContentWidth()) {
                Icon(Icons.Default.Settings, contentDescription = "Localized description")
            }
            Text(text = noteName, color = Color.White, modifier = Modifier
                .weight(1f)
                .fillMaxWidth())
        }
    }

    @Composable
    @Preview
    fun mainEditField():String {
        var text by rememberSaveable { mutableStateOf("Text") }
        Column(){
            Row(
                Modifier
                    .weight(1f, true)) {
                TextField(
                    value = "",
                    onValueChange = {
                        text = it
                    },
                    placeholder = {
                        Text("And so it begins")
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth()
                )
            }
            Row(
                Modifier
                    .wrapContentHeight()) {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()

                ) {
                    Text("Save")
                }
            }
        }
        return text
    }


}
