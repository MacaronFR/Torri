package fr.imacaron.torri

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Command
import com.composables.icons.lucide.DollarSign
import com.composables.icons.lucide.Inbox
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.components.AppBar
import fr.imacaron.torri.components.BottomBar
import fr.imacaron.torri.components.FAB
import fr.imacaron.torri.components.SideBar
import fr.imacaron.torri.data.AppDataBase
import fr.imacaron.torri.screen.CommandDetailScreen
import fr.imacaron.torri.screen.CommandScreen
import fr.imacaron.torri.screen.ItemAddScreen
import fr.imacaron.torri.screen.ItemScreen
import fr.imacaron.torri.screen.PriceListAddScreen
import fr.imacaron.torri.screen.PriceListEditScreen
import fr.imacaron.torri.screen.PriceListScreen
import fr.imacaron.torri.screen.ServiceAddScreen
import fr.imacaron.torri.screen.ServiceScreen
import fr.imacaron.torri.ui.AppTheme
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.SavedItemViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

val data = listOf(
    Item("Sandwich Jambon", "jambon"),
    Item("Sandwich Poulet", "poulet"),
    Item("Glace", "glace"),
    Item("Popcorn", "popcorn"),
    Item("Gateau", "gateau"),
    Item("Canette", "canette")
)

enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    ITEMS("items", "Produits", Lucide.Inbox),
    ITEMS_ADD("items/add", "Ajouter un produit", Lucide.Inbox),
    PRICE_LIST("pricelist", "Tarifs", Lucide.DollarSign),
    PRICE_LIST_ADD("pricelist/add", "Ajouter un tarif", Lucide.DollarSign),
    PRICE_LIST_EDIT("pricelist/edit/{id}", "Modifier un tarif", Lucide.DollarSign),
    SERVICE("service", "Service", Lucide.BookOpen),
    SERVICE_ADD("service/add", "Ajouter un service", Lucide.BookOpen),
    SERVICE_COMMAND("service/command", "Commande", Lucide.Command),
    SERVICE_COMMAND_DETAIL("service/command/detail", "Détail des commandes", Lucide.Command);

    fun routeWithArg(vararg args: Pair<String, String>): String {
        var route = this.route
        args.forEach { route = route.replace("{${it.first}}", it.second) }
        return route
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App(dataBase: AppDataBase, windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass) {
    val savedItems = viewModel { SavedItemViewModel(dataBase) }
    val priceList = viewModel { PriceListViewModel(dataBase) }
    val serviceViewModel = viewModel { ServiceViewModel(dataBase) }
    val commandViewModel = viewModel { CommandViewModel(dataBase) }
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
    val navigationController = rememberNavController()
    AppTheme {
        Scaffold(
            topBar = { AppBar(navigationController, serviceViewModel, commandViewModel) },
            bottomBar = { if(!displaySidePanel) { BottomBar(navigationController) } },
            floatingActionButton = { if(!displaySidePanel) { FAB(navigationController, commandViewModel) } },
        ) {
            if(displaySidePanel) {
                SideBar(it)
            }
            NavHost(
                navigationController,
                startDestination = Destination.SERVICE.route,
                Modifier.padding(it)
            ) {
                composable(Destination.SERVICE_COMMAND.route) { CommandScreen(cols, displaySidePanel, serviceViewModel, navigationController, priceList, commandViewModel) }
                composable(Destination.ITEMS.route) { ItemScreen(savedItems) }
                composable(Destination.ITEMS_ADD.route) { ItemAddScreen(savedItems, navigationController) }
                composable(Destination.PRICE_LIST.route) { PriceListScreen(priceList, navigationController) }
                composable(Destination.PRICE_LIST_ADD.route) { PriceListAddScreen(priceList, savedItems,navigationController) }
                composable(Destination.PRICE_LIST_EDIT.route) { backStackEntry ->
                    val id = backStackEntry.arguments?.read {
                        this.getStringOrNull("id")?.toLongOrNull()
                    } ?: 0L
                    PriceListEditScreen(priceList, savedItems,navigationController, id)
                }
                composable(Destination.SERVICE.route) { ServiceScreen(serviceViewModel, priceList, navigationController) }
                composable(Destination.SERVICE_ADD.route) { ServiceAddScreen(priceList, serviceViewModel, navigationController) }
                composable(Destination.SERVICE_COMMAND_DETAIL.route) { CommandDetailScreen(commandViewModel, priceList, savedItems) }
            }
        }
    }
}