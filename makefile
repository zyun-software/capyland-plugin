run:
	docker compose down
	docker compose run --rm gradle gradle build
	docker compose run -p 25565:25565 -p 8080:8080 --rm mc
