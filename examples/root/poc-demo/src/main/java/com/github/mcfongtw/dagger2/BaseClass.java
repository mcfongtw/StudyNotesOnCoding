package com.github.mcfongtw.dagger2;

import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public abstract class BaseClass {

    @Inject
    Dao1Impl dao1;

}
