package fr.imacaron

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Save
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import torri.composeapp.generated.resources.Res
import torri.composeapp.generated.resources.allDrawableResources

data class Item(val name: String, val image: String)

class ItemsViewModel(private val dataStore: DataStore<Preferences>): ViewModel() {
    val items = data
    val itemsTotal = mutableStateMapOf<String, Int>()

    init {
        GlobalScope.launch {
            println("ICI")
            dataStore.data.collect { pref ->
                println("Collect")
                items.forEach { item ->
                    itemsTotal[item.name] = pref[intPreferencesKey(item.name)] ?: 0
                }
                println("FIN")
            }
            println("Sortie")
        }
    }

    fun add(name: String): () -> Unit {
        return {
            itemsTotal[name] = itemsTotal[name]!! + 1
        }
    }

    fun remove(name: String): () -> Unit {
        return {
            if(itemsTotal[name]!! > 0) {
                itemsTotal[name] = itemsTotal[name]!! - 1
            }
        }
    }

    suspend fun save() {
        dataStore.updateData {
            it.toMutablePreferences().apply {
                items.forEach { item ->
                    this[intPreferencesKey(item.name)] = itemsTotal[item.name]!!
                }
            }
        }
    }

    suspend fun reset() {
        itemsTotal.forEach { (key, _) -> itemsTotal[key] = 0 }
        save()
    }

    fun toCSV(): String {
        return "Article;Total\n" + itemsTotal.map { (key, value) -> "$key;$value" }.joinToString("\n")
    }
}

val data = listOf(
    Item("Sandwich Jambon", "jambon"),
    Item("Sandwich Poulet", "poulet"),
    Item("Glace", "glace"),
    Item("Popcorn", "popcorn"),
    Item("Gateau", "gateau"),
    Item("Canette", "canette")
)

@Composable
fun ItemView(item: Item, total: Int, add: () -> Unit, remove: () -> Unit) {
    Card {
        Box(Modifier.padding(4.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painterResource(Res.allDrawableResources.getValue(item.image)),
                    contentDescription = "Image de ${item.name}"
                )
                Text(item.name)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Button(onClick = { if(total > 0) remove() }) { Text("-") }
                    Button(onClick = add) { Text("+") }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text("$total")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(dataStore: DataStore<Preferences>) {
    val itemsViewModel = viewModel { ItemsViewModel(dataStore) }
    LifecycleStartEffect(Unit) {
        onStopOrDispose {
            GlobalScope.launch {
                itemsViewModel.save()
            }
        }
    }
    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    { Text("Torri") },
                    actions = {
                        IconButton({
                            GlobalScope.launch {
                                itemsViewModel.reset()
                            }
                        }) {
                            Image(Lucide.RefreshCw, "reset")
                        }
                        IconButton({
                            saveToFile("vente.csv", itemsViewModel.toCSV())
                        }) {
                            Image(Lucide.Save, "save")
                        }
                    }
                )
            }
        ) {
            LazyVerticalGrid(
                GridCells.Fixed(2),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(it)
            ) {
                items(itemsViewModel.items) { item ->
                    ItemView(
                        item,
                        itemsViewModel.itemsTotal[item.name]!!,
                        itemsViewModel.add(item.name),
                        itemsViewModel.remove(item.name)
                    )
                }
            }
        }
    }
}