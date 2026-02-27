# FreshPress (Anti-Session)

FreshPress is a modern web application consisting of a React-based frontend and a Java Spring Boot backend.

## Project Structure

- **`freshpress-web/`**: The frontend application built with React and Vite.
- **`freshpress-api/`**: The backend application built with Java and Spring Boot.
- **`presentation/`**: Presentation materials and UI prototype.
- **`archives/`**: Archived items and reference materials.

## Prerequisites

- Node.js (for the frontend)
- Java 17+ (for the backend)
- Maven (included via Maven Wrapper `mvnw` in the backend directory)

## Getting Started

### 1. Running the Backend (Spring Boot)

Navigate to the backend directory and run the application:

```bash
cd freshpress-api
./mvnw spring-boot:run
```

The API will start running on the configured port (default is usually `http://localhost:8080`).

### 2. Running the Frontend (React + Vite)

Navigate to the frontend directory, install dependencies, and start the development server:

```bash
cd freshpress-web
npm install
npm run dev
```

The frontend will be accessible at `http://localhost:5173`.

## Additional Commands

### Backend Tests
To run the backend tests:
```bash
cd freshpress-api
./mvnw test
```

### Frontend Build
To build the frontend for production:
```bash
cd freshpress-web
npm run build
```
