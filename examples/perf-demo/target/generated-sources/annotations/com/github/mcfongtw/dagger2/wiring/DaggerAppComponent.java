package com.github.mcfongtw.dagger2.wiring;

import com.github.mcfongtw.dagger2.BaseClass_MembersInjector;
import com.github.mcfongtw.dagger2.Dao1Impl;
import com.github.mcfongtw.dagger2.Dao2Impl;
import com.github.mcfongtw.dagger2.SubClass;
import com.github.mcfongtw.dagger2.SubClass_MembersInjector;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class DaggerAppComponent implements AppComponent {
  private Provider<Dao1Impl> provideDao1ImplProvider;

  private Provider<Dao2Impl> provideDao2ImplProvider;

  private DaggerAppComponent(Builder builder) {
    initialize(builder);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static AppComponent create() {
    return new Builder().build();
  }

  @SuppressWarnings("unchecked")
  private void initialize(final Builder builder) {
    this.provideDao1ImplProvider =
        DoubleCheck.provider(
            PersistenceModule_ProvideDao1ImplFactory.create(builder.persistenceModule));
    this.provideDao2ImplProvider =
        DoubleCheck.provider(
            PersistenceModule_ProvideDao2ImplFactory.create(builder.persistenceModule));
  }

  @Override
  public void inject(SubClass subClass) {
    injectSubClass(subClass);
  }

  @CanIgnoreReturnValue
  private SubClass injectSubClass(SubClass instance) {
    BaseClass_MembersInjector.injectDao1(instance, provideDao1ImplProvider.get());
    SubClass_MembersInjector.injectDao2(instance, provideDao2ImplProvider.get());
    return instance;
  }

  public static final class Builder {
    private PersistenceModule persistenceModule;

    private Builder() {}

    public AppComponent build() {
      if (persistenceModule == null) {
        this.persistenceModule = new PersistenceModule();
      }
      return new DaggerAppComponent(this);
    }

    public Builder persistenceModule(PersistenceModule persistenceModule) {
      this.persistenceModule = Preconditions.checkNotNull(persistenceModule);
      return this;
    }
  }
}
