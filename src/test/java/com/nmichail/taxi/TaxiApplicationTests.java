package com.nmichail.taxi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires PostgreSQL reachable per application properties (e.g. docker-compose)")
class TaxiApplicationTests {

	@Test
	void contextLoads() {
	}

}
