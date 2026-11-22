package com.pelodeck.ui.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject

private data class ClassSearch(val title: String, val query: String)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClassSelectDialog(onDismiss: () -> Unit, onSearch: (String) -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("class_searches", 0) }
    val saved = remember {
        mutableStateListOf<ClassSearch>().apply {
            runCatching {
                val array = JSONArray(prefs.getString("saved", "[]"))
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(ClassSearch(item.getString("title"), item.getString("query")))
                }
            }
        }
    }
    fun persist() {
        val array = JSONArray()
        saved.forEach { array.put(JSONObject().put("title", it.title).put("query", it.query)) }
        prefs.edit().putString("saved", array.toString()).apply()
    }
    var category by remember { mutableStateOf("Just Spin") }
    var title by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    val choices = when (category) {
        "Just Spin" -> listOf(15, 20, 30, 45).map { ClassSearch("$it minute spin", "$it minute indoor cycling spin class") }
        "Arms and Spin" -> listOf(
            ClassSearch("Biceps", "spin class cycling biceps arm workout"),
            ClassSearch("Triceps", "spin class cycling triceps arm workout"),
            ClassSearch("Shoulders", "spin class cycling shoulders arm workout"),
            ClassSearch("Upper body and arms", "spin class cycling upper body arm workout"),
            ClassSearch("Arms after cycling", "post cycling arm workout dumbbells")
        )
        "Yoga / Stretching" -> listOf(
            ClassSearch("Yoga With Adriene", "Yoga With Adriene yoga class"),
            ClassSearch("Yoga with Kassandra", "Yoga with Kassandra yoga class"),
            ClassSearch("SarahBethYoga", "SarahBethYoga yoga class"),
            ClassSearch("Breathe and Flow", "Breathe and Flow yoga class"),
            ClassSearch("Move With Nicole", "Move With Nicole yoga stretch")
        )
        else -> saved.toList()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Class Select") },
        text = {
            Column(Modifier.fillMaxWidth().heightIn(max = 540.dp)) {
                Text("Choose a search, then pick a video on YouTube.", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                androidx.compose.foundation.layout.FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Just Spin", "Arms and Spin", "Yoga / Stretching", "My Searches").forEach { name ->
                        FilterChip(selected = category == name, onClick = { category = name }, label = { Text(name) })
                    }
                }
                if (category == "My Searches") {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("YouTube search or teacher") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    TextButton(enabled = title.isNotBlank() && query.isNotBlank(), onClick = {
                        saved.add(ClassSearch(title.trim(), query.trim())); persist(); title = ""; query = ""
                    }) { Text("Save search") }
                    if (saved.isEmpty()) Text("Save a teacher or search to find it again here.")
                }
                LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                    items(choices) { choice ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { onSearch(choice.query) }, modifier = Modifier.weight(1f)) {
                                Text(choice.title, modifier = Modifier.fillMaxWidth())
                            }
                            if (category == "My Searches") TextButton(onClick = { saved.remove(choice); persist() }) { Text("Remove") }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
