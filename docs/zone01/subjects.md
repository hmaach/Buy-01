
================================================================
# buy-01
Source: https://learn.zone01oujda.ma//api/content/root/01-edu_module/content/buy-01/README.md
================================================================

## Buy 01

### Overview

In this project, you will build an **end-to-end e-commerce platform** using **Spring Boot microservices** on the backend and **Angular** on the frontend.  
Users can register as **clients** or **sellers**; sellers manage products and related media (images).

You will practice **service decomposition**, **inter-service communication** and a clean Angular UI that consumes these services.

### Role Play

You are a **full-stack engineer** designing a small but realistic marketplace composed of independently deployable services.  
Your mission: deliver a **secure**, **observable**, and **scalable** platform where **clients** browse products and **sellers** manage their catalog and media.

### Learning Objectives

- Design and implement **Spring Boot microservices** (User, Product, Media)
- Apply **asynchronous communication** (e.g., Kafka) where appropriate
- Implement **JWT or OAuth2** with **Spring Security** for role-based access
- Enforce **secure file uploads** with validation and size limits
- Model and persist data in **MongoDB** (or polyglot where justified)
- Build an **Angular** SPA with routing, guards, interceptors, and forms

### Instructions

#### 1) Microservices Setup

- Create separate services:
  - **User Service** (auth, profiles, roles: CLIENT, SELLER)
  - **Product Service** (CRUD for products, image references)
  - **Media Service** (image upload/download, validation, 2 MB limit)
- Configure **Kafka** (optional but recommended) for events such as `PRODUCT_CREATED`, `IMAGE_UPLOADED` (useful for audit, cache invalidation, thumbnails).

#### 2) Enhanced Database Design

![DataBase Design](./Database-Design.png)

#### 3) API Development Enhancement

- **User Service**
  - **Auth**: `POST /auth/register` (role: CLIENT | SELLER), `POST /auth/login` → JWT/OAuth2 token
  - **Profile**: `GET /me`, `PUT /me` (seller may upload/update avatar → delegated to Media Service)
- **Product Service**
  - **Public**: `GET /products` (list), `GET /products/{id}`
  - **Seller-only**: `POST /products`, `PUT /products/{id}`, `DELETE /products/{id}` (enforce ownership)
  - Associate `imageUrls[]`; uploading images is performed via Media Service then linked here.
- **Media Service**
  - `POST /media/images` (seller-only): validate **MIME type (image/\*)** and **≤ 2 MB**
  - `GET /media/images/{id}`: serve image (with proper caching headers)
  - Optional: `DELETE /media/images/{id}` (seller must own media)

> All services expose **/actuator/health**; gateway routes external traffic and applies cross-cutting filters (CORS, auth propagation, rate limiting if added).

#### 4) Front-End Development with Angular

- **Auth Pages**: Sign-in & Sign-up (role selection). Sellers can upload/update avatar.
- **Seller Dashboard**:
  - Manage products (create/edit/delete) and **attach images** (preview, remove).
  - Show validation messages (price > 0, required fields).
- **Product Listing (Public)**:
  - Simple grid/list of products (no search/filter required).
- **Media Management**:
  - Dedicated view to upload/manage images for the seller’s products.
  - Enforce file type/size in the UI before calling the API.
- **Technical**
  - Use **route guards** (AuthGuard, RoleGuard), **HTTP interceptors** (attach token, handle 401/403), **Reactive Forms**, and **Angular Material/Bootstrap** for responsive UI.

#### 5) Authentication & Authorization

- **Spring Security** with **JWT or OAuth2** at the gateway and propagated downstream.
- Roles: **CLIENT** (browse) vs **SELLER** (manage own products/media). **ADMIN** (optional) for moderation.
- Enforce **ownership checks** in Product/Media services (sellerId == auth.subject).

#### 6) Error Handling & Validation

