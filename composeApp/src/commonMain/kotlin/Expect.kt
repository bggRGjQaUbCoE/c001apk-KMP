import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import logic.database.FeedEntityType
import logic.database.HistoryFavoriteDatabase
import logic.database.StringEntityDatabase
import logic.database.StringEntityType

/**
 * Created by bggRGjQaUbCoE on 2024/6/27
 */
expect fun createDataStore(): DataStore<Preferences>

expect fun openInBrowser(url: String)

expect fun copyToClipboard(text: String)

expect fun getSearchHistoryDataBase(type: StringEntityType): RoomDatabase.Builder<StringEntityDatabase>

expect fun getHistoryFavoriteDatabase(type: FeedEntityType): RoomDatabase.Builder<HistoryFavoriteDatabase>
