package vn.npstudio.deviceinfo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.npstudio.deviceinfo.data.source.BatteryDataSource
import vn.npstudio.deviceinfo.data.source.BatteryDataSourceImpl
import vn.npstudio.deviceinfo.data.source.BuildInfoDataSource
import vn.npstudio.deviceinfo.data.source.BuildInfoDataSourceImpl
import vn.npstudio.deviceinfo.data.source.CpuInfoDataSource
import vn.npstudio.deviceinfo.data.source.CpuInfoDataSourceImpl
import vn.npstudio.deviceinfo.data.source.DisplayDataSource
import vn.npstudio.deviceinfo.data.source.DisplayDataSourceImpl
import vn.npstudio.deviceinfo.data.source.MemoryDataSource
import vn.npstudio.deviceinfo.data.source.MemoryDataSourceImpl
import vn.npstudio.deviceinfo.data.source.SysFsReader
import vn.npstudio.deviceinfo.data.source.SysFsReaderImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindBuildInfoDataSource(impl: BuildInfoDataSourceImpl): BuildInfoDataSource

    @Binds
    abstract fun bindDisplayDataSource(impl: DisplayDataSourceImpl): DisplayDataSource

    @Binds
    abstract fun bindCpuInfoDataSource(impl: CpuInfoDataSourceImpl): CpuInfoDataSource

    @Binds
    abstract fun bindSysFsReader(impl: SysFsReaderImpl): SysFsReader

    @Binds
    abstract fun bindBatteryDataSource(impl: BatteryDataSourceImpl): BatteryDataSource

    @Binds
    abstract fun bindMemoryDataSource(impl: MemoryDataSourceImpl): MemoryDataSource
}