- Return meaningful status codes:
  - `400` invalid input / file type / file too large
  - `401/403` unauthenticated/unauthorized
  - `404` not found (product/media not owned or missing)
  - Avoid unhandled `5xx` via global exception handlers.
- Angular: show inline form errors; display toast/snackbar for upload failures, oversized files, or forbidden actions.

#### 7) Security Measures

- **HTTPS** end-to-end (e.g., **Let’s Encrypt** for certs).
- **Password Security**: hash+salt with **BCrypt** in User Service (never expose password).
- **Input Validation**: validate filenames/MIME; verify content by **sniffing** headers; reject non-image payloads.
- **Access Control**: only the creating **seller** can modify/delete a product or its images.
- **CORS**: gateway enforces allowed origins and headers.
- (Optional) **Rate Limiting** at the gateway for auth and media endpoints.

### Constraints

- Backend split into at least **3 microservices** (User, Product, Media) + **Gateway** + **Discovery**.
- **MongoDB** for persistence; images stored in object storage (not in DB).
- Public endpoints: product **GET**s; all write operations require auth and role checks.
- Media uploads: **image/\*** only, max **2 MB**; reject others.
- Provide a comprehensive **README** with run scripts (Docker Compose encouraged).

### Evaluation

- ⚙️ **Functionality**: Role flow works; sellers can CRUD products & images; clients can browse.
- 🔐 **Security**: JWT/OAuth2, password hashing, ownership enforcement, CORS.
- 🧩 **Architecture**: Clean service boundaries, gateway & discovery configured, optional Kafka events.
- 🚫 **Reliability**: Proper error handling (no unhandled 5xx); health checks.
- 🎨 **UX**: Angular app is responsive, guards/interceptors implemented, clear validation.

### Resources

