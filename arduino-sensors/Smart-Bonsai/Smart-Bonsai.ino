#include <ArduinoJson.h>
#include <ESP8266WiFi.h>
#include <MCP3208.h>
#include <SPI.h>
#include <FirebaseArduino.h>

//set Microship up
MCP3208 adc(D8);

#define WIFI_SSID       "Atittan"
#define WIFI_PASSWORD   "kDywepao"
#define FIREBASE_HOST "smart-bonsai-f6d63.firebaseio.com"
#define FIREBASE_AUTH "xiSAEgG6d2oC8vMoW08911mAbm6JOJt9douyAA6G"
#define water_swicth D1

int temp_sensor_pin = 0, light_sensor_pin = 3, moisture_sensor_pin = 6, calibrate = 53;

void setup() {
  adc.begin();                                      //Microship read
  Serial.begin(115200);
  connectWiFi(WIFI_SSID, WIFI_PASSWORD);            //Connect to WiFi
  pinMode(water_swicth, OUTPUT);
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);     //Connect to Firebase
  Firebase.stream("water_switch/status");
}

void loop() {
  
  switchCheck();
  
  int rawValue = adc.analogRead(temp_sensor_pin);
  float light_analog_val = adc.analogRead(light_sensor_pin);
  float moisture_analog_val = adc.analogRead(moisture_sensor_pin);
  float temperature_analog_val = convertTemperature(rawValue);
  
  //Sent to Firebase
//sendObject("temperature",temperature_analog_val);
  pushFloat("temperature",temperature_analog_val);
  Serial.println("Temperature: "+ String(temperature_analog_val));
  
//sendObject("moisture",moisture_analog_val);
  pushFloat("moisture",moisture_analog_val);
  Serial.println("Moisture: "+ String(moisture_analog_val));
  
//sendObject("light",light_analog_val);
  pushFloat("light",light_analog_val);
  Serial.println("Light: "+ String(light_analog_val)+"\n");

  delay(15000);

}//End Loop

void switchCheck(){
  if (Firebase.failed()) {
    Serial.println("streaming error");
    Serial.println(Firebase.error());
  }
  
  if (Firebase.available()) {
     FirebaseObject event = Firebase.readEvent();
     String eventType = event.getString("type");
     eventType.toLowerCase();

     if (eventType == "") return ;
     Serial.print("event: ");
     Serial.println(eventType);
     if (eventType == "put") {
       String path = event.getString("path");
       bool data = event.getBool("data");
       Serial.println("\n[" + path + "] " + String(data));
       if (path == "/") {
         digitalWrite(water_swicth, (data == false ? LOW : HIGH));
       }
     }
  }
   
}

//Temperature Converter
float convertTemperature(long volt) {
  float milliVolt = volt * 3.3 / 4095.0 * 1000.0;
  float tempC = (milliVolt - 500.0) / 10.0;
  return tempC;
}

//Wifi Connector
void connectWiFi(String ssid, String pwd) {
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, pwd);
  Serial.print("\nConnecting");

  while (WiFi.status() != WL_CONNECTED) {
    Serial.print('.');
    delay(500);
  }

  Serial.println();
  Serial.print("Wifi Connected: ");
  Serial.println(WiFi.localIP());
}

//Send by Value No object
void pushFloat(String sensor_name, float value){
  String path = "sensor/"+sensor_name+"/values";
  Firebase.pushFloat(path,value);
}

//Send by Object
//void sendObject(String sensor_name, float value){
//  StaticJsonBuffer<200> jsonBuffer;
//  JsonObject& obj = jsonBuffer.createObject();
//  obj["values"] = value;
//  String path = "sensor/"+sensor_name;
//  Firebase.push(path, obj);
//}
