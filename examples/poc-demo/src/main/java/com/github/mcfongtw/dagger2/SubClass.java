package com.github.mcfongtw.dagger2;

import com.github.mcfongtw.dagger2.wiring.AppComponent;
import com.github.mcfongtw.dagger2.wiring.DaggerAppComponent;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class SubClass extends BaseClass {

    @Inject
    Dao2Impl dao2;

    public SubClass() {
        this(DaggerAppComponent.create());
        log.info("[END] SubClass()");
    }

    public SubClass(AppComponent component) {
        component.inject(this);
        log.info("[END] SubClass(AppComponent)");
    }
}
