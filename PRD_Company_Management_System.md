# Company Management System (CMS) — Product Requirements Document (PRD)

| Field | Value |
|---|---|
| Version | 1.0 (Draft for review) |
| Date | 22 Oct 2026 |
| Author | _[Your name]_ |
| Companion documents | `TRD_Company_Management_System.md`, `SECURITY_Company_Management_System.md` |
| Backend | Java, Spring Boot, Spring Data JPA, PostgreSQL |
| Analytics | Python (Pandas, scikit-learn, statsmodels, TextBlob, Plotly, Matplotlib/Seaborn) |

---

## 1. Overview

### 1.1 Purpose
This document defines **what** the Company Management System (CMS) must do, **for whom**, and **how success is measured**. The TRD defines **how** it is built; the Security document defines how it is protected.

### 1.2 Problem statement
Small and mid-sized companies often keep employee, project, customer and sales data in scattered spreadsheets. This causes:
- no single trusted source of data, with duplicated and inconsistent records;
- no control over who can see or change what (for example, anyone can see salaries);
- no easy way to turn operational data into decisions (sales trends, regional performance, customer behaviour, customer sentiment).

### 1.3 Product vision
One web application where authorised staff **manage** core company data (departments, employees, projects, customers, products, sales orders, customer feedback) and **see interactive analytics** built on the same data, without leaving the system.

### 1.4 Solution summary
- A **Spring Boot REST backend** (layered architecture, JPA, JWT security, role-based access) is the system of record.
- A **Python analytics service** loads the data through an ETL pipeline into a small data warehouse, runs analysis (KPIs, forecasting, K-Means, sentiment, funnel) and returns chart data.
- A **simple web UI** served by the Spring Boot app contains both the management screens and an **Analytics section with interactive charts** (filters, hover, zoom, download).

---

## 2. Goals and non-goals

### 2.1 Goals
| ID | Goal |
|---|---|
| G1 | Provide secure CRUD management for departments, employees, projects, customers, products, sales orders and customer feedback. |
| G2 | Enforce role-based access (Admin, Manager, Employee) on every API and screen. |
| G3 | Provide interactive dashboards for sales, profit, regional performance, funnel, forecast, customer segments and sentiment **inside the CMS**. |
| G4 | Demonstrate a complete analytics workflow: sources → ETL → warehouse → analysis → visualization. |
| G5 | Satisfy all 12 Java FSD evaluation points and all 14 analytics objectives (see Section 10). |
| G6 | Keep the architecture simple enough to explain in a viva: three core components (Spring Boot, Python service, PostgreSQL). |

### 2.2 Non-goals (out of scope for this version)
Payroll, attendance and leave management, real payment processing, email/SMS notifications, file uploads, multi-company (multi-tenant) support, mobile apps, single sign-on, and self-service public sign-up.

---

## 3. Users and roles

| Role | Who | Main needs |
|---|---|---|
| **Admin** | Company/system administrator | Manage users and all master data; run ETL; see all analytics. |
| **Manager** | Department or sales manager | Manage projects, customers, products and orders; view analytics for the business. |
| **Employee** | Staff member / sales representative | View own profile and assigned projects; create sales orders as sales rep; view own performance. |

### 3.1 Permission summary
`C` create, `R` read, `U` update, `D` delete, `—` no access, `own` only own records.

| Module | Admin | Manager | Employee |
|---|---|---|---|
| Users and roles | CRUD | — | — |
| Departments | CRUD | R | R |
| Employees | CRUD | R (own department, no salary) | R own profile |
| Projects | CRUD | CRU, assign employees | R assigned only |
| Customers | CRUD | CRU | — |
| Products | CRUD | CRU | R |
| Sales orders | CRUD | CRU, change status | C, R own |
| Feedback | CRUD | CR | — |
| Analytics dashboards | All, plus run ETL | All except ETL | Own performance card only |

---

## 4. Scope and modules

