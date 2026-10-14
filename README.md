# SmartTrip Planner ✈️🌍

SmartTrip Planner is a full-stack travel planning web application that helps users discover destinations and create personalized travel plans based on destination, travel dates, budget, and number of travelers.

## 🚀 Features

- User Registration
- User Login
- BCrypt Password Encryption
- JWT Authentication
- Forgot Password with OTP
- Password Reset
- Trip Planning
- Trip Duration Validation
- Budget Validation
- Personalized Day-wise Trip Plan
- Recommended Hotels
- Recommended Tourist Attractions
- Budget Breakdown
- Explore Indian Destinations
- My Trips
- View Saved Trips
- Delete Saved Trips
- Swagger API Documentation
- JUnit Testing
- GitHub Actions CI/CD
- Responsive Web Interface
- Colorful and User-Friendly UI

## 🛠️ Technologies Used

### Frontend

- React.js
- Vite
- JavaScript
- HTML5
- CSS3
- Axios / Fetch API

### Backend

- Java 17
- Spring Boot 3.x
- Spring Security
- JWT
- Spring Data JPA
- Hibernate
- Maven

### Database

- MySQL 8

### Testing

- JUnit 5
- Spring Boot Test

### Documentation

- Swagger / OpenAPI

### CI/CD

- GitHub Actions

## 🏗️ Project Structure

```text
Smarttripplanner/
│
├── Backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/
│   │   │   │       └── smarttrip/
│   │   │   │           └── smarttrip/
│   │   │   │               ├── config/
│   │   │   │               ├── controller/
│   │   │   │               ├── entity/
│   │   │   │               ├── repository/
│   │   │   │               ├── service/
│   │   │   │               └── util/
│   │   │   │
│   │   │   └── resources/
│   │   │
│   │   └── test/
│   │
│   └── pom.xml
│
├── Frontend/
│   ├── src/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   └── ...
│   ├── package.json
│   └── vite.config.js
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
└── README.md