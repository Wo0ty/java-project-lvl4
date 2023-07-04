setup:
	gradle wrapper --gradle-version 7.4

build: generate-migrations
	./gradlew clean build

install:
	./gradlew install

clean:
	./gradlew clean

start: build
	APP_ENV=development ./gradlew run

start-dist:
	APP_ENV=production ./build/install/app/bin/app

generate-migrations:
	./gradlew generateMigrations

lint:
	./gradlew checkstyleMain checkstyleTest

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

check-updates:
	./gradlew dependencyUpdates

.PHONY: build