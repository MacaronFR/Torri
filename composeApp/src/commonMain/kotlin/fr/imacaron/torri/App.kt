package fr.imacaron.torri

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.savedstate.read
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Cog
import com.composables.icons.lucide.Command
import com.composables.icons.lucide.DollarSign
import com.composables.icons.lucide.Inbox
import com.composables.icons.lucide.Lucide
import fr.imacaron.torri.components.AppBar
import fr.imacaron.torri.components.BottomBar
import fr.imacaron.torri.components.FAB
import fr.imacaron.torri.components.SideBar
import fr.imacaron.torri.data.AppDataBase
import fr.imacaron.torri.data.PriceListItemEntity
import fr.imacaron.torri.data.PriceListWithItem
import fr.imacaron.torri.screen.CommandDetailScreen
import fr.imacaron.torri.screen.CommandScreen
import fr.imacaron.torri.screen.ConfScreen
import fr.imacaron.torri.screen.ItemAddScreen
import fr.imacaron.torri.screen.ItemScreen
import fr.imacaron.torri.screen.LoadingScreen
import fr.imacaron.torri.screen.LoginScreen
import fr.imacaron.torri.screen.PriceListAddScreen
import fr.imacaron.torri.screen.PriceListEditScreen
import fr.imacaron.torri.screen.PriceListScreen
import fr.imacaron.torri.screen.ServiceAddScreen
import fr.imacaron.torri.screen.ServiceDetailScreen
import fr.imacaron.torri.screen.ServiceScreen
import fr.imacaron.torri.ui.AppTheme
import fr.imacaron.torri.viewmodel.CommandViewModel
import fr.imacaron.torri.viewmodel.PriceListViewModel
import fr.imacaron.torri.viewmodel.SavedItemViewModel
import fr.imacaron.torri.viewmodel.ServiceViewModel
import io.ktor.client.HttpClient

val activated = booleanPreferencesKey("activated")
val clientKey = stringPreferencesKey("clientID")
val sumupAccessToken = stringPreferencesKey("sumupAccessToken")
val sumupRefreshToken = stringPreferencesKey("sumupRefreshToken")
val sumupExpire = longPreferencesKey("sumupExpire")

enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    SERVICE("service", "Service", Lucide.BookOpen),
    SERVICE_DETAIL("service/detail/{id}", "Détail du service", Lucide.BookOpen),
    SERVICE_ADD("service/add", "Ajouter un service", Lucide.BookOpen),
    SERVICE_COMMAND("service/command", "Commande", Lucide.Command),
    SERVICE_COMMAND_DETAIL("service/command/detail", "Détail des commandes", Lucide.Command),
    PRICE_LIST("pricelist", "Tarifs", Lucide.DollarSign),
    PRICE_LIST_ADD("pricelist/add", "Ajouter un tarif", Lucide.DollarSign),
    PRICE_LIST_EDIT("pricelist/edit/{id}", "Modifier un tarif", Lucide.DollarSign),
    ITEMS("items", "Produits", Lucide.Inbox),
    ITEMS_ADD("items/add", "Ajouter un produit", Lucide.Inbox),
    CONF("conf", "Configuration", Lucide.Cog),
    CONF_SUMUP_LOGIN("conf/sumup/login", "Connexion SumUp", Lucide.Cog);

    fun routeWithArg(vararg args: Pair<String, String>): String {
        var route = this.route
        args.forEach { route = route.replace("{${it.first}}", it.second) }
        return route
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(dataBase: AppDataBase, dataStore: DataStore<Preferences>, windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass, client: HttpClient, nearby: Nearby) {
    var reloadConfScreen by remember { mutableStateOf(false) }
    val snackBarState = remember { SnackbarHostState() }
    SumUp.onLogin = { isLogged ->
        if(!isLogged) {
            dataStore.data.collect {
                if(it[sumupRefreshToken] != null) {
                    val newTokens = SumUp.refreshToken(it[sumupRefreshToken]!!)
                    dataStore.updateData { pref ->
                        pref.toMutablePreferences().apply {
                            if(newTokens == null) {
                                remove(sumupAccessToken)
                                remove(sumupRefreshToken)
                                remove(sumupExpire)
                                snackBarState.showSnackbar("Reconnexion à SumUp impossible")
                            } else {
                                set(sumupAccessToken, newTokens.access_token)
                                newTokens.refresh_token?.let { refreshToken -> set(sumupRefreshToken, refreshToken)}
                                newTokens.expires_in?.let { expiresIn -> set(sumupExpire, newTokens.received_at + expiresIn) }
                            }
                        }
                    }
                    newTokens?.let { SumUp.login(it.access_token) }
                } else {
                    snackBarState.showSnackbar("Session SumUp expirée")
                }
            }
        }
        reloadConfScreen = !reloadConfScreen
    }
    SumUp.init()
    var loggedIn by remember { mutableStateOf<Boolean?>(null) }
    val licenceRegistration = LicenceRegistration(client)
    LaunchedEffect(dataStore) {
        dataStore.data.collect {
            if(it[activated] != true) {
                loggedIn = false
            } else {
                licenceRegistration.validate(it[clientKey] ?: "").onSuccess {
                    loggedIn = true
                }.onFailure {
                    loggedIn = false
                    dataStore.updateData { pref ->
                        pref.toMutablePreferences().apply {
                            set(activated, false)
                            set(clientKey, "")
                        }
                    }
                }
            }
            if(it[sumupAccessToken] != null && !SumUp.isLogged) {
                SumUp.login(it[sumupAccessToken]!!)
            } else {
                dataStore.updateData { pref ->
                    pref.toMutablePreferences().apply {
                        remove(sumupAccessToken)
                        remove(sumupRefreshToken)
                        remove(sumupExpire)
                    }
                }
            }
        }
    }
    val savedItems = viewModel { SavedItemViewModel(dataBase) }
    val priceList = viewModel { PriceListViewModel(dataBase) }
    val commandViewModel = viewModel { CommandViewModel(dataBase) }
    val serviceViewModel = viewModel { ServiceViewModel(dataBase, commandViewModel, savedItems) }
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
    SumUp.snackBarState = snackBarState
    AppTheme {
        if(loggedIn == false) {
            LoginScreen(dataStore, licenceRegistration)
        } else if(loggedIn == null) {
            LoadingScreen()
        } else {
            var items: PriceListWithItem? by remember { mutableStateOf(null) }
            var prices: List<PriceListItemEntity> by remember { mutableStateOf(emptyList()) }
            LaunchedEffect(serviceViewModel.currentService) {
                items = priceList.priceLists.find { it.priceList.idPriceList == serviceViewModel.currentService?.idPriceList }
            }
            LaunchedEffect(priceList, items) {
                items?.let {
                    prices = priceList.getPriceListItems(it.priceList)
                }
            }
            Scaffold(
                topBar = { AppBar(navigationController, serviceViewModel, dataStore) },
                bottomBar = { if (!displaySidePanel) { BottomBar(navigationController) } },
                floatingActionButton = { if (!displaySidePanel) { FAB(navigationController, commandViewModel, items, prices) } },
                snackbarHost = { SnackbarHost(snackBarState) }
            ) {
                Row(Modifier.padding(it)) {
                    if (displaySidePanel) {
                        SideBar(navigationController)
                    }
                    NavHost(
                        navigationController,
                        startDestination = Destination.SERVICE.route
                    ) {
                        composable(Destination.SERVICE.route) { ServiceScreen(serviceViewModel, priceList, navigationController) }
                        composable(Destination.SERVICE_DETAIL.route) { backStackEntry ->
                            val id = backStackEntry.arguments?.read {
                                this.getStringOrNull("id")?.toLongOrNull()
                            } ?: 0L
                            ServiceDetailScreen(serviceViewModel, commandViewModel, savedItems, priceList, id)
                        }
                        composable(Destination.SERVICE_ADD.route) { ServiceAddScreen(priceList, serviceViewModel, navigationController) }
                        composable(Destination.SERVICE_COMMAND.route) { CommandScreen(cols, displaySidePanel, serviceViewModel, navigationController, priceList, commandViewModel, portrait) }
                        composable(Destination.SERVICE_COMMAND_DETAIL.route) { CommandDetailScreen(commandViewModel, priceList, savedItems) }
                        composable(Destination.PRICE_LIST.route) { PriceListScreen(priceList, navigationController) }
                        composable(Destination.PRICE_LIST_ADD.route) { PriceListAddScreen(priceList, savedItems, navigationController) }
                        composable(Destination.PRICE_LIST_EDIT.route) { backStackEntry ->
                            val id = backStackEntry.arguments?.read {
                                this.getStringOrNull("id")?.toLongOrNull()
                            } ?: 0L
                            PriceListEditScreen(priceList, savedItems, navigationController, id, serviceViewModel, snackBarState)
                        }
                        composable(Destination.ITEMS.route) { ItemScreen(savedItems, priceList, snackBarState) }
                        composable(Destination.ITEMS_ADD.route) { ItemAddScreen(savedItems, navigationController) }
                        composable(Destination.CONF.route) { ConfScreen(dataStore, snackBarState, reloadConfScreen, { reloadConfScreen = !reloadConfScreen}, nearby) }
                    }
                }
            }
        }
    }
}