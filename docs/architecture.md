# Architecture documentation

## Use cases (summary)

| Actor | Use cases |
|-------|-----------|
| Employee | Manage tasks, request leave, view payroll/documents, chat with AI |
| Team Leader | Assign/validate tasks, monitor team, approve-related views |
| Manager | Manage projects, reports, leave approvals |
| HR | Employee lifecycle, payroll, training, skills |
| Admin | Read-only system overview |

## Sequence: Login via Gateway

```mermaid
sequenceDiagram
  participant UI as Angular
  participant GW as API Gateway
  participant AUTH as Auth Service
  participant DB as auth_db

  UI->>GW: POST /api/auth/login
  GW->>AUTH: forward login
  AUTH->>DB: validate user
  AUTH-->>GW: access + refresh JWT
  GW-->>UI: tokens + HttpOnly cookies
```

## Sequence: Chatbot question

```mermaid
sequenceDiagram
  participant UI as Angular
  participant GW as API Gateway
  participant BOT as Chatbot Service
  participant HRM as HRM Service
  participant PM as Project Service
  participant GEM as Gemini API

  UI->>GW: POST /api/chatbot/ask (Bearer)
  GW->>BOT: forward with JWT
  BOT->>HRM: fetch role context (REST)
  BOT->>PM: fetch role context (REST)
  BOT->>GEM: prompt + context
  GEM-->>BOT: answer
  BOT-->>GW: response
  GW-->>UI: answer + suggestions
```

## Class diagram (simplified)

```mermaid
classDiagram
  class ApiGateway {
    +route()
    +validateJwt()
  }
  class AuthService {
    +login()
    +refresh()
  }
  class HrmService {
    +approveLeave()
    +publishEvent()
  }
  class ProjectService {
    +validateTask()
    +createProject()
  }
  class ChatService {
    +ask()
    +buildContext()
  }
  ApiGateway --> AuthService
  ApiGateway --> HrmService
  ApiGateway --> ProjectService
  ApiGateway --> ChatService
  ChatService --> HrmService
  ChatService --> ProjectService
```

## API documentation

| Service | Swagger UI | OpenAPI JSON |
|---------|------------|--------------|
| Gateway | http://localhost:8080/swagger-ui.html | http://localhost:8080/api-docs |
| Auth | http://localhost:8081/swagger-ui.html | http://localhost:8081/api-docs |
| HRM | http://localhost:8082/swagger-ui.html | http://localhost:8082/api-docs |
| Project | http://localhost:8083/swagger-ui.html | http://localhost:8083/api-docs |
| Chatbot | http://localhost:8084/swagger-ui.html | http://localhost:8084/api-docs |
