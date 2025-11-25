APP_NAME=clustereddata-warehouse

.PHONY: build test run docker-up docker-down clean

build:
	mvn -B clean package

test:
	mvn -B test

run:
	SPRING_DATASOURCE_URL=$${SPRING_DATASOURCE_URL:-jdbc:postgresql://localhost:5432/fx_deals} \
	SPRING_DATASOURCE_USERNAME=$${SPRING_DATASOURCE_USERNAME:-fx_user} \
	SPRING_DATASOURCE_PASSWORD=$${SPRING_DATASOURCE_PASSWORD:-fx_pass} \
	mvn spring-boot:run

docker-up:
	docker compose up --build

docker-down:
	docker compose down -v

clean:
	mvn -B clean
