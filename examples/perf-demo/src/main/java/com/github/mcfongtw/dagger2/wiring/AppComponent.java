package com.github.mcfongtw.dagger2.wiring;

import com.github.mcfongtw.dagger2.BaseClass;
import com.github.mcfongtw.dagger2.SubClass;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {PersistenceModule.class})
public interface AppComponent {

    void inject(SubClass subClass);
}
