import serial
import requests
import time

PORT = 'COM11' # 아두이노 포트
SERVER_URL = 'https://ai-styling-kiosk.onrender.com/api/trigger' # Node.js 서버 주소

py_serial = serial.Serial(PORT, 9600, timeout=1)
print("✅ 아두이노 연결 완료. 센서 대기 중...")

while True:
    if py_serial.in_waiting > 0:
        line = py_serial.readline().decode('utf-8').rstrip()
        
        if line == "CHECK": # 아두이노가 CHECK를 보내면
            print("🎯 센서 감지! 서버로 신호 전송...")
            requests.post(SERVER_URL, json={'event': 'CHECK'}) # 서버로 신호 쏘기
            time.sleep(2) # 2초 동안 중복 인식 방지