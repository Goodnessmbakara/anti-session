# FreshPress (Anti-Session)

FreshPress is a modern web application consisting of a React-based frontend and a Java Spring Boot backend.

## Project Structure

- **`freshpress-web/`**: The frontend application built with React and Vite.
- **`freshpress-api/`**: The backend application built with Java and Spring Boot.
- **`archives/presentation-MLUYO/`**: Interactive UI prototype and presentation materials regarding the core value proposition of FreshPress. Built with React, Vite, Framer Motion, and GSAP.

## Prerequisites

- Node.js (for the frontend and presentation app)
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

### 3. Running the Presentation Prototype (React)

There is also an interactive UI prototype in the archives, showcasing the core value offering:

```bash
cd archives/presentation-MLUYO
npm install
npm run dev
```

The presentation will be accessible at `http://localhost:5173`.

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
