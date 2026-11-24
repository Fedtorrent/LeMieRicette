package com.fulvio.lemiericette.presentation.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Definizione delle rotte della schermata dell'applicazione.
 */
sealed class Screen(val route: String) {
    data object CategoryList : Screen("categories")
    data object RecipeList : Screen("recipes/{categoryId}") {
        fun createRoute(categoryId: String): String = "recipes/$categoryId"
    }
    data object RecipeDetail : Screen("recipe/{recipeId}") {
        fun createRoute(recipeId: String): String = "recipe/$recipeId"
    }
    data object RecipeForm : Screen("recipe_form?recipeId={recipeId}") {
        fun createRoute(recipeId: String? = null): String =
            if (!recipeId.isNullOrBlank()) "recipe_form?recipeId=$recipeId" else "recipe_form"
    }
    data object WebImport : Screen("web_import?initialQuery={initialQuery}&sharedUrl={sharedUrl}") {
        fun createRoute(initialQuery: String? = null, sharedUrl: String? = null): String {
            val queryParam = if (!initialQuery.isNullOrBlank()) "initialQuery=${URLEncoder.encode(initialQuery, StandardCharsets.UTF_8.name())}" else ""
            val urlParam = if (!sharedUrl.isNullOrBlank()) "sharedUrl=${URLEncoder.encode(sharedUrl, StandardCharsets.UTF_8.name())}" else ""
            val params = listOf(queryParam, urlParam).filter { it.isNotBlank() }.joinToString("&")
            return if (params.isNotBlank()) "web_import?$params" else "web_import"
        }
    }
    data object Search : Screen("search?query={query}") {
        fun createRoute(query: String? = null): String =
            if (!query.isNullOrBlank()) "search?query=${URLEncoder.encode(query, StandardCharsets.UTF_8.name())}" else "search"
    }
    data object Settings : Screen("settings")
}
