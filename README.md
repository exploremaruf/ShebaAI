
# ShebaAI 🩺
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Platform](https://img.shields.io/badge/Platform-Android-brightgreen.svg)](https://developer.android.com)

<img width="3780" height="1890" alt="ShebaAI ● 1 0" src="https://github.com/user-attachments/assets/81d0f8c9-534f-4532-97e2-5761e106ab34" />


**ShebaAI** is a  minimalist offline First-Aid assistant for Android. It bridges the gap between technology and healthcare by providing instant, AI-driven medical guidance without the need for an internet connection.

---

## ✨ Features

- **100% Offline Intelligence:** Uses Google’s MediaPipe GenAI and Gemma-2b-it model for on-device reasoning.
- **Privacy First:** No data leaves your device. Everything is processed locally.
- **Premium UI/UX:** A clean, high-contrast Black & White interface following Material 3 guidelines.
- **Smart Knowledge Base:** Integrated first-aid data specifically optimized for quick symptom checking.
- **Typewriter Effect:** A smooth, interactive chat experience with zero-latency responses.

---

## 📸 Screenshots

| Splash & Branding | Chat Interaction | Medical Guidance |
| :---: | :---: | :---: |
| ![splashscreen](https://github.com/user-attachments/assets/d70b0940-a95a-4098-ba61-36f921022f7c)|![chatinteraction](https://github.com/user-attachments/assets/05a44622-fcb7-42a8-bbc0-37b83d40c730)| ![Medicalguidance](https://github.com/user-attachments/assets/d2e6f96e-f709-4e42-b85e-9786ae90d8e5)|

---

## 🛠️ Tech Stack

- **Language:** Java (Structured for clarity and stability)
- **AI Framework:** MediaPipe GenAI SDK
- **Model:** Gemma 1.1 2B IT (Quantized)
- **UI:** Material Design 3 & Android XML
- **Data:** Structured JSON Knowledge Base

---

## 🚀 Setup & Installation

Since the AI model is large, it is not included in the repository. Follow these steps to get started:

### 1. Download the AI Model
Go to [Kaggle - Gemma 1.1 2B IT](https://www.kaggle.com/models/google/gemma/tfLite/gemma-1.1-2b-it-cpu-int4) and download the **`gemma-1.1-2b-it-cpu-int4.bin`** file.



---

### 2. Prepare the Project

1. Clone the repository:

```bash
git clone https://github.com/your-username/ShebaAi.git
```

2. Rename the downloaded model file to **`model.bin`**.

3. Place the `model.bin` file inside the following directory in your project:

```
app/src/main/assets/
```

---

### 3. Run the App

* Open the project in **Android Studio Ladybug** or later.
* Connect an Android device (physical device recommended, minimum 4GB RAM).
* Sync Gradle and click **Run**.

---

## 🛡️ Medical Disclaimer

ShebaAI is an educational and informational tool designed to provide general first-aid guidance. It is **NOT** a substitute for professional medical advice, diagnosis, or treatment. In case of a life-threatening emergency, please contact your local emergency services (e.g., **999 in Bangladesh**) immediately.

---

## 📄 License

This project is licensed under the **MIT License**. You are free to use, modify, and distribute it with proper attribution.

---

**Developed with ❤️ by Maruf Hasan**

[GitHub](https://github.com/exploremaruf) | [Portfolio](https://itsmaruf.me)

---




