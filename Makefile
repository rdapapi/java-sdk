.PHONY: test lint format clean

test:
	./gradlew test

lint:
	./gradlew spotlessCheck

format:
	./gradlew spotlessApply

clean:
	./gradlew clean
