package edu.project.redstream.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    // RequestRepository  → @Singleton + @Inject constructor → Hilt auto-provides ✅
    // AdminRepository    → @Singleton + @Inject constructor → Hilt auto-provides ✅
    // AdminViewModel     → @HiltViewModel + @Inject constructor → Hilt auto-provides ✅
    // No manual @Provides needed for any of them.
}