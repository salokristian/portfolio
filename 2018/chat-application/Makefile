build-backend:
	cd backend && npm install
	cd backend/functions && npm install

build-frontend:
	cd frontend && ./gradlew build

build: build-backend build-frontend

deploy: build
	cd backend && npm run firebase deploy

default: build

all: deploy

.PHONY: all deploy default build build-frontend build-backend
