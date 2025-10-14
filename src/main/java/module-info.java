import org.junit.jupiter.api.extension.ParameterResolver;

@SuppressWarnings("module")
module io.vertx.testing.junit5 {

  requires static io.vertx.docgen;

  requires static org.assertj.core; // Examples

  requires io.vertx.core;
  requires io.vertx.core.logging;
  requires org.junit.jupiter.params;

  exports io.vertx.junit5;

  uses io.vertx.junit5.VertxExtensionParameterProvider;

  provides ParameterResolver with io.vertx.junit5.VertxExtension;
  provides io.vertx.junit5.VertxExtensionParameterProvider with io.vertx.junit5.VertxParameterProvider, io.vertx.junit5.VertxTestContextParameterProvider;

}
