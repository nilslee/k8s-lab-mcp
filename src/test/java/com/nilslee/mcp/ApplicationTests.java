package com.nilslee.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

	/** Guards full context wiring, including Argo CD {@code ImportHttpServices} and group configurers. */
	@Test
	void contextLoads() {
	}

}
