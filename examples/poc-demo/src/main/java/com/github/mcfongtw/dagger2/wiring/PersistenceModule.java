package com.github.mcfongtw.dagger2.wiring;

import com.github.mcfongtw.dagger2.Dao1Impl;
import com.github.mcfongtw.dagger2.Dao2Impl;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class PersistenceModule {

    @Singleton
    @Provides
    public Dao1Impl provideDao1Impl() {
        return new Dao1Impl();
    }

    @Singleton
    @Provides
    public Dao2Impl provideDao2Impl() {
        return new Dao2Impl();
    }
}