- [Spring Boot Microservices Guide](https://spring.io/guides/tutorials/rest/)
- [Spring Security (JWT/OAuth2)](https://spring.io/projects/spring-security)
- [MongoDB Docs](https://www.mongodb.com/docs/)
- [Angular Documentation](https://angular.io/docs)
- [Let’s Encrypt (HTTPS)](https://letsencrypt.org/getting-started/)


================================================================
# mr-jenk
Source: https://learn.zone01oujda.ma//api/content/root/01-edu_module/content/mr-jenk/README.md
================================================================

## MR-Jenk

### Overview

In this project, you will set up a **Continuous Integration (CI)** and **Continuous Deployment (CD)** pipeline using **Jenkins** for your **e-commerce microservices platform**.  
This pipeline will automatically **build**, **test**, and **deploy** your application, ensuring consistent and reliable delivery.

### Role Play

You are a **DevOps engineer** responsible for automating the development workflow of your e-commerce platform.  
Your mission is to build a robust Jenkins pipeline that fetches the latest code, runs automated tests, deploys the project, and notifies the team of build statuses.

### Learning Objectives

- Set up and configure **Jenkins**
- Implement **CI/CD pipelines** using Jenkins Jobs
- Integrate **automated testing** for backend and frontend
- Automate **build, test, and deployment** stages
- Implement **notifications** and basic **rollback strategies**
- Apply **best practices** in automation and deployment

### Instructions

#### 1. Setting Up Jenkins

- Download, install, and configure **Jenkins**.
  - 💡 _Hint_: Use Jenkins official documentation or Docker to set up Jenkins.
- Set up build agents if necessary.

#### 2. Create a CI/CD Pipeline for Your E-commerce Platform

- Create a Jenkins job that fetches the source code from your Git repository (e.g., GitHub).
- Set up build triggers to initiate a build automatically whenever there’s a new commit.

#### 3. Automated Testing

- Integrate automated testing into your pipeline.
  - 💡 _Hint_: Use **JUnit** for backend testing and **Jasmine/Karma** for Angular frontend testing.
- Ensure the pipeline fails when a test fails.

#### 4. Deployment

- Automatically deploy your application to a server or platform of your choice after successful builds.
  - Examples: AWS, Heroku, or a local server.
- Implement a **rollback strategy** in case a deployment fails.

#### 5. Notifications

- Set up **email** or **Slack notifications** to inform team members of build status (success or failure).

### Constraints

- The pipeline must automate the entire process from **build → test → deploy**.
- Failures in any stage should stop the pipeline.
- Notifications must clearly indicate build and deployment results.
- The system should support rollback in case of deployment errors.

### Evaluation

Your CI/CD setup will be assessed on:

- ⚙️ **Automation**: Proper and automatic code fetching and build triggering
- 🧪 **Testing Integration**: Correct handling of automated tests and failures
- 🚀 **Deployment**: Reliable and repeatable deployment process
- 🔔 **Notifications**: Accurate and timely build and deployment updates
- 🧩 **Pipeline Quality**: Clear structure, proper stage configuration, and maintainability
- (⭐ Bonus) Effective use of parameterized and distributed builds

### Bonus Features (Optional but Recommended)

- **Parameterized Builds**: Allow customization of parameters (e.g., environment selection) for each build run
- **Distributed Builds**: Use multiple build agents for parallel or multi-environment builds

### Resources

- [Jenkins Official Documentation](https://www.jenkins.io/doc/)
- [JUnit Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Jasmine/Karma Testing for Angular](https://angular.io/guide/testing)


================================================================
# safe-zone
Source: https://learn.zone01oujda.ma//api/content/root/01-edu_module/content/safe-zone/README.md
================================================================

## SafeZone

### Overview

In this project, you will improve the **code quality** and **security** of your e-commerce microservices platform by integrating **SonarQube** into your development workflow.  
The goal is to automate code analysis, detect potential vulnerabilities, and ensure continuous monitoring of your codebase to maintain clean, secure, and maintainable code.

### Role Play

You are a **DevOps and quality assurance engineer** responsible for implementing continuous code quality checks across all microservices.  
Your mission is to configure **SonarQube**, connect it to your GitHub repository, and integrate it with your CI/CD pipeline to automatically detect and prevent poor-quality or insecure code from being merged.

### Learning Objectives

- Set up **SonarQube** using Docker
- Configure SonarQube for **multi-service projects**
- Integrate **SonarQube with GitHub** and CI/CD pipelines
- Automate **static code analysis** during builds
- Enforce **quality gates** to prevent merging insecure or low-quality code
- Establish a **review and approval process** for code improvements
- Promote continuous **monitoring and feedback** of code quality metrics

### Instructions

#### 1. SonarQube Setup with Docker

- Pull the official **SonarQube Docker image** from Docker Hub and run it locally.
- Ensure the service is accessible through your browser on the configured port.
- 💡 _Hint_: Use the official SonarQube documentation for Docker setup.

#### 2. SonarQube Configuration

- Access the **SonarQube dashboard** from your local environment.
- Configure a new project for your **e-commerce microservices** codebase.
- Set up appropriate project keys, tokens, and quality profiles.

#### 3. GitHub Integration

- Integrate **SonarQube** with your GitHub repository.
- Configure **webhooks or GitHub Actions** to trigger code analysis on every push.
- Ensure all branches and pull requests are scanned for quality and security issues.

#### 4. Code Analysis

- Automate **static code analysis** through your **CI/CD pipeline**.
- Configure your pipeline to **fail automatically** if SonarQube detects:
  - Major vulnerabilities
  - Code smells
  - Failing quality gates or coverage thresholds

#### 5. Continuous Monitoring

- Schedule **regular SonarQube scans** to continuously monitor code health.
- Review SonarQube dashboards for key metrics such as coverage, duplication, maintainability, and security hotspots.

#### 6. Review and Approval Process

- Establish a **mandatory code review** process before merging pull requests.
- Require all identified issues to be resolved or justified before approval.
- Track quality improvements over time through SonarQube reports.

### Constraints

- Use **SonarQube via Docker** for setup and execution
- Integrate SonarQube with **GitHub** for automatic scanning
- Link SonarQube with your **CI/CD pipeline** for build-time analysis
- The pipeline must **fail** if quality gates are not met
- Maintain a visible dashboard of key quality metrics

### Evaluation

Your project will be assessed based on:

- 🧱 **Setup & Configuration**: Correct installation and Docker-based deployment of SonarQube
- 🔗 **Integration**: Proper linking of SonarQube with GitHub and the CI/CD pipeline
- 🔍 **Code Analysis**: Accurate detection and reporting of code issues and vulnerabilities
- ⚙️ **Automation**: Automatic scans triggered on every push or pull request
- 👥 **Process**: Clear code review and approval workflow integrated with quality gates

### Bonus Features (Optional but Recommended)

- **Notifications**: Set up email or Slack alerts for code analysis results
- **IDE Integration**: Connect SonarLint (or similar tools) to IDEs for real-time feedback

### Resources

- [SonarQube Official Documentation](https://docs.sonarqube.org/latest/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [SonarQube Docker Image – Docker Hub](https://hub.docker.com/_/sonarqube)


================================================================
# buy-02
Source: https://learn.zone01oujda.ma//api/content/root/01-edu_module/content/buy-02/README.md
================================================================

## Buy 02

### Overview

In this project, you will finalize a **full e-commerce web application** by adding core features: **shopping cart**, **orders**, **user & seller profiles with analytics**, and **product search/filtering**.  
You’ll integrate work from previous projects (auth, product/media, microservices) and enforce **best practices**: PR workflows, CI/CD (Jenkins), and code quality (SonarQube).

### Role Play

You are a **full-stack engineer** wrapping up the marketplace for launch.  
Your mission: deliver a **secure**, **performant**, and **user-friendly** experience from browsing → cart → checkout → order tracking, with clean code, CI/CD, and clear documentation.

### Learning Objectives

- Design and extend **data models** for carts, orders, and profiles
- Implement **RESTful APIs** for carts, orders, search, and analytics
- Build **Angular** views (cart, checkout, order history, dashboards)
- Add **search & filtering** (text + facets) with efficient querying
- Apply **error handling**, **validation**, and **role-based security**
- Practice **team workflows** (PRs, reviews) and **CI/CD** with Jenkins
- Measure and improve **code quality** using **SonarQube**
- Write **tests** (unit, integration, e2e) for critical flows

### Instructions

#### Database Design

Extend your existing schema (MongoDB collections or SQL tables—consistent with prior work)

#### Best Practices Ecosystem Familiarity

- Create feature branches; open **PRs** for every change.
- Perform **code reviews** (security, performance, readability).
- Use **Jenkins**: pipeline stages = _build → test → quality gate → package → deploy to staging_.
- Protect main branch: require **approved reviews** and **green pipeline**.

#### API Development Enhancement

**Orders MicroService:**

- Implement orders page to follow the order status.
- List the orders for both users and sellers using a search functionality.
- Implement orders management to remove, cancel, or redo orders.

**User Profile:**

- Develop a user profile section where users can see their best products, most buying products, and how much money they spent.
- Develop a seller profile section where they can see their best-selling products and how much money they gained.

**Search and Filtering:**

- Implement search functionality that enables users to search for products based on keywords.
- Add filtering options to refine product searches.

**Shopping Cart Implementation:**

- Create a shopping cart functionality that allows users to add products to their cart.
- Enable users to complete the purchase of the selected products in the shopping cart by selecting the "pay on delivery" feature.

**Code Quality and Best Practices:**

- Utilize SonarQube to ensure code quality and adherence to best practices.
- Address any code quality issues reported by SonarQube in your PRs.
- Document the improvements made based on SonarQube feedback.

#### Front-End Development with Angular

- **Cart & Checkout**
  - Cart page (list, qty edit, remove, subtotal/total).
  - Checkout wizard (address → review → confirm "Pay on Delivery").
- **Orders**
  - Order list with search (status/date).
  - Order details page with status timeline and totals.
- **Profiles**
  - User dashboard: total spent, most bought, top categories (charts).
  - Seller dashboard: revenue, best-selling products, units sold (charts).
- **Search & Filtering**
  - Product search page with keyword bar, filters (category, price sliders), sort, pagination.
- **UX/Tech**
  - Angular **route guards** (auth), **HTTP interceptors** (token, 401/403), **Reactive Forms**, responsive layout (Angular Material/Bootstrap).

#### Error Handling & Validation

- Backend: global exception handlers; consistent error schema `{ code, message, details }`.
- Common statuses: `400` validation, `401/403` auth, `404` missing, `409` conflicts, avoid unhandled `5xx`.
- Frontend: display inline form errors, snackbars/toasts for API failures, optimistic UI where safe.

#### Security Measures

- Reuse prior security setup: **JWT/OAuth2**, **role checks**, **ownership enforcement**.
- Protect sensitive data (no secrets in responses, mask PII where needed).
- Enforce **HTTPS**, secure cookies (if used), CORS at gateway.
- Server-side checks for: user vs seller capabilities, order ownership, and mutable status rules.

### Constraints

- Must implement: **Cart**, **Orders**, **Profiles (analytics)**, **Search/Filter**, **Pay on Delivery**.
- Keep public endpoints read-only (e.g., product search); all mutations require auth.
- Pipeline must include **tests + SonarQube** quality gate.

### Evaluation

- 🛒 **Cart & Checkout**: add/update/remove items; create order with Pay on Delivery.
- 📦 **Orders**: list/search, details, cancel/redo; seller can list related orders.
- 👤 **Profiles & Analytics**: user spend & most bought; seller revenue & best sellers.
- 🔎 **Search/Filters**: keyword + facets with pagination.
- 🎨 **UX**: responsive, accessible, clear flows.
- 🧪 **Testing**: unit/integration/e2e for critical paths.
- 🔐 **Security & Robustness**: proper authZ, no unhandled 5xx.
- 🧭 **Process**: PRs, reviews, green CI, SonarQube issues addressed.

### Bonus (Optional)

- **Wishlist** (save for later).
- Additional **payment methods** (simulate gateway integration).

### Resources

- **Backend**
  - Spring Boot & REST: https://spring.io/guides
  - Spring Security (JWT/OAuth2): https://spring.io/projects/spring-security
- **Data & Search**
  - MongoDB Docs: https://www.mongodb.com/docs/
  - (Optional) Elasticsearch: https://www.elastic.co/guide/
- **Frontend**
  - Angular Docs: https://angular.io/docs
  - Angular Material: https://material.angular.io/
- **Pipeline & Quality**
  - Jenkins Pipelines: https://www.jenkins.io/doc/pipeline/tour/getting-started/
  - SonarQube: https://docs.sonarsource.com/sonarqube/


================================================================
# nexus
Source: https://learn.zone01oujda.ma//api/content/root/01-edu_module/content/nexus/README.md
================================================================

## Nexus

### Overview

In this project, you will learn to use **Nexus Repository Manager** by **Sonatype** as a centralized platform for **storing, managing, and deploying software artifacts**.  
You will configure Nexus for artifact hosting, integrate it with **Maven** and **Docker**, and establish continuous integration pipelines to automate artifact publishing and dependency management.  
This project focuses on mastering artifact versioning, repository configuration, and secure access control within a CI/CD workflow.

### Role Play

You are a **DevOps engineer** responsible for setting up an internal **artifact management system** for your organization’s software projects.  
Your goal is to deploy and configure **Nexus Repository Manager**, connect it with build tools like Maven and Docker, and ensure seamless integration into the team’s CI/CD pipeline.

### Learning Objectives

- Install and configure **Nexus Repository Manager**
- Manage **Maven** and **Docker** repositories for artifact storage
- Publish and version **software artifacts** (JARs, WARs, Docker images)
- Use Nexus as a **dependency proxy** for builds
- Integrate **Nexus** into a **CI/CD pipeline** for automated artifact deployment
- Apply **security best practices** (RBAC, authentication, access control)
- Document and maintain an artifact management process

### Instructions

#### 1. Nexus Setup

- Download and install the latest version of **Nexus Repository Manager** (OSS or Pro).
- Run Nexus under a dedicated **"nexus" user** (not as root).
- Create and configure repositories for multiple artifact types:
  - **Maven (JAR/WAR)**
  - **Docker images**
  - **Hosted**, **Proxy**, and **Group** repositories as required

#### 2. Sample Web Application

- Clone the provided **buy-02** project repository.
- Configure it as a **Maven project** to build deployable artifacts (e.g., WAR or JAR).

#### 3. Artifact Publishing

- Configure your **Maven `settings.xml`** file to publish artifacts to Nexus.
- Use the appropriate repository credentials and repository URLs.
- Deploy the built artifacts to the corresponding Nexus repositories.

#### 4. Dependency Management

- Configure Nexus as a **proxy repository** to fetch external dependencies.
- Modify Maven configuration to **resolve dependencies** exclusively through Nexus repositories.
- Verify successful dependency retrieval and caching.

#### 5. Versioning

- Implement **artifact versioning** through Maven build configuration.
- Demonstrate retrieving and managing **multiple versions** of artifacts within Nexus.
- Show how version control aids rollback or dependency traceability.

#### 6. Docker Integration

- Create a **Docker repository** in Nexus.
- Build and tag your web application Docker image.
- Publish the image to Nexus using `docker push`.
- Confirm image retrieval from Nexus via `docker pull`.

#### 7. Continuous Integration (CI)

- Extend your CI pipeline (e.g., Jenkins, GitHub Actions) to include **artifact publishing** as part of the build process.
- Trigger the pipeline on every code change to automatically build, test, and publish artifacts to Nexus.

#### 8. Documentation

- Provide clear documentation describing:
  - Nexus setup and configuration steps
  - Maven and Docker integration
  - CI/CD publishing workflow
- Include **screenshots** and **example commands** demonstrating artifact deployment and retrieval.

#### 9. Bonus – Nexus Security and Access Control

- Explore and configure **Nexus security settings**:
  - User authentication
  - Role-based access control (RBAC)
  - Repository-level permissions
- Restrict access to specific repositories or artifacts based on user roles.

### Constraints

- Must use **Java 11** and a **compatible version of Maven**
- Nexus must not run under the root user
- Artifacts must be correctly versioned and published to the Nexus repositories
- The CI/CD pipeline must automate artifact building and publishing
- Proper documentation must accompany the implementation

### Evaluation

Your project will be evaluated based on:

- 🏗️ **Setup & Configuration**: Correct Nexus installation and repository setup
- ⚙️ **Integration**: Successful Maven and Docker integration with Nexus
- 🚀 **Automation**: Functional CI/CD pipeline with automated artifact publishing
- 🔄 **Version Control**: Proper management of artifact versions
- 🔐 **Security**: Implementation of authentication and access control policies
- 📘 **Documentation**: Clear and complete setup and usage guide

### Resources

- [Nexus Repository Manager Documentation](https://help.sonatype.com/repomanager3)
- [Maven Repository Configuration Guide](https://maven.apache.org/guides/mini/guide-configuring-maven.html)
- [Docker Hub – Nexus Image](https://hub.docker.com/r/sonatype/nexus3)
- [Jenkins Integration with Nexus](https://www.jenkins.io/doc/book/pipeline/)
- [Sonatype Security and Access Management](https://help.sonatype.com/repomanager3/security)

