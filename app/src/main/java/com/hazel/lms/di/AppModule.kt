package com.hazel.lms.di

import android.content.Context
import androidx.room.Room
import com.hazel.lms.data.room.dao.DepartmentDao
import com.hazel.lms.data.room.dao.StudentsDao
import com.hazel.lms.data.room.db.DatabaseMigration
import com.hazel.lms.data.room.db.LmsRoom
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun provideRoomDB(@ApplicationContext context: Context): LmsRoom {
        return Room.databaseBuilder(
            context,
            LmsRoom::class.java,
            "lms"
        )
            .addMigrations(DatabaseMigration.migration_1_2)
            .build()
    }

    @Provides
    fun provideDepartmentDao(roomDatabase: LmsRoom): DepartmentDao {
        return roomDatabase.departmentDao()
    }

    @Provides
    fun provideStudentDao(roomDatabase: LmsRoom): StudentsDao {
        return roomDatabase.studentDao()
    }
}