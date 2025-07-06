package fr.imacaron

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.composables.icons.lucide.CircleMinus
import com.composables.icons.lucide.CirclePlus
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Save
import fr.imacaron.torri.CommandViewModel
import fr.imacaron.torri.Item
import fr.imacaron.torri.ItemsViewModel
import fr.imacaron.torri.isHeightAtLeast
import fr.imacaron.torri.isWidthAtLeast
import fr.imacaron.torri.ui.AppTheme
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import torri.composeapp.generated.resources.Res
import torri.composeapp.generated.resources.allDrawableResources

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
    Card(Modifier.clickable { add() }) {
        Box(Modifier.padding(4.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painterResource(Res.allDrawableResources.getValue(item.image)),
                    contentDescription = "Image de ${item.name}"
                )
                Text(item.name)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Button(onClick = { if(total > 0) remove() }) {
                        Icon(Lucide.CircleMinus, contentDescription = "Retirer un article", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Button(onClick = add) {
                        Icon(Lucide.CirclePlus, contentDescription = "Ajouter un article", tint = MaterialTheme.colorScheme.onPrimary)
                    }
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
fun App(dataStore: DataStore<Preferences>, windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass) {
    val itemsViewModel = viewModel { ItemsViewModel(dataStore) }
    val commandViewModel = viewModel { CommandViewModel(dataStore) }
    val displaySidePanel = windowSizeClass.isWidthAtLeast(WindowWidthSizeClass.EXPANDED) && windowSizeClass.isHeightAtLeast(
        WindowHeightSizeClass.MEDIUM) || windowSizeClass.isWidthAtLeast(WindowWidthSizeClass.MEDIUM) && windowSizeClass.isHeightAtLeast(
        WindowHeightSizeClass.EXPANDED)
    val cols = when(windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> 2
        WindowWidthSizeClass.MEDIUM -> 3
        WindowWidthSizeClass.EXPANDED -> 3
        else -> 2
    }
    val portrait = LocalWindowInfo.current.containerSize.let {
        it.width < it.height
    }
    LifecycleStartEffect(Unit) {
        onStopOrDispose {
            GlobalScope.launch {
                itemsViewModel.save()
                commandViewModel.save()
            }
        }
    }
    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    { Text("Torri") },
                    actions = {
                        IconButton({
                            GlobalScope.launch {
                                itemsViewModel.reset()
                                commandViewModel.reset()
                            }
                        }) {
                            Icon(Lucide.RefreshCw, contentDescription = "Réinitialiser", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton({
                            saveToFile("vente.csv", itemsViewModel.toCSV())
                        }) {
                            Icon(Lucide.Save, contentDescription = "Sauvegarder", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            }
        ) {
            if(portrait) {
                Column(Modifier.padding(it)) {
                    CommandScreen(cols, itemsViewModel, commandViewModel, displaySidePanel)
                }
            } else {
                Row(Modifier.padding(it), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    CommandScreen(cols, itemsViewModel, commandViewModel, displaySidePanel, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CommandScreen(cols: Int, itemsViewModel: ItemsViewModel, commandViewModel: CommandViewModel, displaySidePanel: Boolean, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        GridCells.Fixed(cols),
        contentPadding = PaddingValues(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        items(itemsViewModel.items) { item ->
            ItemView(
                item,
                itemsViewModel.itemsTotal[item.name]!!,
                {
                    itemsViewModel.add(item.name)()
                    commandViewModel.add(item)
                },
                {
                    itemsViewModel.remove(item.name)()
                    commandViewModel.remove(item)
                }
            )
        }
    }
    if(displaySidePanel) {
        val total = commandViewModel.command.values.sum()
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Card(Modifier.padding(4.dp).fillMaxWidth()) {
                Text("Commande en cours :", style = MaterialTheme.typography.displaySmall)
                if(total == 0) {
                    Text("Aucun article")
                }
                commandViewModel.command.filter { it.value > 0 }.forEach { (item, total) ->
                    Text("${item.name} : x$total")
                }
            }
            Card(Modifier.padding(4.dp).fillMaxWidth()) {
                Text("Total : $total article${if(total > 1) "s" else ""}")
                Button({ commandViewModel.reset() } ) {
                    Text("Valider")
                }
            }
        }
    }
}