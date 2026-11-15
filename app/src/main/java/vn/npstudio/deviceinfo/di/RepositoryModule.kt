package vn.npstudio.deviceinfo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.npstudio.deviceinfo.data.repository.DeviceInfoRepositoryImpl
import vn.npstudio.deviceinfo.data.repository.DisplayRepositoryImpl
import vn.npstudio.deviceinfo.domain.repository.DeviceInfoRepository
import vn.npstudio.deviceinfo.domain.repository.DisplayRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindDeviceInfoRepository(impl: DeviceInfoRepositoryImpl): DeviceInfoRepository

    @Binds
    abstract fun bindDisplayRepository(impl: DisplayRepositoryImpl): DisplayRepository
}