| # | Module | Description | Priority | Phase |
|---|---|---|---|---|
| M1 | Authentication and Users | Login, logout, JWT, roles, change password, admin-created accounts | Must | 1 |
| M2 | Departments | Company departments | Must | 1 |
| M3 | Employees | Employee records, department, manager | Must | 1 |
| M4 | Projects | Projects with assigned employees (many-to-many) | Must | 1 |
| M5 | Customers | Customer records with region | Must | 1 |
| M6 | Products | Product catalogue with price and cost | Must | 1 |
| M7 | Sales Orders | Orders with items, status workflow, sales rep | Must | 1 |
| M8 | Feedback | Customer ratings and review text | Must | 1 |
| M9 | Analytics Core | ETL, warehouse, KPI, sales, regional, funnel dashboards | Must | 2 |
| M10 | Analytics Advanced | Forecast, K-Means segments, sentiment, unified report, Matplotlib/Seaborn plots | Must | 2 |
| M11 | Real-time and BI tools | Kafka live feed, open-source BI tool exploration and benchmark | Should | 3 |

---

## 5. Functional requirements

### 5.1 M1 Authentication and Users
| ID | Requirement | Priority |
|---|---|---|
| FR-AUTH-1 | A user can log in with email and password and receives a signed JWT. | Must |
| FR-AUTH-2 | A user can log out; the token is no longer accepted afterwards. | Must |
| FR-AUTH-3 | Every API endpoint (except login) requires authentication; access is decided by role. | Must |
| FR-AUTH-4 | Only an Admin can create, update, deactivate users and assign roles. There is no public sign-up. | Must |
| FR-AUTH-5 | A user can change their own password (old password required, strength rules applied). | Must |
| FR-AUTH-6 | Repeated failed logins are rate limited. | Must |

### 5.2 M2 Departments
| ID | Requirement | Priority |
|---|---|---|
| FR-DEP-1 | Admin can create, update and delete departments (name must be unique). | Must |
| FR-DEP-2 | A department with employees cannot be deleted; the system returns a clear conflict message. | Must |
| FR-DEP-3 | All roles can list departments. | Must |

### 5.3 M3 Employees
| ID | Requirement | Priority |
|---|---|---|
| FR-EMP-1 | Admin can create, view, update and delete employees (code, name, email, phone, job title, hire date, salary, status). | Must |
| FR-EMP-2 | Each employee belongs to one department and may have a manager (another employee). | Must |
| FR-EMP-3 | List supports pagination, sorting, search by name and filter by department and status. | Must |
| FR-EMP-4 | Salary and date of birth are visible to Admin only. | Must |
| FR-EMP-5 | Deleting an employee with sales history deactivates and anonymises the record instead of removing it. | Should |

### 5.4 M4 Projects
| ID | Requirement | Priority |
|---|---|---|
| FR-PRJ-1 | Admin and Manager can create and update projects (name, description, dates, status, department). | Must |
| FR-PRJ-2 | Admin and Manager can assign and remove employees on a project. | Must |
| FR-PRJ-3 | An employee can view only the projects they are assigned to. | Must |
| FR-PRJ-4 | Admin can delete a project. | Must |

### 5.5 M5 Customers and M6 Products
| ID | Requirement | Priority |
|---|---|---|
| FR-CUS-1 | Admin and Manager can create, view, update customers (name, email, phone, region); Admin can delete. | Must |
| FR-CUS-2 | Region is one of: North, South, East, West, Central. | Must |
| FR-PRD-1 | Admin and Manager can manage products (SKU unique, name, category, unit price, unit cost, active flag). | Must |
| FR-PRD-2 | Unit price and unit cost must be positive; cost cannot exceed price by more than a configured limit (warning only). | Should |

### 5.6 M7 Sales Orders
| ID | Requirement | Priority |
|---|---|---|
| FR-ORD-1 | A user can create an order for a customer with one or more items (product, quantity). The **server** calculates unit price snapshot, line totals and order total. | Must |
| FR-ORD-2 | Order status follows a workflow: `INQUIRY → QUOTED → CONFIRMED → PAID → DELIVERED`, with `CANCELLED` allowed before `DELIVERED`. Invalid jumps are rejected. | Must |
| FR-ORD-3 | Every status change is stored with timestamp (used for the sales funnel). | Must |
| FR-ORD-4 | Orders store the sales representative (an employee) and inherit the customer's region. | Must |
| FR-ORD-5 | Quantity must be 1 or more; discount must be between 0% and 30%. | Must |
| FR-ORD-6 | Employees see only orders where they are the sales rep. | Must |

