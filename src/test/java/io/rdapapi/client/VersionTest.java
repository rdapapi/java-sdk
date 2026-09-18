package io.rdapapi.client;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class VersionTest {

  @Test
  void sdkConstantMatchesTheBuildVersion() {
    assertThat(Version.SDK)
        .as("Version.SDK feeds the User-Agent; bump it with gradle.properties")
        .isEqualTo(System.getProperty("project.version"));
  }
}
