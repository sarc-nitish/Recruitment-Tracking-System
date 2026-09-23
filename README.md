# Recruitment Tracking System (RTS)

A simple backend project: recruiters post jobs, candidates apply, and each application moves through a hiring pipeline.

**Tech used (only these):** Spring Boot, REST API, CRUD, MySQL, JPA/Hibernate, DTO + Validation, Spring Security, Global Exception Handling.

## How to run

1. Install Java 21 and MySQL. Make sure MySQL is running.
2. Go to `src/main/resources/`, copy `application.properties.example` and name the copy `application.properties`.
   Open it and put **your MySQL password** in `spring.datasource.password`.
   (`application.properties` is in `.gitignore`, so your password is never pushed to GitHub.)
3. Run `RtsApplication` from your IDE (or `mvn spring-boot:run`).
   The database `rts_rest_db` and all tables are created automatically.
4. Test with Postman: import `postman/RTS.postman_collection.json` and run the requests from top to bottom.

## Pipeline

```
APPLIED -> TEST -> INTERVIEW -> SELECTED
              (REJECTED is possible from any stage before SELECTED)
```

* Recruiter sends a test link -> status `TEST`.
* Candidate presses **mark test as completed** -> `testCompletedAt` is stamped (status stays `TEST`).
* Only after that the recruiter can enter the test score and schedule the interview -> `INTERVIEW`.
* Then the recruiter selects or rejects.
* Every step checks the current status, so no stage can be skipped.

## Roles and login

* `RECRUITER` - belongs to a company. Manages jobs and applications **of his own company only**.
* `CANDIDATE` - applies to jobs, sees his own applications, marks the test as completed.
* Login = **HTTP Basic Auth** (email + password with every request; in Postman use the *Authorization -> Basic Auth* tab).
* Passwords are stored as BCrypt hashes.

## Endpoints

| Method | URL | Who | What |
|---|---|---|---|
| POST | `/api/auth/register` | public | register a RECRUITER or CANDIDATE |
| GET | `/api/auth/me` | logged in | who am I |
| GET | `/api/jobs` | public | all OPEN jobs |
| GET | `/api/jobs/{id}` | public | one job |
| GET | `/api/jobs/my` | recruiter | all jobs of my company |
| POST | `/api/jobs` | recruiter | create job |
| PUT | `/api/jobs/{id}` | recruiter (same company) | update job |
| DELETE | `/api/jobs/{id}` | recruiter (same company) | delete job (and its applications) |
| POST | `/api/jobs/{jobId}/apply` | candidate | apply to a job |
| GET | `/api/applications/my` | candidate | my applications |
| PUT | `/api/applications/{id}/complete-test` | candidate | mark test as completed |
| GET | `/api/applications?jobId=&status=` | recruiter | my company's applications (filters optional) |
| GET | `/api/applications/{id}` | owner candidate / same-company recruiter | one application |
| PUT | `/api/applications/{id}/send-test` | recruiter | APPLIED -> TEST |
| PUT | `/api/applications/{id}/schedule-interview` | recruiter | TEST -> INTERVIEW |
| PUT | `/api/applications/{id}/select` | recruiter | INTERVIEW -> SELECTED |
| PUT | `/api/applications/{id}/reject` | recruiter | any stage -> REJECTED |

## Error format (Global Exception Handling)

Every error has the same JSON shape:

```json
{
  "timestamp": "2026-09-24T10:15:30",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": { "email": "Email is not valid" }
}
```

| Status | When |
|---|---|
| 400 | validation failed, broken JSON, or a business rule (e.g. wrong pipeline stage) |
| 401 | no / wrong email + password |
| 403 | wrong role, or data of another company / another candidate |
| 404 | job or application not found |
| 409 | duplicate (email already registered, already applied) |
| 500 | anything unexpected |

## Project structure

```
controller/   REST endpoints (JSON in, JSON out) - no business logic
service/      business logic and rules (pipeline, company check, match score)
repository/   Spring Data JPA interfaces (database access)
entity/       JPA entities = database tables (User, Job, JobApplication)
dto/          request / response objects with validation annotations (records)
security/     Spring Security config (Basic Auth, stateless, roles)
exception/    custom exceptions + GlobalExceptionHandler
postman/      Postman collection to test every API
```

Flow of one request: `Postman -> Controller (@Valid DTO) -> Service (rules) -> Repository -> MySQL`, and the answer goes back as a Response DTO.

## Note

Registration lets the caller choose the role (RECRUITER / CANDIDATE) to keep this demo simple. A real product would create recruiter accounts through an admin.