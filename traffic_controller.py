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
