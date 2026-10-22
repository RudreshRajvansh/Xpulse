# XPulse

Android app for XPulse, a personal health record built on ABDM where patients decide who can see their data.

It's Kotlin with Jetpack Compose and Hilt, split into domain, data and presentation layers. One app covers eight roles: patient, doctor, receptionist, pharmacy, diagnostics, admin, super admin and customer care. Patients can share chosen parts of their record with a doctor for a limited time, get medicine reminders, read paper prescriptions with on-device OCR, and check in at a clinic by scanning the reception QR.

Staff only see their own facility's queue, and area admins only see their area. The server enforces that; the app just shows what it gets back. The full rules are in the backend README.

It talks to the [XPulse backend](https://github.com/RudreshRajvansh/Xpulse-Backend). You can change the server address from the login screen without rebuilding.

Open the project in Android Studio and run it, or build with `./gradlew assembleDebug`.
