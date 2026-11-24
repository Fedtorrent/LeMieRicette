package com.fulvio.lemiericette.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.fulvio.lemiericette.presentation.category.CategoryListScreen
import com.fulvio.lemiericette.presentation.recipe_detail.RecipeDetailScreen
import com.fulvio.lemiericette.presentation.recipe_form.RecipeFormScreen
import com.fulvio.lemiericette.presentation.recipe_list.RecipeListScreen
import com.fulvio.lemiericette.presentation.search.SearchScreen
import com.fulvio.lemiericette.presentation.settings.SettingsScreen
import com.fulvio.lemiericette.presentation.web_import.WebImportScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.CategoryList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Schermata principale: Categorie
        composable(Screen.CategoryList.route) {
            CategoryListScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate(Screen.RecipeList.createRoute(categoryId))
                },
                onAddRecipeClick = {
                    navController.navigate(Screen.RecipeForm.createRoute(null))
                },
                onWebImportClick = {
                    navController.navigate(Screen.WebImport.createRoute(null))
                },
                onSearchClick = {
                    navController.navigate(Screen.Search.createRoute(null))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Schermata lista ricette per categoria
        composable(
            route = Screen.RecipeList.route,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) {
            RecipeListScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Schermata dettaglio ricetta
        composable(
            route = Screen.RecipeDetail.route,
            arguments = listOf(navArgument("recipeId") { type = NavType.StringType })
        ) {
            RecipeDetailScreen(
                onEditClick = { id ->
                    navController.navigate(Screen.RecipeForm.createRoute(id))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Form inserimento/modifica manuale
        composable(
            route = Screen.RecipeForm.route,
            arguments = listOf(
                navArgument("recipeId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getString("recipeId")
            RecipeFormScreen(
                recipeId = recipeId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Browser per importazione web
        composable(
            route = Screen.WebImport.route,
            arguments = listOf(
                navArgument("initialQuery") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("sharedUrl") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            WebImportScreen(
                onBackClick = {
                    if (!navController.popBackStack()) {
                        navController.navigate(Screen.CategoryList.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Schermata Ricerca & Filtri globale
        composable(
            route = Screen.Search.route,
            arguments = listOf(
                navArgument("query") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            SearchScreen(
                onRecipeClick = { recipeId ->
                    navController.navigate(Screen.RecipeDetail.createRoute(recipeId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Schermata Impostazioni & Sync
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
