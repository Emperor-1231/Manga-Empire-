package eu.kanade.tachiyomi.ui.setting

import android.os.Handler
import androidx.preference.PreferenceScreen
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.data.database.DatabaseHelper
import eu.kanade.tachiyomi.data.database.models.Category
import eu.kanade.tachiyomi.data.library.LibraryUpdateJob
import eu.kanade.tachiyomi.data.preference.DelayedLibrarySuggestionsJob
import eu.kanade.tachiyomi.ui.category.CategoryController
import eu.kanade.tachiyomi.ui.library.LibraryPresenter
import eu.kanade.tachiyomi.ui.library.display.TabbedLibraryDisplaySheet
import eu.kanade.tachiyomi.util.system.launchIO
import eu.kanade.tachiyomi.util.view.withFadeTransaction
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import eu.kanade.tachiyomi.data.preference.PreferenceKeys as Keys

class SettingsLibraryController : SettingsController() {

    private val db: DatabaseHelper = Injekt.get()

    override fun setupPreferenceScreen(screen: PreferenceScreen) = screen.apply {
        titleRes = R.string.library
        preferenceCategory {
            titleRes = R.string.general
            switchPreference {
                key = Keys.removeArticles
                titleRes = R.string.sort_by_ignoring_articles
                summaryRes = R.string.when_sorting_ignore_articles
                defaultValue = false
            }

            switchPreference {
                key = Keys.showLibrarySearchSuggestions
                titleRes = R.string.search_suggestions
                summaryRes = R.string.search_tips_show_periodically

                onChange {
                    it as Boolean
                    if (it) {
                        launchIO {
                            LibraryPresenter.setSearchSuggestion(preferences, db, Injekt.get())
                        }
                    } else {
                        DelayedLibrarySuggestionsJob.setupTask(false)
                        preferences.librarySearchSuggestion().set("")
                    }
                    true
                }
            }

            preference {
                key = "library_display_options"
                isPersistent = false
                titleRes = R.string.display_options
                summaryRes = R.string.can_be_found_in_library_filters

                onClick {
                    TabbedLibraryDisplaySheet(this@SettingsLibraryController).show()
                }
            }
        }

        val dbCategories = db.getCategories().executeAsBlocking()

        preferenceCategory {
            titleRes = R.string.categories
            preference {
                key = "edit_categories"
                isPersistent = false
                val catCount = db.getCategories().executeAsBlocking().size
                titleRes = if (catCount > 0) R.string.edit_categories else R.string.add_categories
                if (catCount > 0) summary = context.resources.getQuantityString(R.plurals.category_plural, catCount, catCount)
                onClick { router.pushController(CategoryController().withFadeTransaction()) }
            }
            intListPreference(activity) {
                key = Keys.defaultCategory
                titleRes = R.string.default_category

                val categories = listOf(Category.createDefault(context)) + dbCategories
                entries =
                    listOf(context.getString(R.string.always_ask)) + categories.map { it.name }.toTypedArray()
                entryValues = listOf(-1) + categories.mapNotNull { it.id }.toList()
                defaultValue = "-1"

                val selectedCategory = categories.find { it.id == preferences.defaultCategory() }
                summary =
                    selectedCategory?.name ?: context.getString(R.string.always_ask)
                onChange { newValue ->
                    summary = categories.find {
                        it.id == newValue as Int
                    }?.name ?: context.getString(R.string.always_ask)
                    true
                }
            }
        }

        preferenceCategory {
            titleRes = R.string.updates
            intListPreference(activity) {
                key = Keys.libraryUpdateInterval
                titleRes = R.string.library_update_frequency
                entriesRes = arrayOf(
                    R.string.manual,
                    R.string.every_3_hours,
                    R.string.every_6_hours,
                    R.string.every_12_hours,
                    R.string.daily,
                    R.string.every_2_days,
                    R.string.weekly
                )
                entryValues = listOf(0, 3, 6, 12, 24, 48, 168)
                defaultValue = 24

                onChange { newValue ->
                    // Always cancel the previous task, it seems that sometimes they are not updated.
                    LibraryUpdateJob.setupTask(0)

                    val interval = newValue as Int
                    if (interval > 0) {
                        LibraryUpdateJob.setupTask(interval)
                    }
                    true
                }
            }
            multiSelectListPreferenceMat(activity) {
                key = Keys.libraryUpdateRestriction
                titleRes = R.string.library_update_restriction
                entriesRes = arrayOf(R.string.wifi, R.string.charging)
                entryValues = listOf("wifi", "ac")
                summaryRes = R.string.library_update_restriction_summary

                preferences.libraryUpdateInterval().asObservable()
                    .subscribeUntilDestroy { isVisible = it > 0 }

                onChange {
                    // Post to event looper to allow the preference to be updated.
                    Handler().post { LibraryUpdateJob.setupTask() }
                    true
                }
            }
            switchPreference {
                key = Keys.updateOnlyNonCompleted
                titleRes = R.string.only_update_ongoing
                defaultValue = false
            }

            intListPreference(activity) {
                key = Keys.libraryUpdatePrioritization
                titleRes = R.string.library_update_order

                // The following array lines up with the list rankingScheme in:
                // ../../data/library/LibraryUpdateRanker.kt
                entriesRes = arrayOf(
                    R.string.alphabetically,
                    R.string.last_updated
                )
                entryRange = 0..1
                defaultValue = 0
                summaryRes = R.string.select_order_to_update
            }

            multiSelectListPreferenceMat(activity) {
                key = Keys.libraryUpdateCategories
                titleRes = R.string.categories_to_include_in_global_update

                val categories = listOf(Category.createDefault(context)) + dbCategories
                entries = categories.map { it.name }
                entryValues = categories.map { it.id.toString() }

                allSelectionRes = R.string.all
            }

            intListPreference(activity) {
                key = Keys.updateOnRefresh
                titleRes = R.string.categories_on_manual

                entriesRes = arrayOf(
                    R.string.first_category,
                    R.string.categories_in_global_update
                )
                entryRange = 0..1
                defaultValue = -1
            }

            switchPreference {
                key = Keys.refreshCoversToo
                titleRes = R.string.auto_refresh_covers
                summaryRes = R.string.auto_refresh_covers_summary
                defaultValue = true
            }

            switchPreference {
                key = Keys.showLibraryUpdateErrors
                titleRes = R.string.show_notification_error
                defaultValue = false
            }
        }
    }
}
