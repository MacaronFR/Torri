package fr.imacaron.torri

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import fr.imacaron.torri.ui.AppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

val data = listOf(
    Item("Sandwich Jambon", "jambon"),
    Item("Sandwich Poulet", "poulet"),
    Item("Glace", "glace"),
    Item("Popcorn", "popcorn"),
    Item("Gateau", "gateau"),
    Item("Canette", "canette")
)

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
            itemsViewModel.save()
            commandViewModel.save()
        }
    }
    AppTheme {
        Scaffold(
            topBar = { AppBar(itemsViewModel, commandViewModel) }
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