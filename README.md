# Key Point Hub

A Spring Boot 3 application for extracting, storing, and serving human pose keypoints from images. It integrates with an external MediaPipe-based service for keypoint extraction, persists structured keypoints in MySQL, stores original images in MongoDB GridFS, and provides REST endpoints to manage and retrieve data. It also includes scheduled jobs to export daily data and optionally email reports.

## Features
- Upload an image and extract pose keypoints via an external MediaPipe service
- Persist keypoints in MySQL (via JPA/Hibernate)
- Store original images in MongoDB GridFS and retrieve them by pose ID
- RESTful API to create, read, update, and delete poses
- Health/ping endpoint
- CORS configured for a React frontend (localhost:3000 by default)
- Scheduled daily export to a ZIP (JSON + images)
- Optional email of daily ZIP export
- Dockerfile for containerized deployment

## Tech Stack
- Java 21
- Spring Boot 3.2 (Web, Data JPA, Actuator, Mail, Quartz)
- MySQL (keypoints metadata) with Hibernate/JPA
- MongoDB + GridFS (image storage)
- Zip4j (zip creation)
- Micrometer/Zipkin (optional tracing)
- Maven build

## Architecture Overview
- `PoseController` orchestrates the upload flow:
  1. Forwards the uploaded image to an external MediaPipe service: `mediapipe.service.url`
  2. Parses the returned keypoints JSON
  3. Saves keypoints to MySQL (`Pose` + `KeyPoint` entities)
  4. Stores the original image in MongoDB GridFS and links the file ID to the pose record
- Read endpoints serve poses and binary images directly from GridFS
- Schedulers:
  - `DataZipScheduler` creates a dated ZIP export at 02:00 every day
  - `DailyZipScheduler` emails a static ZIP if present (you can adapt it to send the dated file)

## Project Layout
- `src/main/java/org/ganesh/keypointhub/controller`
  - `PoseController` — upload/extract, CRUD, image retrieval
  - `PingController` — `/api/ping` liveness
  - `GreetController` — demo root/web page
- `src/main/java/org/ganesh/keypointhub/entity` — `Pose`, `KeyPoint`
- `src/main/java/org/ganesh/keypointhub/repository` — `PoseRepository`
- `src/main/java/org/ganesh/keypointhub/configuration` — Mongo GridFS bean, CORS
- `src/main/java/org/ganesh/keypointhub/service` — `DataZipScheduler`, `DailyZipScheduler`, `EmailService`
- `src/main/resources/application.properties` — configuration
- `Dockerfile` — container packaging

## Requirements
- JDK 21+
- Maven 3.9+
- MySQL 8.x database accessible by the service
- MongoDB 6.x (or compatible) accessible by the service
- External MediaPipe extraction service listening at `mediapipe.service.url` (default points to `http://host.docker.internal:8000/extract-pose`)

## Configuration
All configuration defaults are in `src/main/resources/application.properties`. For production and local overrides, prefer environment variables or a separate `application-*.properties`.

Key properties (showing their purpose — provide your own secure values):

- Spring app
  - `server.port=8080`
  - `app.cors.allowed-origins=http://localhost:3000`
- MySQL
  - `spring.datasource.url=jdbc:mysql://host.docker.internal:3306/keypointhub`
  - `spring.datasource.username=...`
  - `spring.datasource.password=...`
  - `spring.jpa.hibernate.ddl-auto=update` (adjust for prod)
- MongoDB (GridFS)
  - `spring.data.mongodb.uri=mongodb://host.docker.internal:27017/keypointhub_images`
- MediaPipe service
  - `mediapipe.service.url=http://host.docker.internal:8000/extract-pose`
- Mail (optional)
  - `spring.mail.host=smtp.gmail.com`
  - `spring.mail.port=587`
  - `spring.mail.username=...`
  - `spring.mail.password=...`
  - `report.mail.to=recipient@example.com`
- Backup/export
  - `backup.directory=./backups`
  - `backup.cron=0 59 23 * * *` (not currently wired to a running job; daily export uses hard-coded 02:00)

