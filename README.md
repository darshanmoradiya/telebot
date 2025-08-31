# 📱 Dtob – Telegram Controlled Android Bot

Dtob (short for **Device Telegram Bot**) is a modular Android application built with **Kotlin** and **Jetpack Compose** that allows you to remotely control your Android device via a **Telegram bot**.  
This project is designed for **educational, red teaming, and ethical PoC purposes only**.

---

## ✨ Features
- 🔗 Connects to a **Telegram bot** using your **bot token**.
- 📡 Automatically establishes communication once an **internet connection** is available.
- 📋 **Remote Command Execution**: Send commands from Telegram, and the app will execute them on the device.
- 🛠️ **Modular Architecture** for adding/removing features easily.
- 🖥️ Built with **Jetpack Compose** for modern UI.
- 🔒 Requests necessary permissions at runtime for full functionality.

---

## 🚀 How It Works
1. Generate a **Telegram Bot** using [BotFather](https://core.telegram.org/bots#botfather) and copy the **bot token**.
2. Add your **bot token** to the app’s configuration.
3. Install the app on your Android device.
4. Once the device connects to the internet, it will start listening for commands from your bot.
5. Control the device remotely by sending predefined commands via Telegram.

---

## 🛠️ Setup & Installation

### 1. Clone the repository
```bash
git clone https://github.com/darshanmoradiya/telebot.git
cd telebot
