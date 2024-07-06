import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.room.RoomDatabase
import logic.database.FeedEntityType
import logic.database.HistoryFavoriteDatabase
import logic.database.StringEntityDatabase
import logic.database.StringEntityType
import logic.datastore.AppSettings
import logic.datastore.dataStoreFileName
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.URI
import java.util.Locale

/**
 * Created by bggRGjQaUbCoE on 2024/6/27
 */
actual fun createDataStore(): DataStore<Preferences> {
    return AppSettings.getDataStore(
        producePath = {
            "dataStore/$dataStoreFileName"
        }
    )
}

actual fun openInBrowser(url: String) {
    try {
        val uri = URI.create(url)
        val osName by lazy(LazyThreadSafetyMode.NONE) {
            System.getProperty("os.name").lowercase(Locale.getDefault())
        }
        val desktop = Desktop.getDesktop()
        when {
            Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE) ->
                desktop.browse(uri)

            "mac" in osName -> Runtime.getRuntime().exec("open $uri")
            "nix" in osName || "nux" in osName -> Runtime.getRuntime().exec("xdg-open $uri")
            else -> throw RuntimeException("cannot open $uri")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

actual fun copyToClipboard(text: String) {
    val stringSelection = StringSelection(text)
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(stringSelection, null)
}

actual fun getSearchHistoryDataBase(type: StringEntityType): RoomDatabase.Builder<StringEntityDatabase> {
    val dir = File("database")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    val fileName = when (type) {
        StringEntityType.HISTORY -> "search_history"
        StringEntityType.USER -> "user_blacklist"
        StringEntityType.TOPIC -> "topic_blacklist"
        StringEntityType.EMOJI -> "recent_emoji"
    }
    val dbFile = File("database/$fileName.db")
    return Room.databaseBuilder<StringEntityDatabase>(
        name = dbFile.absolutePath,
    )
}

actual fun getHistoryFavoriteDatabase(type: FeedEntityType): RoomDatabase.Builder<HistoryFavoriteDatabase> {
    val dir = File("database")
    if (!dir.exists()) {
        dir.mkdirs()
    }
    val fileName = when (type) {
        FeedEntityType.HISTORY -> "browse_history"
        FeedEntityType.FAVORITE -> "feed_favorite"
    }
    val dbFile = File("database/$fileName.db")
    return Room.databaseBuilder<HistoryFavoriteDatabase>(
        name = dbFile.absolutePath,
    )
}