package com.sumika;

import com.sumika.support.MockJwtDecoderConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import({TestcontainersConfiguration.class, MockJwtDecoderConfiguration.class})
@SpringBootTest
class SumikaApplicationTests {

  @Test
  void contextLoads() {}
}
