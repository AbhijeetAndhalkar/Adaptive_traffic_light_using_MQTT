🚦 Adaptive Traffic Light Using MQTT (VehicleQTT)

An Intelligent Traffic Management System that uses MQTT for real-time communication between vehicles (Android app) and traffic signal controllers to dynamically adjust traffic lights.
Designed especially for emergency vehicle priority (ambulance green corridor) and smart city traffic optimization.

📌 Project Overview

This project demonstrates how vehicle data (GPS / priority) can be transmitted using MQTT from an Android application to a traffic signal controller.
The controller adapts signal timings dynamically to reduce congestion and give priority to emergency vehicles.

🔹 Key Highlights

Real-time MQTT communication

Android (Kotlin) vehicle publisher

Adaptive traffic light logic

Emergency vehicle prioritization

Smart City & ITS use-case

🏗️ System Architecture
+--------------------+
| Android Vehicle App|
| (Kotlin + MQTT)    |
+---------+----------+
          |
          | MQTT Publish
          v
+--------------------+
| MQTT Broker        |
| (Mosquitto)        |
+---------+----------+
          |
          | MQTT Subscribe
          v
+--------------------+
| Traffic Controller |
| (Python / RPi)     |
+---------+----------+
          |
          v
+--------------------+
| Traffic Lights     |
| (Adaptive Timing)  |
+--------------------+

📡 MQTT Topics Used
Topic	Purpose
vehicle/location	Publishes vehicle GPS data
vehicle/type	Identifies emergency vehicles
traffic/signal	Traffic light control messages
traffic/status	Current signal state
🚑 Use Cases
1️⃣ Ambulance Green Corridor

Ambulance publishes its location

Traffic signals turn GREEN on its route

Reduces emergency response time

2️⃣ Traffic Congestion Control

Vehicles send traffic density data

Signals adjust green time dynamically

3️⃣ Smart City Demonstration

Real-time IoT based traffic automation

Scalable for city-wide deployment

📱 Android Application (VehicleQTT)
Features

MQTT publisher

Vehicle identification

Real-time data transmission

Lightweight and fast communication

Built With

Kotlin

Android Studio

MQTT protocol

Gradle (KTS)

🖥️ Python / Raspberry Pi Subscriber (Traffic Controller)

A Python MQTT subscriber simulates the traffic light controller and decides signal states based on incoming messages.

▶️ How to Run the Project
🔹 Step 1: MQTT Broker

Install and run Mosquitto:

sudo apt install mosquitto mosquitto-clients
mosquitto

🔹 Step 2: Android App

Open project in Android Studio

Configure MQTT broker IP

Run the app on a real device or emulator

Start publishing vehicle data

🔹 Step 3: Python Traffic Controller

Run the subscriber script:

python traffic_controller.py

📷 Screenshots

(Add screenshots here)

/screenshots/app_home.png
/screenshots/mqtt_logs.png
/screenshots/traffic_signal.png

🎓 Academic & Resume Value

Intelligent Transportation Systems (ITS)

IoT & MQTT

Android Development

Real-time systems

Smart City applications

👤 Author

Abhijeet Andhalkar
BE Electronics & Telecommunication

✅ 2️⃣ USE CASE MENTION (INTERVIEW-READY)

You can say this confidently in interviews:

"This project creates an ambulance green corridor using MQTT, where an Android app publishes vehicle data in real time, and traffic signals dynamically adjust to provide priority, reducing congestion and emergency response time. It is scalable for smart city traffic systems."

✅ 3️⃣ PYTHON MQTT SUBSCRIBER (TRAFFIC LIGHT SIMULATION)

Save this as traffic_controller.py

import paho.mqtt.client as mqtt
import time

BROKER = "localhost"   # Change to broker IP
PORT = 1883

GREEN_TIME = 10
RED_TIME = 5

def on_connect(client, userdata, flags, rc):
    print("Connected to MQTT Broker")
    client.subscribe("vehicle/type")
    client.subscribe("vehicle/location")

def on_message(client, userdata, msg):
    payload = msg.payload.decode()

    if msg.topic == "vehicle/type":
        if payload.lower() == "ambulance":
            print("\n🚑 Ambulance detected!")
            set_green_priority()
        else:
            print("Normal vehicle detected")

def set_green_priority():
    print("🟢 GREEN signal activated for ambulance")
    time.sleep(GREEN_TIME)
    print("🔴 Signal back to normal operation")

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.connect(BROKER, PORT, 60)
client.loop_forever()

📌 What This Demonstrates

MQTT subscription

Emergency detection logic

Adaptive traffic signal behavior

Raspberry Pi compatibility  add this to the project
