package com.gatewayx;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@Testcontainers
class GatewayXApplicationTests {

	static Network network = Network.newNetwork();

	@Container
	static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
			.withExposedPorts(6379);

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
			.withDatabaseName("gatewayx")
			.withUsername("test_user")
			.withPassword("test_pass");

	@Container
	static GenericContainer<?> zookeeper = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-zookeeper:7.5.0"))
			.withNetwork(network)
			.withNetworkAliases("zookeeper")
			.withEnv("ZOOKEEPER_CLIENT_PORT", "2181");

	@Container
	static GenericContainer<?> kafka = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
			.withNetwork(network)
			.withExposedPorts(9092)
			.withEnv("KAFKA_BROKER_ID", "1")
			.withEnv("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181")
			.withEnv("KAFKA_LISTENERS", "PLAINTEXT://0.0.0.0:9092,BROKER://0.0.0.0:9093")
			.withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092,BROKER://kafka:9093")
			.withEnv("KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", "PLAINTEXT:PLAINTEXT,BROKER:PLAINTEXT")
			.withEnv("KAFKA_INTER_BROKER_LISTENER_NAME", "BROKER")
			.withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1")
			.dependsOn(zookeeper);

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		registry.add("spring.kafka.bootstrap-servers",
				() -> kafka.getHost() + ":" + kafka.getMappedPort(9092));
	}

	@Test
	void contextLoads() {
	}
}