### 5.7 M8 Feedback
| ID | Requirement | Priority |
|---|---|---|
| FR-FDB-1 | Admin and Manager can record customer feedback: customer, optional product/order, rating 1 to 5, review text. | Must |
| FR-FDB-2 | Feedback can be listed with filters (rating, product, date) and edited or deleted by Admin. | Must |

### 5.8 Platform
| ID | Requirement | Priority |
|---|---|---|
| FR-PLT-1 | All inputs are validated; errors return a consistent JSON structure with field-level messages. | Must |
| FR-PLT-2 | Interactive API documentation is available through Swagger UI (development and demo only). | Must |
| FR-PLT-3 | A seed command loads demo data (about 3 years of sales history, customers, products, reviews) so analytics are meaningful. | Must |

---

## 6. Analytics requirements (visible inside the CMS)

### 6.1 Analytics pages
All pages live under **Analytics** in the CMS navigation. Filters update charts without reloading the page.

| ID | Page | What the user sees | Filters | Method / tool | Roles | Phase |
|---|---|---|---|---|---|---|
| AN-1 | **Overview KPIs** | Revenue, profit, profit margin %, revenue growth rate (vs previous period), orders, average order value, average rating | Date range, region | SQL on warehouse + Pandas | Admin, Manager | 2 |
| AN-2 | **Sales and Profit** | Revenue and profit trend (line), category/product breakdown (bar) | Date range, region, category, granularity (month/quarter) | Plotly | Admin, Manager | 2 |
| AN-3 | **Regional Performance** | Region comparison (bar), region × month heatmap | Date range, category | Plotly | Admin, Manager | 2 |
| AN-4 | **Sales Funnel** | Funnel chart Inquiry → Quoted → Confirmed → Paid → Delivered with conversion % and drop-off | Date range, region, sales rep | Plotly funnel | Admin, Manager | 2 |
| AN-5 | **Forecast** | Historical sales with forecast line and confidence band; accuracy shown (MAPE) | Horizon (3–12 months), region | Time-series (Holt-Winters / ARIMA via statsmodels) | Admin, Manager | 2 |
| AN-6 | **Customer Segments** | K-Means clusters on Recency–Frequency–Monetary (scatter), segment summary table | Number of clusters (2–8) | scikit-learn K-Means | Admin, Manager | 2 |
| AN-7 | **Sentiment** | Positive/neutral/negative split, sentiment trend over time, top words, rating vs sentiment | Date range, product | TextBlob / NLTK | Admin, Manager | 2 |
| AN-8 | **Unified Report** | Sales per employee and department joined with customer feedback score, from multiple sources | Date range, department | Pandas merge of 3 sources | Admin, Manager | 2 |
| AN-9 | **Advanced Plots** | Correlation heatmap, distribution and box plots, pair plot (static images) | Metric selector | Matplotlib, Seaborn | Admin, Manager | 2 |
| AN-10 | **Live Sales Feed** | Orders appearing in near real time, running total for today | None | Apache Kafka | Admin, Manager | 3 |
| AN-11 | **Data Pipeline Status** | Last ETL run time, rows loaded per source, rows rejected, data-quality summary, "Run ETL now" button | None | ETL log | Admin | 2 |
| AN-12 | **My Performance** | Employee's own orders, revenue and conversion | Date range | SQL on warehouse | Employee | 2 |

### 6.2 Interaction and usability requirements
- Charts support hover tooltips, zoom/pan and "download as PNG" (Plotly toolbar).
- Every chart has a title, axis labels with units, a loading indicator, an empty-state message and a "last updated" time.
- Dashboards must work on a laptop screen and remain readable on a tablet.
- A failure in the analytics service shows a friendly "Analytics temporarily unavailable" message; the rest of the CMS keeps working.

### 6.3 Data sources for analytics
| Source | Content | Purpose |
|---|---|---|
| S1: CMS database (PostgreSQL, operational tables) | Customers, products, orders, employees, departments, feedback | Live company data |
| S2: `historical_sales.csv` | About 3 years of past sales | Enough history for forecasting and trends |
| S3: `reviews.csv` | Public sample customer reviews | Enough text for sentiment analysis |

### 6.4 Open-source BI tool exploration (supporting)
An open-source BI tool (Metabase or Apache Superset) is connected to the warehouse for exploration, the funnel/KPI comparison and the performance benchmark. It supports learning objectives and is **not** a dependency of the CMS.

