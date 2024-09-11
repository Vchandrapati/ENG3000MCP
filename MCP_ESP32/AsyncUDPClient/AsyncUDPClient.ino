// Initialize pin numbers
const int touchPin = 15; 
const int RedPin = 5;
const int GreenPin = 4;

// Threshold value for touch sensitivity
const int threshold = 30;

// Variable to store the sensor reading
int touchValue;

void setup() {
  Serial.begin(115200);
  delay(1000);  // Delay to stabilize the sensor
  // Initialize the LEDs as outputs
  pinMode(RedPin, OUTPUT);
  pinMode(GreenPin, OUTPUT);
}

void loop() {
  // Read the sensor value
  touchValue = touchRead(touchPin);
  
  // If touchValue is greater than the threshold (sensor is touched)
  if (touchValue > threshold) {
    digitalWrite(GreenPin, HIGH);   // Turn on the Red LED
    digitalWrite(RedPin, LOW);  // Turn off the Green LED
  } else {
    digitalWrite(RedPin, HIGH); // Turn on the Green LED
    digitalWrite(GreenPin, LOW);    // Turn off the Red LED
    Serial.println("tripped"); // Indicates sensor has been tripped
  }
  
  delay(500);  // Wait for half a second
}
