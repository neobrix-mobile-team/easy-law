package com.easylaw.app.common.util

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = "https://qecbifdhdjebqxpzucal.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InFlY2JpZmRoZGplYnF4cHp1Y2FsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzA4NjI5MTUsImV4cCI6MjA4NjQzODkxNX0.YrUAPYN33L3KTpwyk09PMxQ1-nSHVIH84ZaQ37KTJEc"
        ) {
            install(Postgrest)
            install(Auth)
        }
    }
}