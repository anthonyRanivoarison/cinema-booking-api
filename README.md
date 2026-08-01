# CineBook API

**CineBook API** is a Spring Boot REST API for managing a cinema reservation system.

The application allows users to browse movie projections, reserve seats, and manage reservations. It also provides administration features for managing movies, rooms, and projections based on user roles.

<p align="left">
  <img src="https://skillicons.dev/icons?i=java,spring,postgres,idea,git,github" alt="Tech stack: Java, Spring Boot, PostgreSQL, IntelliJ IDEA, Git, GitHub" />
</p>

---

## Main Features

- Movie management
- Projection management
- Room and seat management
- Seat reservation
- User management
- Role-based access control (`CLIENT`, `EMPLOYEE`, `MANAGER`)

---

## Domain Model

The application is composed of the following entities:

| Entity | Description |
|---|---|
| `User` | User account with an associated role |
| `Movie` | Movie available in the catalog |
| `Projection` | Screening linked to a movie, a room, and a time slot |
| `Room` | Cinema room containing seats |
| `Seat` | Individual seat within a room |
| `Reservation` | Reservation of one or more seats for a projection |

---

## User Roles

| Role | Permissions |
|---|---|
| **CLIENT** | Browse projections and make reservations |
| **EMPLOYEE** | Manage reservations |
| **MANAGER** | Manage movies, projections, and cinema resources |
