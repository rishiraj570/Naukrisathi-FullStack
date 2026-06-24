# NaukriSaathi – Spring Boot Backend

Java 17 + Spring Boot 3.2 + MongoDB backend for NaukriSaathi internship platform.

## Prerequisites
- Java 17+
- Maven 3.8+
- MongoDB Atlas cluster (or local MongoDB)

## Setup

### 1. Configure MongoDB
Edit `src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=mongodb+srv://<username>:<password>@<cluster>.mongodb.net/naukrisaathi?retryWrites=true&w=majority
```
Replace `<username>`, `<password>`, `<cluster>` with your Atlas credentials.

### 2. Run the server
```bash
mvn spring-boot:run
```
The server starts on **http://localhost:5000**

### 3. Connect the frontend
Open `frontend/index (2).html` (and other HTML files) via a local server — NOT file:// protocol.  
Use VS Code Live Server, or:
```bash
cd frontend
npx serve .
```
The frontend's `api.js` already points to `http://localhost:5000/api`.

## API Endpoints

### Auth  (`/api/auth`)
| Method | Route | Description |
|--------|-------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT |
| GET  | `/api/auth/me` | Get current user (Bearer token) |
| POST | `/api/auth/logout` | Logout (client clears token) |

### Internships  (`/api/internships`)
| Method | Route | Description |
|--------|-------|-------------|
| GET | `/api/internships` | Get all (supports `?keyword=&city=&type=&minPay=&maxPay=`) |
| GET | `/api/internships/featured` | Get featured internships |
| GET | `/api/internships/:id` | Get single internship |
| POST | `/api/internships` | Create internship (recruiter only) |
| PUT | `/api/internships/:id` | Update internship |
| DELETE | `/api/internships/:id` | Delete internship |

### Applications  (`/api/applications`)
| Method | Route | Description |
|--------|-------|-------------|
| POST | `/api/applications` | Apply to internship (student) |
| GET  | `/api/applications/mine` | My applications with internship details |
| GET  | `/api/applications/internship/:id` | Applications for a listing (recruiter) |
| PUT  | `/api/applications/:id/status` | Update application status |
| DELETE | `/api/applications/:id` | Withdraw application |

### Users  (`/api/users`)
| Method | Route | Description |
|--------|-------|-------------|
| GET | `/api/users/profile` | Get profile |
| PUT | `/api/users/profile` | Update profile |
| POST | `/api/users/save/:internshipId` | Toggle save/unsave internship |
| GET | `/api/users/saved` | List saved internships |
| GET | `/api/users/stats` | Dashboard stats |

## Data Seeding
On first startup, 10 sample internships (Google, Microsoft, Amazon, etc.) are automatically seeded into MongoDB — the frontend will show cards immediately.

## Auth Flow
- JWT is returned on login/register
- Frontend stores it in `localStorage` as `is_token`
- All protected routes require `Authorization: Bearer <token>` header
- Token expiry: 24 hours
