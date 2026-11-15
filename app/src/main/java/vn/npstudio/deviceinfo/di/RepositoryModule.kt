package vn.npstudio.deviceinfo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.npstudio.deviceinfo.data.repository.BatteryRepositoryImpl
import vn.npstudio.deviceinfo.data.repository.CpuRepositoryImpl
import vn.npstudio.deviceinfo.data.repository.DeviceInfoRepositoryImpl
import vn.npstudio.deviceinfo.data.repository.DisplayRepositoryImpl
import vn.npstudio.deviceinfo.data.repository.MemoryRepositoryImpl
import vn.npstudio.deviceinfo.domain.repository.BatteryRepository
import vn.npstudio.deviceinfo.domain.repository.CpuRepository
import vn.npstudio.deviceinfo.domain.repository.DeviceInfoRepository
import vn.npstudio.deviceinfo.domain.repository.DisplayRepository
import vn.npstudio.deviceinfo.domain.repository.MemoryRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindDeviceInfoRepository(impl: DeviceInfoRepositoryImpl): DeviceInfoRepository

    @Binds
    abstract fun bindDisplayRepository(impl: DisplayRepositoryImpl): DisplayRepository

    @Binds
    abstract fun bindCpuRepository(impl: CpuRepositoryImpl): CpuRepository

    @Binds
    abstract fun bindBatteryRepository(impl: BatteryRepositoryImpl): BatteryRepository

    @Binds
    abstract fun bindMemoryRepository(impl: MemoryRepositoryImpl): MemoryRepository
}
