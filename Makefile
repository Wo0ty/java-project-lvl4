clean:
	./gradlew clean

.PHONY: build
build:
	./gradlew clean build

install: clean
	./gradlew install

run-dist:
	./build/install/app/bin/app

lint:
	./gradlew checkstyleMain checkstyleTest
