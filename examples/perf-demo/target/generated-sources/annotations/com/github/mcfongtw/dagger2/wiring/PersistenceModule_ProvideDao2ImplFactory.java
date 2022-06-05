package com.github.mcfongtw.dagger2.wiring;

import com.github.mcfongtw.dagger2.Dao2Impl;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class PersistenceModule_ProvideDao2ImplFactory implements Factory<Dao2Impl> {
  private final PersistenceModule module;

  public PersistenceModule_ProvideDao2ImplFactory(PersistenceModule module) {
    this.module = module;
  }

  @Override
  public Dao2Impl get() {
    return provideInstance(module);
  }

  public static Dao2Impl provideInstance(PersistenceModule module) {
    return proxyProvideDao2Impl(module);
  }

  public static PersistenceModule_ProvideDao2ImplFactory create(PersistenceModule module) {
    return new PersistenceModule_ProvideDao2ImplFactory(module);
  }

  public static Dao2Impl proxyProvideDao2Impl(PersistenceModule instance) {
    return Preconditions.checkNotNull(
        instance.provideDao2Impl(), "Cannot return null from a non-@Nullable @Provides method");
  }
}
