const int SENSOR_PIN = 2;
int lastState = HIGH;

void setup() {
  Serial.begin(9600);
  pinMode(SENSOR_PIN, INPUT_PULLUP);
  
  // 💡 시작하자마자 센서의 실제 신호 상태를 미리 읽어서 맞춰둡니다.
  delay(10); // 전원 안정화 대기
  lastState = digitalRead(SENSOR_PIN); 
}

void loop() {
  int currentState = digitalRead(SENSOR_PIN);

  // 물체가 지나갈 때 (신호가 HIGH에서 LOW로 바뀔 때만)
  if (lastState == HIGH && currentState == LOW) {
    Serial.println("CHECK");
    delay(500); // 중복 감지 방지
  }

  lastState = currentState;
}