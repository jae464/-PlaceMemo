package com.jae464.placememo.data.di

import com.jae464.placememo.data.api.RetrofitClient
import com.jae464.placememo.data.repository.address.AddressRepositoryImpl
import com.jae464.placememo.data.repository.login.LoginRepositoryImpl
import com.jae464.placememo.data.repository.login.remote.LoginRemoteDataSource
import com.jae464.placememo.data.repository.memo.MemoRepositoryImpl
import com.jae464.placememo.data.repository.memo.local.MemoLocalDataSource
import com.jae464.placememo.data.repository.memo.remote.MemoRemoteDataSource
import com.jae464.placememo.domain.repository.AddressRepository
import com.jae464.placememo.domain.repository.LoginRepository
import com.jae464.placememo.domain.repository.MemoRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(ViewModelComponent::class)
@Module
interface RepositoryModule {

    @Binds
    fun bindMemoRepository(
        memoRepositoryImpl: MemoRepositoryImpl
    ): MemoRepository

    @Binds
    fun bindAddressRepository(
        addressRepositoryImpl: AddressRepositoryImpl
    ): AddressRepository

    @Binds
    fun bindLoginRepository(
        loginRepositoryImpl: LoginRepositoryImpl
    ): LoginRepository

}