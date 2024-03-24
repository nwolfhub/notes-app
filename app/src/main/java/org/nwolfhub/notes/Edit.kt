package org.nwolfhub.notes

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import org.nwolfhub.notes.model.Note
import org.nwolfhub.notes.ui.theme.NotesTheme
import org.nwolfhub.notes.util.NotesStorage

class Edit : ComponentActivity() {
    lateinit var activeNotePref: SharedPreferences
    lateinit var cachePref: SharedPreferences
    lateinit var note: Note
    var noteText = ""
    var noteName = ""
    val showDialog = mutableListOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activeNotePref = getSharedPreferences("active_note", MODE_PRIVATE)
        cachePref = getSharedPreferences("cached_notes", MODE_PRIVATE)
        note = Gson().fromJson(activeNotePref.getString("active", null), Note::class.java)
        val prev =
            cachePref.getString(note.getServerAddr() + note.getOwner().getId() + note.getId(), null)
        var prevText = ""
        noteName = note.getName()
        noteText = note.getContent()
        prevText = noteText
        if (prev != null) {
            prevText = prev
        }
        setContent {
            NotesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        ActionsPanel(noteName = note.getName())
                        MainEditField(prevText)
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
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            TextButton(
                onClick = {
                    Toast.makeText(
                        this@Edit,
                        "Я обязательно сделаю эту кнопку...",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://cs11.pikabu.ru/post_img/2020/07/16/6/og_og_15948909712400651.jpg")
                        )
                    )
                },
                Modifier.wrapContentWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Localized description")
            }
            /*Text(text = noteName, fontSize = 15.sp, textAlign = TextAlign.Center, color = Color.White, modifier = Modifier
                .weight(1f)
                .fillMaxWidth())*/
            var name by rememberSaveable { mutableStateOf(noteName) }
            TextField(value = name, placeholder = {
                Text(text = "Name")
            }, onValueChange = {
                name = it
                this@Edit.noteName = it
            })
        }
    }

    @Composable
    fun MainEditField(previous: String) {
        var text by rememberSaveable { mutableStateOf(previous) }
        Column() {
            Row(
                Modifier
                    .weight(1f, true)
            ) {
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        noteText = it
                        cacheText(text)
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
                    .wrapContentHeight()
            ) {
                Button(
                    onClick = {
                        if (noteText == "") {
                            noteText = " "
                        }
                        val notesStorage = NotesStorage(
                            getSharedPreferences("notes_updated", MODE_PRIVATE),
                            getSharedPreferences("sync", MODE_PRIVATE)
                        )
                        notesStorage.setLocalNote(
                            note.setContent(text).setName(noteName)
                                .setSyncState(Note.SyncState.local)
                        )
                        cachePref.edit().remove(
                            note.getServerAddr() + note.getOwner().getId() + note.getId()
                        ).apply()
                        startActivity(Intent(this@Edit, Notes::class.java))
                        finish()
                    },
                    modifier = Modifier.fillMaxWidth()

                ) {
                    Text("Save")
                }
            }
        }
    }

    private fun cacheText(text: String) {
        cachePref.edit()
            .putString(note.getServerAddr() + note.getOwner().getId() + note.getId(), text).apply()
    }

    @Composable
    fun AlertDialogFactory(
        title: String,
        content: String?,
        button: @Composable () -> Unit
    ) {
        AlertDialog(onDismissRequest = {},
            confirmButton = { button },
            title = { Text(text = title) },
            text = {
                if (content != null)
                    Text(text = content)
                else {
                }
            })
    }

}
