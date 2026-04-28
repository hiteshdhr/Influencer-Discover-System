# InfluenceHub AI (Micro-Influencer Discovery System)

InfluenceHub AI is a premium SaaS application designed for brands to discover and analyze micro-influencers across platforms like YouTube and Instagram.

> **Note:** The dashboard and web application are **still in development**. Some features may be mocked or incomplete.

## Features
- **Live Discovery Pipeline**: Connects to the YouTube Data API to fetch real, active micro-influencers based on your keywords.
- **Content Intelligence**: Analyzes engagement rates and niches to generate a "Fit Score" for your brand context.
- **Minimalist SaaS UI**: A sleek, dark-themed interface inspired by Linear and Vercel, built with React, Tailwind CSS, and Framer Motion.
- **Java Spring Boot Backend**: Robust and scalable backend REST API running on port 8080.

## Screenshots

*(The application is still under development. Below is a preview of the discovery dashboard)*

![Discovery Dashboard](assets/dashboard.png)
*(Please add the actual `dashboard.png` to the `assets/` folder)*

## Tech Stack
- **Frontend**: React 19, Vite, Tailwind CSS, Framer Motion, React Router
- **Backend**: Java Spring Boot (Port 8080)
- **APIs**: YouTube Data API v3

## How to Run

1. **Backend**:
   Navigate to `backend_java/` and configure your API keys in `src/main/resources/application.properties`.
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Frontend**:
   Navigate to `frontend/` and start the Vite dev server.
   ```bash
   npm install
   npm run dev
   ```

## License
MIT
