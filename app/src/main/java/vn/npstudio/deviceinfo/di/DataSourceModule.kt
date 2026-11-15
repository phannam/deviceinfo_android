package vn.npstudio.deviceinfo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.npstudio.deviceinfo.data.source.BuildInfoDataSource
import vn.npstudio.deviceinfo.data.source.BuildInfoDataSourceImpl
import vn.npstudio.deviceinfo.data.source.DisplayDataSource
import vn.npstudio.deviceinfo.data.source.DisplayDataSourceImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindBuildInfoDataSource(impl: BuildInfoDataSourceImpl): BuildInfoDataSource

    @Binds
    abstract fun bindDisplayDataSource(impl: DisplayDataSourceImpl): DisplayDataSource
}
