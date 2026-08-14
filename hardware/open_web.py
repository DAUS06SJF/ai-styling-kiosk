import serial
import webbrowser
import time

# 아두이노 포트 및 속도 설정 (환경에 맞게 COM 포트 변경)
PORT = 'COM11'
BAUD_RATE = 9600

# 열고 싶은 웹사이트 주소
URL = "https://www.google.com"

try:
    py_serial = serial.Serial(PORT, BAUD_RATE, timeout=1)
    print(f"{PORT} 포트에 연결되었습니다. 센서 신호를 대기 중...")

    while True:
        if py_serial.in_waiting > 0:
            # 아두이노에서 보낸 문자열 읽기
            line = py_serial.readline().decode('utf-8').rstrip()
            
            # 아두이노가 "CHECK"를 출력하면 웹사이트 열기
            if line == "CHECK":
                print("센서 감지! 웹사이트를 엽니다.")
                webbrowser.open(URL)
                time.sleep(1) # 짧은 시간 내 중복 실행 방지

except Exception as e:
    print(f"에러 발생: {e}")