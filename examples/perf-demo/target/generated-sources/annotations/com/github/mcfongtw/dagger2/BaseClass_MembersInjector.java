package com.github.mcfongtw.dagger2;

import dagger.MembersInjector;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class BaseClass_MembersInjector implements MembersInjector<BaseClass> {
  private final Provider<Dao1Impl> dao1Provider;

  public BaseClass_MembersInjector(Provider<Dao1Impl> dao1Provider) {
    this.dao1Provider = dao1Provider;
  }

  public static MembersInjector<BaseClass> create(Provider<Dao1Impl> dao1Provider) {
    return new BaseClass_MembersInjector(dao1Provider);
  }

  @Override
  public void injectMembers(BaseClass instance) {
    injectDao1(instance, dao1Provider.get());
  }

  public static void injectDao1(BaseClass instance, Dao1Impl dao1) {
    instance.dao1 = dao1;
  }
}
