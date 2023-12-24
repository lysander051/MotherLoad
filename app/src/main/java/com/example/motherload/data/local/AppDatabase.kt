package com.example.motherload.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.motherload.data.ItemDescription

@Database(entities = [ItemDescription::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Permet de récupérer le DAO de la BDD
     *
     * @return le DAO
     */
    abstract fun itemDescriptionDao(): ItemDescriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Permet de récupérer la BDD
         *
         * @param context le context courant
         * @return la BDD
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "motherload_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
