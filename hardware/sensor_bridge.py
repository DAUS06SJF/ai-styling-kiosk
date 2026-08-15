import serial
import requests
import time

PORT = 'COM11'
BAUD_RATE = 9600
SERVER_URL = 'http://localhost:3000/api/trigger'

try:
    py_serial = serial.Serial(PORT, BAUD_RATE, timeout=1)
    print(f"✅ {PORT} 포트 연결 완료. 센서 대기 중...")

    while True:
        if py_serial.in_waiting > 0:
            line = py_serial.readline().decode('utf-8').rstrip()
            
            if line == "CHECK":
                print("🎯 센서 감지! 서버로 신호 전송...")
                try:
                    # Node.js 서버로 HTTP POST 요청 전송
                    response = requests.post(SERVER_URL, json={'event': 'CHECK'})
                    print("서버 응답:", response.json())
                except Exception as req_err:
                    print("❌ 서버 전송 실패:", req_err)
                
                time.sleep(2) # 중복 감지 방지

except Exception as e:
    print(f"❌ 시리얼 에러: {e}")