Environment variables override properties automatically (Spring Boot convention), for example:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATA_MONGODB_URI`
- `MEDIAPIPE_SERVICE_URL`
- `APP_CORS_ALLOWED-ORIGINS`

Never commit real secrets to version control. Use environment variables or externalized config.

## Build
```
mvn clean package
```
This produces `target/key-point-hub.jar`.

## Run Locally
1. Ensure MySQL and MongoDB are running and reachable. Create the `keypointhub` database (MySQL) and `keypointhub_images` database (MongoDB) if needed.
2. Ensure the MediaPipe extraction service is running at the URL you configured.
3. Launch the service:
   ```
   java -jar target/key-point-hub.jar
   ```
4. Service listens on `http://localhost:8080` by default.

To supply config via env vars on Windows PowerShell:
```
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/keypointhub"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="<your_password>"
$env:SPRING_DATA_MONGODB_URI="mongodb://localhost:27017/keypointhub_images"
$env:MEDIAPIPE_SERVICE_URL="http://localhost:8000/extract-pose"
java -jar target/key-point-hub.jar
```

## Docker
Build the image (after packaging):
```
mvn clean package
docker build -t key-point-hub:latest .
```
Run the container, wiring databases and MediaPipe via host networking reference `host.docker.internal` (on Docker Desktop):
```
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/keypointhub" \
  -e SPRING_DATASOURCE_USERNAME="root" \
  -e SPRING_DATASOURCE_PASSWORD="<your_password>" \
  -e SPRING_DATA_MONGODB_URI="mongodb://host.docker.internal:27017/keypointhub_images" \
  -e MEDIAPIPE_SERVICE_URL="http://host.docker.internal:8000/extract-pose" \
  key-point-hub:latest
```

## API
Base URL: `http://localhost:8080`

- Ping
  - `GET /api/ping` → returns `"KeyPointHub is alive"`

- Poses
  - `POST /api/poses` (multipart/form-data)
    - Parts: `file` — image file
    - Action: forwards to MediaPipe service, stores keypoints + image
    - Response: JSON returned by MediaPipe service (includes `keypoints` array)
    - Example:
      ```
      curl -X POST http://localhost:8080/api/poses \
        -H "Content-Type: multipart/form-data" \
        -F "file=@/path/to/image.jpg"
      ```
  - `GET /api/poses` → list all poses (JSON)
  - `GET /api/poses/{id}` → single pose by ID (JSON)
  - `GET /api/poses/{id}/image` → original image as binary response
  - `PUT /api/poses/{id}` → update a pose's keypoints
    - Body: `Pose` JSON; ensure each `KeyPoint` references the parent pose on server side (controller sets backrefs)
  - `DELETE /api/poses/{id}` → deletes pose and its image from GridFS

### Entities
- `Pose`
  - `id: Long`
  - `createdAt: LocalDateTime`
  - `keypoints: List<KeyPoint>` (ONE-TO-MANY)
  - `imageFileId: String` (GridFS file ID)
- `KeyPoint`
  - `landmarkIndex: int`
  - `x: double`, `y: double`, `z: double`, `visibility: double`

## Schedulers & Exports
- `DataZipScheduler`
  - Runs daily at 02:00 (`@Scheduled("0 0 2 * * *")`)
  - Creates `exports/daily-export-YYYY-MM-DD.zip`
  - Contents: `poses-<tmp>.json` + images as `images/pose_<id>.jpg`
- `DailyZipScheduler`
  - Also scheduled at 02:00
  - If `exports/daily-data.zip` exists, emails it using `EmailService`

Adjust cron expressions and behavior as needed. Ensure mail configuration is valid before enabling email sending.

## CORS
Configured to allow `http://localhost:3000` and methods `GET, POST, PUT, DELETE`. Adjust `CorsConfig` or `app.cors.allowed-origins` if your frontend runs elsewhere.

## Health & Observability
- Actuator enabled with basic endpoints (`/actuator/health`, `/actuator/info`).
- Micrometer tracing libraries included; configure Zipkin/Brave reporters if needed.

## Troubleshooting
- MediaPipe service unreachable → upload returns 5xx. Verify `MEDIAPIPE_SERVICE_URL` and service availability.
- MySQL/Mongo connection issues → check connection strings and credentials; confirm containers/servers are reachable from the app/container.
- Large uploads blocked → adjust `spring.servlet.multipart.max-file-size` and `spring.servlet.multipart.max-request-size`.
- Image not found for a pose → ensure GridFS contains the file ID referenced by the `Pose`.
