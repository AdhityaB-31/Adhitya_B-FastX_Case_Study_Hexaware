# FastX - Multi-Service Transit & Booking Platform

FastX is a high-performance, full-stack transit and booking platform consisting of a Spring Boot Java backend and a React (Vite) frontend.

This repository is structured as a monorepo containing both the backend and frontend services under the root directory. To secure sensitive data, secret keys, diagrams, and deployment files, specific gitignore rules are implemented, with example templates provided to ease development setup.

---

## 📁 Repository Structure

```text
FastX/ (Root Repo Folder)
├── FastX_Backend/             # Spring Boot Backend Service
│   ├── src/
│   │   ├── main/resources/
│   │   │   ├── application.properties          # Ignored (Contains secrets)
│   │   │   └── application.properties.example  # Checked-in template
│   │   └── ...
│   ├── Dockerfile                               # Ignored (Deployment config)
│   └── ...
├── FastX_Frontend/            # React + Vite Frontend Service
│   ├── src/
│   ├── .env                                     # Ignored (Contains secrets/client IDs)
│   ├── .env.example                             # Checked-in template
│   ├── Dockerfile                               # Ignored (Deployment config)
│   └── ...
├── .gitignore                 # Root Git Ignore config
├── docker-compose.yml         # Ignored (Contains container secrets)
├── docker-compose.yml.example # Checked-in template
├── Adhitya.pem                # Ignored (Private SSL key / Auth keys)
├── Service.puml               # Ignored (UML design files)
├── UML.puml                   # Ignored (UML design files)
└── seed_data.sql              # Ignored (Database seed scripts)
```

---

## 🔒 Security & Data Protection (Ignored Files)

To secure our environments, database credentials, API integration secrets, and private structural diagrams, we restrict specific files from being pushed to the public Git repository.

### 🚫 Ignored File Groups

| Category | File Pattern / Path | Reason for Securing | Template Provided |
| :--- | :--- | :--- | :--- |
| **Secrets & Keys** | `**/application*.properties` | Database credentials, Razorpay secret key, Gmail SMTP app password | [application.properties.example](file:///media/adhitya/My%20Space/Projects/Project-Sources/FastX/FastX_Backend/src/main/resources/application.properties.example) |
| **Frontend Env** | `**/.env` | Client-side endpoints and Google OAuth Client IDs | [env.example](file:///media/adhitya/My%20Space/Projects/Project-Sources/FastX/FastX_Frontend/.env.example) |
| **Docker Compose** | `docker-compose.yml` | Central orchestration settings and production/environment secrets | [docker-compose.yml.example](file:///media/adhitya/My%20Space/Projects/Project-Sources/FastX/docker-compose.yml.example) |
| **Dockerfiles** | `**/Dockerfile`, `**/dockerfile` | Deployment configurations and environments | — |
| **Keys & Certs** | `**/*.pem` | SSL/SSH private keys and connection credentials | — |
| **Database Seeds** | `**/*.sql` | SQL dumps, seed data containing production-like entries | — |
| **UML Diagrams** | `**/*.puml` | System internal specifications and design diagrams | — |

> [!WARNING]
> Never force-add (`git add -f`) any of the ignored files. Doing so can compromise API keys, database credentials, and SMTP mail passwords.

---

## 🚀 Getting Started

To run the application locally, follow these steps to configure your environment:

### 1. Set Up Environment Secrets
Duplicate the provided example templates and fill in your local or production credentials:

- **Backend Configuration**:
  ```bash
  cp FastX_Backend/src/main/resources/application.properties.example FastX_Backend/src/main/resources/application.properties
  # Open and configure database password, Gmail SMTP keys, and Razorpay secrets.
  ```
- **Frontend Configuration**:
  ```bash
  cp FastX_Frontend/.env.example FastX_Frontend/.env
  # Set VITE_GOOGLE_CLIENT_ID and VITE_API_URL.
  ```
- **Docker Compose Configuration**:
  ```bash
  cp docker-compose.yml.example docker-compose.yml
  # Set your MYSQL_ROOT_PASSWORD and other secrets.
  ```

### 2. Run the Application
You can run the application services locally or using Docker.

#### Run with Docker Compose (Recommended)
Make sure you have Docker installed and running, then execute:
```bash
docker compose up --build
```
This will build and start:
- MySQL Container (port `3306`)
- Backend Spring Boot App (port `8080`)
- Frontend React App (port `80` or mapped port)

#### Run Locally
* **Backend**:
  ```bash
  cd FastX_Backend
  ./mvnw spring-boot:run
  ```
* **Frontend**:
  ```bash
  cd FastX_Frontend
  npm install
  npm run dev
  ```
