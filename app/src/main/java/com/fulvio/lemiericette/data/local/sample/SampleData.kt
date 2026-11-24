package com.fulvio.lemiericette.data.local.sample

import com.fulvio.lemiericette.domain.model.Category
import com.fulvio.lemiericette.domain.model.Nutrition
import com.fulvio.lemiericette.domain.model.Recipe

/**
 * Ricette di esempio per popolare il database al primo avvio dell'applicazione.
 */
object SampleData {
    val sampleRecipes = listOf(
        Recipe(
            id = "sample-carbonara",
            categoryId = Category.PRIMI.id,
            title = "Spaghetti alla Carbonara",
            description = "Un classico della tradizione romana a base di guanciale, uova, pecorino romano e pepe nero.",
            prepTimeMin = 10,
            cookTimeMin = 15,
            servings = 4,
            ingredientsText = "- 400 g Spaghetti\n- 200 g Guanciale\n- 4 Tuorli d'uovo\n- 100 g Pecorino Romano DOP\n- Pepe nero macinato fresco q.b.",
            instructionsText = "1. Tagliare il guanciale a listarelle e rosolarlo in padella senza olio finché non diventa croccante.\n\n2. Sbattere i tuorli con il pecorino grattugiato e abbondante pepe nero fino a ottenere una crema densa.\n\n3. Cuocere gli spaghetti in acqua bollente poco salata e scolarli al dente tenendo da parte un mestolo di acqua di cottura.\n\n4. Mantecare la pasta a fuoco spento con la crema di uova, il guanciale e l'acqua di cottura per creare un'emulsione cremosa.",
            notes = "Servire immediatamente con un'ulteriore spolverata di pecorino e pepe.",
            nutrition = Nutrition(calories = 680, proteinGrams = 28.0, carbsGrams = 72.0, fatGrams = 32.0),
            origin = "Lazio, Italia",
            isFavorite = true,
            isTried = true
        ),
        Recipe(
            id = "sample-tiramisu",
            categoryId = Category.DOLCI.id,
            title = "Tiramisù Tradizionale",
            description = "Il dolce al cucchiaio italiano più famoso al mondo, con savoiardi imbevuti di caffè e crema al mascarpone.",
            prepTimeMin = 25,
            cookTimeMin = 0,
            servings = 6,
            ingredientsText = "- 500 g Mascarpone\n- 300 g Savoiardi\n- 4 Uova freschissime\n- 100 g Zucchero\n- 300 ml Caffè espresso amaro\n- 30 g Cacao amaro in polvere",
            instructionsText = "1. Separare i tuorli dagli albumi. Montare i tuorli con lo zucchero fino a ottenere un composto spumoso.\n\n2. Aggiungere il mascarpone ai tuorli e amalgamare delicatamente.\n\n3. Montare gli albumi a neve ferma e incorporarli alla crema di mascarpone dal basso verso l'alto.\n\n4. Iniziare gli strati inzuppando velocemente i savoiardi nel caffè, coprire con crema e ripetere.\n\n5. Spolverizzare abbondantemente con cacao amaro prima di servire dopo almeno 3 ore in frigorifero.",
            notes = "Per una consistenza ottimale lasciar riposare in frigo tutta la notte.",
            nutrition = Nutrition(calories = 520, proteinGrams = 12.0, carbsGrams = 55.0, fatGrams = 28.0),
            origin = "Veneto / Friuli-Venezia Giulia",
            isFavorite = true,
            isTried = true
        ),
        Recipe(
            id = "sample-caprese",
            categoryId = Category.ANTIPASTI.id,
            title = "Insalata Caprese dell'Isola d'Capri",
            description = "Fresco antipasto estivo con pomodori maturi, mozzarella di bufala e basilico fresco.",
            prepTimeMin = 10,
            cookTimeMin = 0,
            servings = 2,
            ingredientsText = "- 3 Pomodori cuor di bue maturi\n- 250 g Mozzarella di Bufala Campana DOP\n- 1 mazzetto Basilico fresco\n- 4 cucchiai Olio extravergine d'oliva\n- Sale marino e origano q.b.",
            instructionsText = "1. Tagliare i pomodori e la mozzarella a fette di circa 1 cm di spessore.\n\n2. Disporre le fette alternate su un piatto da portata.\n\n3. Guarnire con foglie di basilico fresco, un pizzico di sale, origano e un filo d'olio EVO di qualità.",
            notes = "Servire a temperatura ambiente, non freddissima da frigorifero.",
            nutrition = Nutrition(calories = 340, proteinGrams = 18.0, carbsGrams = 8.0, fatGrams = 26.0),
            origin = "Campania, Italia",
            isFavorite = false,
            isTried = true
        )
    )
}
