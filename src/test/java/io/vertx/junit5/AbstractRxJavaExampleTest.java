package io.vertx.junit5;

import io.vertx.core.Vertx;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import rx.Single;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class AbstractRxJavaExampleTest extends AbstractRxJava1VertxTest {
  @Test
  public void testWithoutAbstractRxJavaTest(Vertx vertx, VertxTestContext context) throws InterruptedException {
    AuthenticatorStub auth = new AuthenticatorStub(new io.vertx.rxjava.core.Vertx(vertx));
    auth.getToken().subscribe(token -> {
      try {
        assertNotNull(token);
        assertTrue(token.getValue().length() > 0);
        assertNotNull(token.getExpiry());
        assertTrue(token.getExpiry().isAfter(Instant.now()));
        context.completeNow();
      } catch (Throwable t) {
        context.failNow(t);
      }
    }, context::failNow);
    assertTrue(context.awaitCompletion(60, TimeUnit.SECONDS));
  }

  @Test
  public void testWithAbstractRxJavaTest(VertxTestContext context) throws InterruptedException {
    testSingle(context,
      () -> new AuthenticatorStub(getRxVertx()).getToken(),
      token -> {
        assertNotNull(token);
        assertTrue(token.getValue().length() > 0);
        assertNotNull(token.getExpiry());
        assertTrue(token.getExpiry().isAfter(Instant.now()));
      });
  }

  public static class AuthenticatorStub {
    public AuthenticatorStub(io.vertx.rxjava.core.Vertx vertx) {
    }

    public Single<Token> getToken() {
      return Single.just(new Token().setValue("bogus").setExpiry(Instant.now().plusSeconds(5 * 60)));
    }
  }

  public static class Token {
    String value;
    Instant expiry;

    public String getValue() {
      return value;
    }

    public Token setValue(String value) {
      this.value = value;
      return this;
    }

    public Instant getExpiry() {
      return expiry;
    }

    public Token setExpiry(Instant expiry) {
      this.expiry = expiry;
      return this;
    }
  }
}
