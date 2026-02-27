# AWS Cloud Governance Tool – Sprint 1

**Module:** User & Cloud Account Onboarding + Basic Cost Dashboard  
**Branch:** `SE`  
**Java:** 17  |  **UI:** JavaFX 17 + External CSS  |  **DB:** MySQL (XAMPP)

---

## Sprint 1 Scope

| Story | Description | Status |
|-------|-------------|--------|
| US-01a | Register user | ✅ |
| US-01b | Login user | ✅ |
| US-01c | Logout | ✅ |
| US-02a | AWS Credential form UI (separate screen) | ✅ |
| US-02b | Encrypt + store credentials in DB | ✅ |
| US-02c | "Test Connection" mock (format check only) | ✅ |
| US-03a | Dashboard reads local DB totals | ✅ |

> **Postponed to Sprint 2:** EC2/RDS/ECS/SageMaker monitoring, idle detection, rules, alerts, live AWS SDK sync.

---

## Four Structured Specs (SDA-style)

### 1 – Login (US-01b)
| | |
|---|---|
| **Preconditions** | User record exists in `users` table |
| **Main flow** | Enter username + password → validate against DB → open Dashboard (or Credentials Setup if none saved) |
| **Alternate** | Invalid credentials → red error label shown |

### 2 – Register (US-01a)
| | |
|---|---|
| **Preconditions** | Username must be unique |
| **Main flow** | Fill form (name, username, email, password, confirm) → hash/store → navigate to Credentials Setup |
| **Alternate** | Duplicate username / validation failure → show error |

### 3 – Save Credentials (US-02b)
| | |
|---|---|
| **Preconditions** | User is logged in |
| **Main flow** | Input Access Key + Secret Key + Region → (optional mock test) → save to `aws_credentials` table linked to user ID |
| **Alternate** | Empty fields → show validation error |

### 4 – View Dashboard (US-03a)
| | |
|---|---|
| **Preconditions** | User is logged in |
| **Main flow** | Query DB → display: user count, credential status, active region, EC2/RDS/ECS counts (0 in Sprint 1), monthly cost ($0 until Sprint 2 syncs) |
| **Logout** | US-01c – click Logout → back to Login screen |

---

## Application Flow

```
App.start()
    └─ login.fxml  (LoginController)
           ├─ [no credentials] → credentials.fxml  (CredentialsController)  → dashboard.fxml
           └─ [credentials saved] ───────────────────────────────────────────→ dashboard.fxml
                                                                                    │
                    signup.fxml (SignupController) → credentials.fxml ──────────────┘
```

---

## Screens

| Screen | FXML | Controller |
|--------|------|------------|
| Login | `login.fxml` | `LoginController` |
| Register | `signup.fxml` | `SignupController` |
| AWS Credentials Setup | `credentials.fxml` | `CredentialsController` |
| Dashboard | `dashboard.fxml` | `DashboardController` |

---

## CSS Architecture

All styling is in **`src/main/resources/css/styles.css`**.  
No `style="..."` inline attributes are used in any FXML file.

Key style classes:

| Class | Purpose |
|-------|---------|
| `.auth-panel` | Login / signup / credentials card container |
| `.primary-button` | Main orange action button |
| `.secondary-button` | Outlined orange button |
| `.nav-button` / `.nav-button-active` | Dashboard sidebar |
| `.stat-card` | Stats bar tiles |
| `.info-card` | Dashboard summary cards |
| `.badge-success/warning/error` | Inline status chips |
| `.error-label` / `.success-label` | Form feedback |

---

## Setup

### 1 – Database
```sql
-- In phpMyAdmin / MySQL CLI:
SOURCE database_schema_sprint1.sql;
```

Default admin account is created automatically:  
- **Username:** `admin`  
- **Password:** `admin123`

### 2 – DBConnection config
Edit `src/database/DBConnection.java` if your MySQL password is not empty:
```java
private static final String PASSWORD = "your_password_here";
```

### 3 – Build & Run
```bash
mvn clean javafx:run
```

Or package:
```bash
mvn clean package
java -jar target/aws-governance-1.0.0.jar
```

---

## Screenshots (Sprint 1 deliverables)
Take screenshots of:
1. **Login screen** – `login.fxml`
2. **Register screen** – `signup.fxml`
3. **AWS Credential Setup screen** – `credentials.fxml`
4. **Dashboard screen** – `dashboard.fxml` (shows $0.00 cost and 0 resources until Sprint 2)
