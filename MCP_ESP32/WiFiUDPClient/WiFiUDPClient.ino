/*
 *  This sketch sends random data over UDP on a ESP32 device
 *
 */
#include <SPI.h>
#include <WiFi.h>
#include <NetworkUdp.h>

// WiFi network name and password:
const char *networkName = "ENGG2K3K";
int count = 0;


//IP address to send UDP data to:
// either use the ip address of the server or
// a network broadcast address
const char *udpAddress = "10.20.30.1";
const int udpPort = 2001;

//Are we currently connected?
boolean connected = false;

//The udp library class
NetworkUDP udp;

void setup() {
  // Initialize hardware serial:
  Serial.begin(115200);

  //Connect to the WiFi network
  connectToWiFi(networkName);


}

void loop() {
  //only send data when connected
    if (connected) {
    //Send a packet
    udp.beginPacket("10.20.30.177", 2000);
    udp.printf("Seconds since boot: %lu", millis() / 1000);
        udp.endPacket();
    Serial.println("Sent");
    count++;
  }

    //Wait for 1 second
  delay(1000);

}

void connectToWiFi(const char *ssid) {
  Serial.println("Connecting to WiFi network: " + String(ssid));

  // delete old config
  WiFi.disconnect(true);
  //register event handler
  WiFi.onEvent(WiFiEvent);  // Will call WiFiEvent() from another thread.

  //Initiate connection
  WiFi.begin(ssid);

  Serial.println("Waiting for WIFI connection...");
}

// WARNING: WiFiEvent is called from a separate FreeRTOS task (thread)!
void WiFiEvent(WiFiEvent_t event) {
  switch (event) {
    case ARDUINO_EVENT_WIFI_STA_GOT_IP:
      //When connected set
      Serial.print("WiFi connected! IP address: ");
      Serial.println(WiFi.localIP());
      //initializes the UDP state
      //This initializes the transfer buffer
      udp.begin(WiFi.localIP(), udpPort);
      connected = true;
      break;
    case ARDUINO_EVENT_WIFI_STA_DISCONNECTED:
      Serial.println("WiFi lost connection");
      connected = false;
      break;
    default: break;
  }


}
