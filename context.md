# Context

## Current architecture
- Java Spring Boot 3.x
- Layers: controller, service, repository, entity, dto, security, config
- PostgreSQL via Spring Data JPA
- JWT authentication using jjwt
- Frontend static pages served from `src/main/resources/static`

## Entities and relationships
- User (id, name, email, password, role)
- Project (id, name, description, createdBy)
- ProjectMember (id, project, user)
- Task (id, title, description, status, dueDate, project, assignedTo, createdBy)
- Enums: Role, TaskStatus

## APIs implemented
- None yet

## Security status
- JWT authentication scaffolded
- Password hashing using BCrypt implemented
- Role-based security configuration started

## Completed features
- Project structure initialized
- Core entities defined
- DB config bootstrap created
- Authentication module implemented
- Auth register/login endpoints created
- Project management module implemented
- Project creation, listing, and add-member endpoints created

## Pending features
- Dashboard module
- Frontend pages
- Deployment setup

## Update 2026-04-30

# Project: Team Task Manager

## Tech Stack
- Java Spring Boot 3.x
- Spring Security + JWT
- JPA/Hibernate
- PostgreSQL
- HTML/CSS/JavaScript frontend (static pages)

## Current Architecture
- Packages implemented:
  - `controller`
  - `service`
  - `repository`
  - `entity`
  - `dto`
  - `security`
  - `config`
- Clean layered architecture with DTOs and global exception handling
- Authentication layer complete and compiled successfully

## Entities & Relationships
- `User`:
  - `id`, `name`, `email`, `password`, `role`
- `Project`:
  - `id`, `name`, `description`, `createdBy` (User)
- `ProjectMember`:
  - `id`, `project` (Project), `user` (User)
- `Task`:
  - `id`, `title`, `description`, `status`, `dueDate`, `project` (Project), `assignedTo` (User), `createdBy` (User)
- Enums:
  - `Role` (`ADMIN`, `MEMBER`)
  - `TaskStatus` (`TODO`, `IN_PROGRESS`, `DONE`)

## Features Implemented
- Authentication:
  - User registration
  - User login
  - JWT generation and validation
  - BCrypt password hashing
  - First user auto-assigned `ADMIN`
- Project Management:
  - Create project (ADMIN only)
  - List user projects
  - Add member to project (ADMIN only)
- Task Management:
  - Create task
  - Get my tasks
  - Get project tasks
  - Update task status
  - Mark tasks as completed via dashboard UI
- Dashboard:
  - Dashboard statistics endpoint implemented
  - Interactive dashboard UI with task list, active/completed views, and professional completion actions
  - Project selection dropdown added for switching task views by project
  - Overdue tasks now highlight with a red accent and label in active view
  - Completed tasks styled with a softer, faded look for readability
  - Smooth task completion animation with live removal and dashboard refresh
  - UI simplified by removing unused project action
  - PUT /tasks/{id}/status now accepts JSON body and updates status cleanly
- Frontend:
  - Static folder scaffolded
- Deployment:
  - Environment variable-based DB config prepared via `application.properties`

## APIs Implemented
- `POST /auth/register` — user registration
- `POST /auth/login` — user login and JWT retrieval
- `POST /projects` — create a new project (ADMIN only)
- `GET /projects` — list projects where the logged-in user is creator or member
- `POST /projects/{projectId}/add-member` — add a user to a project (ADMIN only)
- `POST /tasks` — create a new task
- `GET /tasks/my` — get tasks assigned to current user
- `GET /tasks/project/{projectId}` — get tasks for a project
- `PATCH /tasks/{taskId}/status` — update task status
- `GET /dashboard` — retrieve current user dashboard statistics

## Database Configuration
- PostgreSQL configured in application.properties
- URL: jdbc:postgresql://localhost:5432/taskmanager
- Username: postgres
- Password: placeholder (replace ${POSTGRES_PASSWORD} with actual password)
- JPA settings: ddl-auto=update, show-sql=true, PostgreSQL dialect

## Security
- JWT authentication filter in place
- Spring Security configured with stateless session management
- Password hashing with BCrypt
- Role-based authorization enabled for ADMIN-protected project operations

## Pending Work
- Build frontend pages: `login.html`, `register.html`, `dashboard.html`, `project.html`, `task.html`
- Prepare Railway deployment manifest or instructions

## Notes
- Existing content preserved and new update appended for reuse in another session.
- Current build state: `mvn compile` passes.

## Update 2026-04-30 (Refactor)

- Base package refactored to `com.yash.taskmanager` for a cleaner production namespace.
- Updated entity relationships with proper JPA mappings and LAZY fetching:
  - `User` → `projectMemberships`, `createdProjects`
  - `Project` → `members`
- Security configuration verified and tightened:
  - CSRF disabled
  - Stateless sessions
  - JWT filter added before `UsernamePasswordAuthenticationFilter`
  - `/auth/**` endpoints publicly accessible
  - all other requests authenticated
- Application properties updated:
  - `spring.jpa.show-sql=true`
  - PostgreSQL driver class configured
- Current readiness: backend compiles cleanly and auth module remains functional.

## Update 2026-05-01 (Backend bugfixes)

- `ProjectService.addMember(...)` authorization logic finalized:
  - project creator can add members
  - ADMIN can add members
  - all others are blocked (`if !creator && !admin`)
- `ProjectService.addMember(...)` input and error handling tightened:
  - invalid/non-positive/null `userId` now rejected as `User not found`
  - unknown `userId` returns `User not found`
  - duplicate membership returns `User already a member of this project`
- `TaskService` project access checks stabilized:
  - replaced creator checks based on entity object equality with ID-based comparison
  - fixes false authorization failures for valid project creators during task operations
- Runtime note:
  - prior startup failures were due to port `8080` conflict (not code compile failure)
