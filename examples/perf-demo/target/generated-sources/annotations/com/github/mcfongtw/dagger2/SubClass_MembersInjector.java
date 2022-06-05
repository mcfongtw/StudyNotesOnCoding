package com.github.mcfongtw.dagger2;

import dagger.MembersInjector;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class SubClass_MembersInjector implements MembersInjector<SubClass> {
  private final Provider<Dao1Impl> dao1Provider;

  private final Provider<Dao2Impl> dao2Provider;

  public SubClass_MembersInjector(
      Provider<Dao1Impl> dao1Provider, Provider<Dao2Impl> dao2Provider) {
    this.dao1Provider = dao1Provider;
    this.dao2Provider = dao2Provider;
  }

  public static MembersInjector<SubClass> create(
      Provider<Dao1Impl> dao1Provider, Provider<Dao2Impl> dao2Provider) {
    return new SubClass_MembersInjector(dao1Provider, dao2Provider);
  }

  @Override
  public void injectMembers(SubClass instance) {
    BaseClass_MembersInjector.injectDao1(instance, dao1Provider.get());
    injectDao2(instance, dao2Provider.get());
  }

  public static void injectDao2(SubClass instance, Dao2Impl dao2) {
    instance.dao2 = dao2;
  }
}
