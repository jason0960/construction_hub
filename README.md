# ConstructionHub
App to help smaller and individual contractors with permit, job, and crew management.

## Project Structure

```
construction_hub/
├── backend/          # Spring Boot 3 API (Java 17, Gradle)
├── frontend/         # React + Vite + TypeScript (PWA)
└── render.yaml       # Render.com deployment blueprint
```

## Tech Stack

- **Backend**: Spring Boot 3.x, Spring Security (JWT), Spring Data JPA, Flyway
- **Frontend**: React, Vite, TypeScript, TanStack Query, Tailwind CSS
- **Database**: PostgreSQL (Render managed)
- **File Storage**: Cloudflare R2 (S3-compatible)
- **Hosting**: Render.com

## Local Development

### Prerequisites
- Java 17+
- Node.js 20+
- PostgreSQL 17

### Backend
```bash
cd backend
./gradlew bootRun
```
API runs at http://localhost:8080

### Frontend
```bash
cd frontend
npm install
npm run dev
```
App runs at http://localhost:5173

## Deployment
Push to `main` triggers auto-deploy on Render via `render.yaml` blueprint.
