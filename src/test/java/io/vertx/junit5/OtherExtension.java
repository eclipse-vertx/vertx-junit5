package io.vertx.junit5;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class OtherExtension implements ParameterResolver {

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    return type.equals(OtherVertx.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Class<?> type = parameterContext.getParameter().getType();
    ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.create(OtherExtension.class, extensionContext));
    if (OtherVertx.class.equals(type))
      return store.getOrComputeIfAbsent("otherVertx", k -> new OtherVertx(VertxExtension.retrieveVertx(parameterContext.getDeclaringExecutable(), extensionContext)));
    throw new IllegalStateException("Looks like the ParameterResolver needs a fix...");
  }
}
