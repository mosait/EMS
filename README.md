# EMS – Einsatz-Dokumentations-App

Android-App zur digitalen Dokumentation von Rettungsdienst-Einsätzen, angelehnt an das DIVIDOK-Protokoll.

## Features

- **Einsatzverwaltung** – Einsätze erstellen und verwalten (Einsatzart, Rettungsmittel, Einsatzort)
- **Patientendokumentation** – Mehrere Patienten pro Einsatz erfassen
- **Erstbefund** – Bewusstseinslage, Kreislauf, Messwerte, GCS, Pupillen, EKG, Schmerz, Atmung
- **Notfallgeschehen** – Dokumentation des Notfallgeschehens
- **Erkrankung** – Diagnose aus Kategorien auswählen (Atmung, Herz-Kreislauf, ZNS, etc.)
- **Verletzung** – Verletzungsart, betroffene Körperregionen mit Schweregrad, Körper-Silhouette
- **Vitalwerte-Verlauf** – Messwerte im Zeitverlauf dokumentieren
- **Maßnahmen** – Durchgeführte Maßnahmen dokumentieren
- **Ergebnis / Übergabe** – Zustand, Transport, NACA-Score
- **Transportverweigerung** – Verweigerungsdokumentation mit Unterschrift
- **Infektionsprotokoll** – Infektionsschutz-Dokumentation
- **Export** – PDF- und DOCX-Export mit Teilen-Funktion
- **Offline-first** – Funktioniert komplett ohne Internetverbindung
- **Material Design 3** – Modernes Design mit Dark Mode Unterstützung

## Tech Stack

| Komponente  | Technologie                           |
| ----------- | ------------------------------------- |
| Sprache     | Kotlin 2.1.20                         |
| UI          | Jetpack Compose + Material Design 3   |
| Architektur | Multi-Modul, MVVM, Repository Pattern |
| Datenbank   | Room (SQLite)                         |
| DI          | Hilt / Dagger                         |
| Navigation  | Compose Navigation (Type-safe)        |
| Min SDK     | 29 (Android 10)                       |
| Target SDK  | 36                                    |

## Projektstruktur

```
EMS/
├── app/                    # Application module
├── core/
│   ├── model/              # Domain models & enums
│   ├── database/           # Room entities, DAOs, database
│   ├── data/               # Repositories
│   └── ui/                 # Theme, shared composables
└── feature/
    ├── overview/           # Einsatzliste
    ├── mission/            # Einsatz erstellen / Detail
    ├── patient/            # Patient, Erstbefund, Diagnose, Verletzung, Vitals, Maßnahmen, Ergebnis, Transportverweigerung, Infektionsprotokoll
    └── export/             # PDF / DOCX Export
```

## Build

Voraussetzungen: Android Studio mit JDK 17 und Android SDK (API 36).

```bash
# Debug APK bauen
./gradlew assembleDebug

# APK liegt dann unter:
# app/build/outputs/apk/debug/app-debug.apk
```

## Lizenz

Siehe [LICENSE](LICENSE).
