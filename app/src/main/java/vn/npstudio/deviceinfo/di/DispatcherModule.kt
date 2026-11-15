package vn.npstudio.deviceinfo.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import vn.npstudio.deviceinfo.data.PollingIntervals

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    fun providePollingIntervals(): PollingIntervals = PollingIntervals()
}