---

## 7. User stories and acceptance criteria

| ID | Story | Acceptance criteria |
|---|---|---|
| US-1 | As an **Admin**, I want to create an employee so that the company records are complete. | Given I am logged in as Admin, when I submit valid employee data, then I receive `201 Created` and the employee appears in the list. Invalid email returns `400` with a field error. |
| US-2 | As an **Employee**, I must not see other people's salaries. | Given I am an Employee, when I request another employee's record, then I receive `403`. My own profile contains no salary of others. |
| US-3 | As a **Manager**, I want to assign employees to a project. | When I assign an employee, the project shows the employee; assigning the same employee twice does not create a duplicate. |
| US-4 | As an **Employee**, I want to create a sales order. | When I add items, the server returns totals; if I send a negative quantity I get `400`. Changing the price in the request has no effect. |
| US-5 | As a **Manager**, I want to move an order through its statuses. | `QUOTED → CONFIRMED` succeeds; `INQUIRY → DELIVERED` returns `409` with message; each change is stored with a time. |
| US-6 | As a **Manager**, I want to filter sales by region and date. | When I choose "West" and "Last 12 months", all charts on the page update within 3 seconds and show the applied filters. |
| US-7 | As a **Manager**, I want to see a sales forecast. | The Forecast page shows history plus the next N months with a confidence band and the model's MAPE. |
| US-8 | As a **Manager**, I want to see customer segments. | The segments page shows k clusters with names, sizes and average spend; changing k re-renders the chart. |
| US-9 | As a **Manager**, I want to see how customers feel. | Feedback is classified positive/neutral/negative; the chart totals match the number of reviews in the selected range. |
| US-10 | As an **Admin**, I want to refresh analytics data. | Clicking "Run ETL now" returns `202 Accepted`; the status page shows running → success/failed with row counts. |
| US-11 | As a **user**, I want my access protected. | After 5 failed logins in a minute from one address, the next attempt returns `429`. |

---

## 8. Non-functional requirements

| Area | Requirement |
|---|---|
| Performance | CRUD API: 95% of requests under 500 ms with 10,000 records. Dashboards: first render under 3 s on a warehouse with 100,000 fact rows (pre-aggregation and caching allowed). |
| Security | JWT authentication, role-based authorization on the server, BCrypt password hashing, validated input, no secrets in code, security headers. Details in the Security document. |
| Privacy | Analytics warehouse holds no emails, phone numbers or salaries. Reviews are analysed locally (no external AI service). |
| Reliability | Analytics failure must not break CMS CRUD. ETL is repeatable (running it twice gives the same result). |
| Maintainability | Layered code (Controller, Service, Repository, Entity/DTO), meaningful names, no business logic in controllers. |
| Testability | Service-layer unit tests with Mockito, controller and security tests with MockMvc, repository tests, Postman collection, pytest for analytics. Target: 70% line coverage on the service layer. |
| Portability | Runs from a terminal with JDK 21 and Maven wrapper, and opens and runs in Antigravity. Everything else starts with `docker compose up`. |
| Usability | Clear navigation, consistent forms, readable error messages. |
| Documentation | Swagger/OpenAPI for APIs, README with run steps, these three documents. |

---

## 9. Success metrics

| Metric | Target |
|---|---|
| Evaluation points demonstrable in the demo | 12 of 12 |
| Analytics objectives with a visible deliverable | 14 of 14 |
| Endpoints with automated tests | At least 80% of endpoints |
| Role-based access test cases passing | 100% |
| Dashboard load time (100k rows) | Under 3 s |
| Critical or high findings in final security checklist | 0 open |

---

## 10. Traceability

### 10.1 Java FSD evaluation points

| # | Evaluation point | Where it is satisfied |
|---|---|---|
| 1 | Project concept and requirements | Sections 1–6 of this PRD |
| 2 | Spring Boot implementation | TRD §4 (structure, annotations, profiles) |
| 3 | CRUD operations | FR-DEP, FR-EMP, FR-PRJ, FR-CUS, FR-PRD, FR-ORD, FR-FDB |
| 4 | Database integration | PostgreSQL + Spring Data JPA (TRD §5) |
| 5 | Entities and relationships | One-to-One, One-to-Many, Many-to-One, Many-to-Many (TRD §5.2) |
| 6 | REST APIs | TRD §6 (methods, status codes) |
| 7 | Layered architecture | Controller, Service, Repository, Entity/DTO (TRD §4.2) |
| 8 | Validation and exception handling | FR-PLT-1, TRD §6.4 |
| 9 | Authentication and authorization | M1, TRD §7 |
| 10 | Testing | Postman collection, JUnit, Mockito (TRD §11) |
| 11 | Code quality | Naming and structure rules (TRD §4.5) |
| 12 | Presentation and viva | Demo script in TRD §14 |

