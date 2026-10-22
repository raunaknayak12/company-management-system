# 🏢 Company Management System

> **Enterprise CMS** — Full-stack management platform with Operational CRUD, Analytics Intelligence, Star-Schema Data Warehouse, and Production-Ready Containerized Deployment.

[![CI/CD Pipeline](https://github.com/your-org/company-management-system/actions/workflows/ci.yml/badge.svg)](https://github.com/your-org/company-management-system/actions/workflows/ci.yml)

---

## 📋 Table of Contents

- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Quick Start (Local Development)](#quick-start-local-development)
- [Production Deployment (Docker)](#production-deployment-docker)
- [Configuration Reference](#configuration-reference)
- [API Documentation](#api-documentation)
- [Security](#security)
- [Testing](#testing)
- [Project Structure](#project-structure)

---

## Architecture

```
┌─────────────┐     ┌──────────────────────────────────────────────┐
│   Browser   │────▶│            Nginx Reverse Proxy (:80)         │
└─────────────┘     └──────────┬──────────────────────┬────────────┘
                               │                      │
                    ┌──────────▼──────────┐  ┌────────▼──────────┐
                    │  Spring Boot (:8080)│  │  FastAPI  (:8000)  │
                    │  (REST API + UI)    │  │  (Analytics & ETL) │
                    └──────────┬──────────┘  └────────┬──────────┘
                               │                      │
                    ┌──────────▼──────────────────────▼──────────┐
                    │        PostgreSQL 16 (:5432)                │
                    │  ┌──────────────┐  ┌────────────────────┐  │
                    │  │ public schema│  │ dw schema (OLAP)   │  │
                    │  │ (OLTP CRUD)  │  │ (Star Schema DW)   │  │
                    │  └──────────────┘  └────────────────────┘  │
                    └────────────────────────────────────────────┘
```

---

## Tech Stack

| Layer        | Technology                                                     |
|:-------------|:---------------------------------------------------------------|
| **Backend**  | Java 21, Spring Boot 3.3, Spring Security 6, JPA/Hibernate    |
| **Analytics**| Python 3.11, FastAPI, Pandas, Scikit-learn, Plotly, TextBlob   |
| **Database** | PostgreSQL 16 (prod) / H2 (dev), Star-Schema Data Warehouse   |
| **Proxy**    | Nginx 1.25 (reverse proxy, rate limiting, gzip, TLS-ready)    |
| **Auth**     | JWT (jjwt 0.12.5), BCrypt-12, Stateless session, RBAC         |
| **CI/CD**    | GitHub Actions (test → build → integration)                    |
| **Container**| Docker, Docker Compose v3.8, multi-stage builds                |

---

## Features

### Operational (CRUD)
- 👥 **User Management** — Registration, RBAC (Admin/Manager/Employee)
- 🏢 **Departments** — CRUD with employee count aggregation
- 👨‍💼 **Employees** — Full lifecycle, department assignment, performance
- 📊 **Projects** — Creation, team assignment, status tracking
- 📦 **Products** — Catalog with SKU, pricing, cost tracking
- 🛒 **Sales Orders** — Multi-line items, discount/tax, forward-only status
- 👤 **Customers** — CRM with region-based segmentation
- ⭐ **Feedback** — Star ratings + text reviews

### Analytics & Intelligence
- 📈 **Sales Trend Analysis** — Interactive Plotly time-series
- 🗺️ **Regional Comparison** — Geographic revenue distribution
- 🔮 **Holt-Winters Forecasting** — 6–24 month sales predictions
- 🎯 **K-Means Customer Segmentation** — RFM-based clustering
- 💬 **Sentiment Analysis** — TextBlob NLP on customer reviews
- 📊 **KPI Dashboard** — Revenue, orders, AOV, profit margin
- 🔄 **ETL Pipeline** — Automated star-schema data warehouse hydration

### Security & Production
- 🔐 **JWT Authentication** with secret length validation (≥32 bytes)
- 🛡️ **Rate Limiting** — 5 failed login attempts/minute per IP
- 🔒 **CSP Headers** — Content Security Policy, X-Frame-Options
- 🧹 **PII Scrubbing** — Email/phone redaction in ETL pipeline
- 🚫 **IDOR Protection** — Deactivated user token rejection
- 📝 **Correlation IDs** — Masked error responses with tracking

---

## Quick Start (Local Development)

### Prerequisites
- **Java 21** (JDK)
- **Maven 3.9+**
- **Python 3.11+**
- **Node.js** (not required — static HTML frontend)

### 1. Clone & Setup

```bash
git clone https://github.com/your-org/company-management-system.git
cd company-management-system
```

### 2. Start Backend (Spring Boot + H2)

```bash
cd backend
mvn spring-boot:run
```
Backend starts at **http://localhost:8080** with H2 in-memory database.

### 3. Start Analytics (FastAPI)

```bash
cd analytics
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```
Analytics API at **http://localhost:8000**, health check at `/health`.

### 4. Access Application

| URL                                  | Description           |
|:-------------------------------------|:----------------------|
| http://localhost:8080                 | Main Application      |
| http://localhost:8080/login.html      | Login Page            |
| http://localhost:8080/swagger-ui.html | API Documentation     |
| http://localhost:8080/h2-console      | H2 Database Console   |
| http://localhost:8080/actuator/health | Health Check          |
| http://localhost:8000/health          | Analytics Health      |
| http://localhost:8000/docs            | FastAPI Swagger       |

### Default Credentials (Dev Only)

| Role     | Email              | Password   |
|:---------|:-------------------|:-----------|
| Admin    | admin@company.com  | admin123   |
| Manager  | manager@company.com| manager123 |
| Employee | emp@company.com    | emp123     |

---

## Production Deployment (Docker)

### Prerequisites
- **Docker** 24+
- **Docker Compose** v2+

### 1. Configure Environment

```bash
cp .env.example .env
```

**Edit `.env` with production values:**

```env
POSTGRES_SUPERUSER_PASSWORD=<strong-password>
DB_PASSWORD=<strong-password>
DW_PASSWORD=<strong-password>
JWT_SECRET=$(openssl rand -hex 32)
INTERNAL_API_KEY=$(openssl rand -hex 16)
```

### 2. Build & Start

```bash
docker compose up -d --build
```

### 3. Verify Health

```bash
# Nginx proxy
curl http://localhost/nginx-health

# Backend actuator
curl http://localhost:8080/actuator/health

# View logs
docker compose logs -f
```

### 4. Stop & Cleanup

```bash
# Stop services (data persisted)
docker compose down

# Stop & remove all data
docker compose down -v
```

---

## Configuration Reference

### Environment Variables

| Variable                        | Default                   | Description                       |
|:--------------------------------|:--------------------------|:----------------------------------|
| `POSTGRES_SUPERUSER`            | `postgres`                | PostgreSQL superuser              |
| `POSTGRES_SUPERUSER_PASSWORD`   | `postgrespassword`        | Superuser password                |
| `DB_HOST`                       | `postgres`                | Database host                     |
| `DB_PORT`                       | `5432`                    | Database port                     |
| `DB_NAME`                       | `cms_db`                  | Database name                     |
| `DB_USER`                       | `cms_app`                 | Application DB user               |
| `DB_PASSWORD`                   | `cmspassword`             | Application DB password           |
| `DB_SSLMODE`                    | `disable`                 | PostgreSQL SSL mode               |
| `DW_USER`                       | `dw_user`                 | Data warehouse DB user            |
| `DW_PASSWORD`                   | `dwpassword`              | Data warehouse DB password        |
| `JWT_SECRET`                    | (hex string)              | JWT signing secret (≥32 bytes)    |
| `JWT_EXPIRATION_MS`             | `86400000`                | JWT token TTL (24h)               |
| `INTERNAL_API_KEY`              | `cms-internal-...`        | Backend ↔ Analytics bridge key    |
| `APP_PORT`                      | `80`                      | Nginx external port               |

### Spring Profiles

| Profile  | Database    | Swagger | H2 Console | DDL        |
|:---------|:------------|:--------|:-----------|:-----------|
| `dev`    | H2 (file)   | ✅       | ✅          | `update`   |
| `prod`   | PostgreSQL  | ❌       | ❌          | `validate` |

---

## API Documentation

### REST API Endpoints

| Method   | Endpoint                    | Roles                    | Description                |
|:---------|:----------------------------|:-------------------------|:---------------------------|
| `POST`   | `/api/auth/login`           | Public                   | Authenticate & get JWT     |
| `GET`    | `/api/departments`          | Authenticated            | List departments           |
| `POST`   | `/api/departments`          | Admin                    | Create department          |
| `GET`    | `/api/employees/me`         | Authenticated            | Current employee profile   |
| `GET`    | `/api/employees`            | Admin, Manager           | List employees             |
| `POST`   | `/api/employees`            | Admin                    | Create employee            |
| `GET`    | `/api/projects`             | Authenticated            | List projects              |
| `POST`   | `/api/projects`             | Admin, Manager           | Create project             |
| `GET`    | `/api/products`             | Authenticated            | List products              |
| `POST`   | `/api/products`             | Admin, Manager           | Create product             |
| `GET`    | `/api/orders`               | Authenticated            | List orders                |
| `POST`   | `/api/orders`               | Authenticated            | Create order               |
| `PATCH`  | `/api/orders/{id}/status`   | Admin, Manager           | Update order status        |
| `GET`    | `/api/customers`            | Admin, Manager           | List customers             |
| `GET`    | `/api/feedback`             | Admin, Manager           | List feedback              |
| `GET`    | `/api/analytics/**`         | Admin, Manager           | Analytics dashboard data   |
| `POST`   | `/api/analytics/etl/run`    | Admin                    | Trigger ETL pipeline       |
| `GET`    | `/actuator/health`          | Public                   | Application health check   |

### Analytics Endpoints (Internal)

| Endpoint                  | Description                            |
|:--------------------------|:---------------------------------------|
| `/health`                 | Service health check                   |
| `/etl/run`                | Trigger ETL pipeline                   |
| `/analytics/kpis`         | Revenue, orders, AOV metrics           |
| `/analytics/sales/trend`  | Time-series sales data                 |
| `/analytics/regional`     | Geographic comparison                  |
| `/analytics/funnel`       | Sales funnel metrics                   |
| `/analytics/forecast`     | Holt-Winters forecast                  |
| `/analytics/segments`     | K-Means customer segments              |
| `/analytics/sentiment`    | TextBlob sentiment analysis            |

---

## Security

See [docs/SECURITY_Company_Management_System.md](docs/SECURITY_Company_Management_System.md) for the full security audit report.

**Key Security Features:**
- JWT secret validation (≥32 bytes) at startup
- BCrypt-12 password hashing
- Rate limiting on login (5 attempts/min per IP)
- Content Security Policy (CSP) headers
- Constant-time API key comparison (`hmac.compare_digest`)
- PII scrubbing in ETL pipeline
- Masked internal errors with correlation IDs
- Forward-only order state transitions
- Deactivated user token rejection
- Last admin deactivation prevention

---

## Testing

### Backend (Java)

```bash
cd backend
mvn clean verify
```

### Analytics (Python)

```bash
cd analytics
pip install pytest
pytest -v
```

### Integration (Docker)

```bash
docker compose up -d --build --wait
curl http://localhost:8080/actuator/health
docker compose down -v
```

---

## Project Structure

```
company-management-system/
├── .github/workflows/
│   └── ci.yml                    # GitHub Actions CI/CD pipeline
├── analytics/                    # Python FastAPI microservice
│   ├── app/
│   │   ├── api/endpoints.py      # REST API routes
│   │   ├── charts/               # Plotly & Seaborn builders
│   │   ├── etl/pipeline.py       # ETL pipeline with PII scrubbing
│   │   ├── models/               # Forecast, segmentation, sentiment
│   │   ├── config.py             # Environment config
│   │   ├── db.py                 # Database engine
│   │   └── main.py               # FastAPI application
│   ├── data/                     # CSV data sources
│   ├── tests/                    # Pytest test suite
│   ├── Dockerfile                # Production container
│   └── requirements.txt          # Python dependencies
├── backend/                      # Spring Boot application
│   ├── src/main/java/com/company/cms/
│   │   ├── config/               # Security, CORS, OpenAPI, DataInit
│   │   ├── controller/           # REST controllers
│   │   ├── dto/                  # Data transfer objects
│   │   ├── entity/               # JPA entities
│   │   ├── exception/            # Global exception handler
│   │   ├── repository/           # Spring Data repositories
│   │   ├── security/             # JWT, auth filters, rate limiting
│   │   └── service/              # Business logic services
│   ├── src/main/resources/
│   │   ├── static/               # Frontend HTML/CSS/JS
│   │   ├── application.yml       # Dev profile config
│   │   └── application-prod.yml  # Production profile config
│   ├── Dockerfile                # Multi-stage production container
│   └── pom.xml                   # Maven dependencies
├── db/                           # Database initialization scripts
│   ├── 01_roles.sql              # PostgreSQL roles & permissions
│   └── 02_dw_schema.sql          # Star-schema data warehouse DDL
├── docs/
│   └── SECURITY_*.md             # Security audit report
├── nginx/
│   └── nginx.conf                # Reverse proxy configuration
├── postman/                      # API testing collection
├── .env.example                  # Environment variable template
├── .gitignore                    # Git ignore rules
├── docker-compose.yml            # Production orchestration
├── PRD_*.md                      # Product Requirements Document
└── TRD_*.md                      # Technical Requirements Document
```

---

## License

Proprietary – All rights reserved.
