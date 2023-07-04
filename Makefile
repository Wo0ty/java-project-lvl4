setup:
	gradle wrapper --gradle-version 7.4

build:
	make -C app build

install:
	make -C app install

clean:
	make -C app clean

start:
	make -C app start

start-dist:
	make -C app start-dist

generate-migrations:
	make -C app generate-migrations

lint:
	make -C app lint

test:
	make -C app test

report:
	make -C app report

check-updates:
	make -C app updates

.PHONY: build