# The Gentlemen’s Barber – Android Mobile Application

**The Gentlemen’s Barber** is a native Kotlin Android application developed by **SiboSync Solutions** (`XISD6329MM`) to modernize barbershop appointments, retail product sales, and staff operations. Synchronized in real time with *The Gentlemen’s Bar* PHP web platform (`http://gentlemensbarwebsite.infinityfreeapp.com`), the app connects customers, barbers, and managers to a shared **Firebase Cloud Firestore** backend.

* **Mobile App GitHub Repository:** https://github.com/ST10440580/The_Gentlemens_Barber
* **Companion Website GitHub Repository:** https://github.com/sibongilee/GentlemensBarWebsite
* **Live Companion Website:** http://gentlemensbarwebsite.infinityfreeapp.com
* **Video Demonstration (YouTube Unlisted):** *[Insert Molebogeng's YouTube Link Here]*

---

## Problem Statement & Purpose
Traditional barbershop workflows rely on manual appointment books, disconnected walk-in tracking, and cash-only payment logs. *The Gentlemen’s Barber* mobile app replaces these manual processes with a role-based digital platform:
* **Customers:** Register and log in via Single Sign-On (SSO), browse grooming services and pricing, book or reschedule appointments, shop grooming products, and pay securely via **Paystack**.
* **Staff (Barbers):** View real-time daily appointment schedules, log in-store walk-in clients, and mark unavailable days off to prevent double bookings.
* **Administrators (Managers):** Monitor shop-wide bookings, track expected vs. collected revenue, manage grooming services, and export operational/financial reports to PDF.

---

## Key Features & Design Considerations

### 1. Role-Based Authentication & Dashboards
* **Firebase SSO Routing:** Authenticates users via Firebase Auth and automatically routes them to the Customer, Staff, or Admin dashboard based on their Firestore user role.
* **Offline Resilience:** Pairs Cloud Firestore with an on-device SQLite cache (`barber_cache.db`) and `SharedPreferences` so users can view loaded schedules and profile details offline.

### 2. Smart Appointment Booking & Holiday Validation
* **Step-by-Step Booking Flow:** Guides customers through selecting a grooming service (`Haircut`, `Beard Trim`, `Hot Towel Shave`, `Facial`, `Executive Combo`), choosing a barber, and picking an available date and time slot.
* **Double-Booking & Public Holiday Protection:** Uses Firestore `slotLocks` (`staffId_date_HHmm`) to block overlapping time slots and integrates the **South African Public Holidays API (`api.zanode.co.za`)** to automatically alert customers and lock bookings on national holidays.

### 3. Product Store, Cart & Paystack Payments
* **Grooming Product Catalog:** Customers browse items (*Shaving Cream*, *Professional Clippers*, *Hair Conditioner*, *Beard Balm*, *Facial Cleanser*, *Barber Cape*), add products to their cart, and calculate totals dynamically.
* **Paystack Android SDK Integration:** Processes card payments directly inside the app via `PaymentActivity.kt` (`co.paystack.android:paystack:3.3.2`), generates digital receipts, and updates `paymentStatus` to `paid` across both mobile and web platforms.

### 4. Staff & Admin Management Tools
* **Staff Schedule & Walk-In Tracking:** Barbers mark appointments as `Completed`, block days off, and add walk-in clients without colliding with online reservations.
* **Customer Feedback & Admin PDF Reports:** Customers submit star ratings and reviews; administrators generate on-screen and downloadable PDF financial/operational reports.

---

## Tech Stack & Architecture
* **Language & UI:** Kotlin, Android XML Layouts, Material Design Components
* **Cloud Backend & Auth:** Firebase Authentication, Firebase Cloud Firestore (`africa-south1`)
* **Local Persistence:** SQLite (`barber_cache.db`), `SharedPreferences`
* **APIs & SDKs:** Paystack Android SDK (`3.3.2`), ZANode South African Public Holidays API (`api.zanode.co.za`)
* **DevOps, CI/CD & Testing:** Azure DevOps (7 Agile Sprints), GitHub & GitHub Actions, JUnit (`SlotsTest`, `CommonUtilsTest`, `CartLogicTest`), Espresso UI Testing

---

## GitHub & GitHub Actions Utilization
* **Version Control Workflow:** Source code is maintained cleanly on GitHub with atomic commits across team members and no compressed `.zip` source archives.
* **Automated CI/CD (GitHub Actions):** Automated workflow runs on every push/pull request to compile the Kotlin project, verify Gradle dependencies, and execute JUnit unit tests (`SlotsTest`, `CommonUtilsTest`, `CartLogicTest`) before building release artifacts.

---

## Release Notes (v2.0 – Final Production Release)
Key improvements implemented since the Formative 2 prototype:
* **[New] Live Firebase Cloud Firestore Sync:** Replaced local-only prototype persistence with real-time Cloud Firestore synchronization linking the Android app and PHP web portal.
* **[New] Paystack Payment Gateway (`PaymentActivity.kt`):** Integrated the Paystack Android SDK (`co.paystack.android:paystack:3.3.2`) with Rand-to-cent conversion and unique transaction references (`GENT_APP_`) to prevent duplicate charges.
* **[New] South African Public Holiday API:** Connected the booking calendar to `api.zanode.co.za` to block appointments on South African public holidays automatically.
* **[New] Slot-Locking Overlap Prevention:** Implemented `slotLocks` validation so two customers cannot reserve the same barber at the same date and time.
* **[Fixed] Missing Contact Formatting & Role Routing:** Standardized user profile persistence in `SharedPreferences` and Firestore role checks across Customer, Staff, and Admin accounts.

---

## Team Contact Details (SiboSync Solutions)
For inquiries or technical support regarding the application, please contact our team:
* **Sibongile Nhlapo** (Project Manager) – `ST10449136@rcconnect.edu.za`
* **Natasha Mvundlela** (Software Designer) – `ST10447508@rcconnect.edu.za`
* **Molebogeng Thupi** (System Analyst) – `ST10349188@rcconnect.edu.za`
* **Tshepang Ramohapi** (Lead Software Developer) – `ST10440580@rcconnect.edu.za`
* **Ronewa Nemubvumoni** (Quality Assurance) – `ST10445772@rcconnect.edu.za`
