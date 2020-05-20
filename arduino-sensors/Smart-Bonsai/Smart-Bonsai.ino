//#include <ArduinoJson.h>
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

const int temp_sensor_pin = 0, light_sensor_pin = 3, moisture_sensor_pin = 6;
int calibrate = 85;
bool auto_switch = false, sw_status = false;
float moisture;

void setup() {
  adc.begin();                                      //Microship read begin
  Serial.begin(115200);                             //baud rate
  connectWiFi(WIFI_SSID, WIFI_PASSWORD);            //Connect to WiFi
  pinMode(water_swicth, OUTPUT);                    //Set pinMode for Selenoid Valve
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);     //Connect to Firebase
  Firebase.stream("/switch");                       //Detect Swtitch
  Firebase.setBool("switch/water_auto/status", auto_switch);
  Firebase.setBool("switch/water_switch/status", sw_status);
}

void loop() {
  int rawValue = adc.analogRead(temp_sensor_pin);
  float light_analog_val = adc.analogRead(light_sensor_pin);
  float moisture_analog_val = adc.analogRead(moisture_sensor_pin);
  float temperature_analog_val = convertTemperature(rawValue);
    
  pushFirebase("temperature",temperature_analog_val);
  pushFirebase("moisture",moisture_analog_val);
  pushFirebase("light",light_analog_val);
  switchCheck();
  if(auto_switch == true){
    moisture = moisture_analog_val;
    waterAutoSwitch(moisture);
  }else{
    waterManualSwitch();
  }
  Serial.println();     
  //sendObject("temperature",temperature_analog_val);

  Serial.println("auto_switch: "+String(auto_switch));
  Serial.println("water_switch: "+String(sw_status));

  delay(10000);

   if(auto_switch == true && sw_status == true){                //if you turn on auto switch but 
    Firebase.setBool("switch/water_switch/status", false);      //you forget to turn off the switch
    sw_status = false;                                          //this will be turn of the water
   }                                                            
}//end Loop


void waterAutoSwitch(float moisture){
  if(moisture <= 1500.0){
    digitalWrite(water_swicth, HIGH);
    delay(180000);
    digitalWrite(water_swicth, LOW);
    Serial.println("Water was close!");
  }else{
    digitalWrite(water_swicth, LOW);
    Serial.println("No need water!");
  }
}

void waterManualSwitch(){
  digitalWrite(water_swicth, (sw_status == false ? LOW : HIGH));    //Switch on/off
  (sw_status == false ? Serial.println("Water already close") : Serial.println("Water!"));
}        

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
       
       if (path == "/water_switch/status") {
         (data != true ? sw_status = false : sw_status = true);
       }
       
       if(path == "/water_auto/status") {
         (data == true ? auto_switch = true : auto_switch = false);    //Detect Auto/Not

       }//end Path
     }//end Event
  }
}//end switchCheck()

//Temperature Converter
float convertTemperature(long volt) {
  float milliVolt = (volt * 3.3 / 4095.0) * 1000.0;
  float tempC = (milliVolt - 500.0) / 10.00;
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
void pushFirebase(String sensor_name, float value){
  String path = "sensor/"+sensor_name+"/values";
  Firebase.pushFloat(path,value);
  Serial.println(sensor_name + ": "+ String(value));
}

//Send by Object
//void sendObject(String sensor_name, float value){
//  StaticJsonBuffer<200> jsonBuffer;
//  JsonObject& obj = jsonBuffer.createObject();
//  obj["values"] = value;
//  String path = "sensor/"+sensor_name;
//  Firebase.push(path, obj);
//}
