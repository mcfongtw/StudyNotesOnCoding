package com.github.mcfongtw.dagger2.wiring;

import com.github.mcfongtw.dagger2.Dao1Impl;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class PersistenceModule_ProvideDao1ImplFactory implements Factory<Dao1Impl> {
  private final PersistenceModule module;

  public PersistenceModule_ProvideDao1ImplFactory(PersistenceModule module) {
    this.module = module;
  }

  @Override
  public Dao1Impl get() {
    return provideInstance(module);
  }

  public static Dao1Impl provideInstance(PersistenceModule module) {
    return proxyProvideDao1Impl(module);
  }

  public static PersistenceModule_ProvideDao1ImplFactory create(PersistenceModule module) {
    return new PersistenceModule_ProvideDao1ImplFactory(module);
  }

  public static Dao1Impl proxyProvideDao1Impl(PersistenceModule instance) {
    return Preconditions.checkNotNull(
        instance.provideDao1Impl(), "Cannot return null from a non-@Nullable @Provides method");
  }
}