### 10.2 Analytics objectives

| # | Objective | Deliverable in the CMS project |
|---|---|---|
| 1 | Explore open-source BI tools, basic visualizations | Metabase/Superset connected to the warehouse; screenshots and short report (§6.4) |
| 2 | Clean and preprocess data (OpenRefine or Pandas) | Cleaning step of the ETL (missing values, duplicates, normalization) with a data-quality report on AN-11 |
| 3 | Interactive dashboard: sales, profit, regional, filters | AN-2, AN-3 |
| 4 | Small data warehouse and SQL queries | Star schema in `dw` schema; `queries.sql` with sales and customer queries |
| 5 | Simulate ETL | Python ETL pipeline (extract from S1–S3, transform, load) with run log; optional Pentaho/Talend screenshot of the same flow |
| 6 | Sentiment analysis | AN-7 |
| 7 | Time-series forecasting | AN-5 |
| 8 | KPIs (growth rate, profit margin) | AN-1 |
| 9 | Customer segmentation (K-Means) | AN-6 |
| 10 | Real-time BI with Kafka | AN-10 (Phase 3) |
| 11 | Matplotlib and Seaborn visualization | AN-9 |
| 12 | Integrate multiple data sources | S1 + S2 + S3 in ETL; AN-8 |
| 13 | Sales funnel | AN-4 (and a funnel built in the BI tool for comparison) |
| 14 | Compare BI tool performance on large data | Benchmark of Metabase, Superset and the in-app dashboard on 1M+ rows; results table in `docs/` |

---

## 11. Assumptions, constraints and dependencies

**Assumptions**
- A single company, single currency, single time zone.
- Demo data is generated by a seed script and public sample CSVs; no real personal data is used.
- Users have a modern desktop browser.

**Constraints**
- Backend must be Java with Spring Boot and MySQL or PostgreSQL through Spring Data JPA.
- Analytics must use Python libraries and tools.
- The project is built by a small team (or one student) within one semester.
- Architecture must remain simple (no microservice sprawl).

**Dependencies**
- JDK 21, Maven, PostgreSQL 16, Python 3.11+, Docker Desktop (for Postgres, Kafka, BI tool).
- Antigravity IDE (project must open and run there).

---

## 12. Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Scope too large for the time | Missed deliverables | Phase plan; Phase 3 items are "Should" |
| Too little data for forecasting/segments/sentiment | Weak analytics | Seed generator plus S2 and S3 datasets |
| Java ↔ Python integration issues | No charts in the UI | Simple JSON contract; Spring proxy with timeouts and a friendly fallback |
| Security gaps from rushed code | Data exposure | Security checklist run at the end of every phase |
| Kafka and BI tools add setup effort | Delays | Docker Compose; optional until Phase 3 |

---

## 13. Release plan

| Phase | Weeks | Content | Exit criteria |
|---|---|---|---|
| **1: Core CMS** | 1–3 | M1–M8, Swagger, validation, exception handling, tests, Postman collection | All CRUD and role tests pass; evaluation points 1–11 demonstrable |
| **2: Analytics** | 4–6 | Warehouse, ETL, M9, M10, in-app dashboards | AN-1 to AN-9, AN-11, AN-12 working with filters |
| **3: Real-time and BI** | 7–8 | Kafka live feed, BI tool exploration and benchmark, polish, security final pass | AN-10, objectives 1 and 14 delivered, security checklist signed off |

---

## 14. Open questions
1. Final database: PostgreSQL (recommended) or MySQL?
2. Should Managers be limited to their own department's orders, or see all? (Current draft: see all.)
3. Preferred open-source BI tool for objectives 1, 13, 14: Metabase (easier) or Superset (more Python-friendly)?
4. Is a Pentaho/Talend screenshot required for objective 5, or is the Python ETL enough?
