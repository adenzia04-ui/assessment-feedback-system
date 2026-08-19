# 🎓 Assessment Feedback System (AFS)

A **Java Swing** desktop application for managing and delivering assessment feedback to students — built for the **Object-Oriented Development with Java** module at **APU**.

> 📝 A team coursework project. This repository is a **write-up / showcase** of the system and its design; the full source is kept private.

## 👥 Four user roles
- **Administrative staff** — manage users, modules and class groups, assign lecturers, and view system reports.
- **Academic leaders** — oversee lecturers and modules.
- **Lecturers** — create assessments and deliver graded feedback.
- **Students** — view their feedback and grades.

## ✨ Highlights
- **Role-based access control** built on an OOP **inheritance hierarchy** — `User` → Admin / Academic Leader / Lecturer / Student.
- **Feedback & grading** — create assessments, apply a pre-defined marks-allocation grading system, and generate feedback reports.
- **Custom Swing UI** — a themed dark interface with rounded components, a particle-animated background, and reusable dialogs.
- **File-based persistence & audit logging** — managers persist users / modules / feedback, and every admin action is written to an audit log.

## 🏗️ Architecture
Layered into `model` (Assessment, Module, Feedback, GradingSystem, ClassGroup…), `users` (the role hierarchy), `util` (DataManager, FileManager, LoginManager, UserManager, ValidationUtil, LogManager) and `gui` (frames, pages, reusable components, theme).

## 🧰 Tech
`Java` · `Swing` · `OOP (inheritance · encapsulation · polymorphism)` · `File I/O`
