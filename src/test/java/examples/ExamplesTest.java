package examples;

import org.junit.jupiter.api.Nested;

class ExamplesTest {
  @Nested
  class ExamplesNested extends Examples {
  }

  @Nested
  class LifecycleExampleTestNested extends LifecycleExampleTest {
  }
}
