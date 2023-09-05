package ed.maevski.diabeticdiary.di.modules

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import ed.maevski.diabeticdiary.data.DDRepository
import ed.maevski.diabeticdiary.data.dao.DDDao
import ed.maevski.diabeticdiary.data.db.DDDatabase
import javax.inject.Singleton

@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideDDDao(context: Context) =
        Room.databaseBuilder(
            context,
            DDDatabase::class.java,
            "art_db"
        ).build().ddDao()

    @Provides
    @Singleton
    fun provideRepository(ddDao: DDDao) = DDRepository(ddDao)
}