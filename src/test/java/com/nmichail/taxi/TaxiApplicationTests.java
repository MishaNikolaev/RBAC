package com.nmichail.taxi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("H2 removed; enable when Postgres/Testcontainers test config is added")
class TaxiApplicationTests {

	@Test
	void contextLoads() {
	}

}
