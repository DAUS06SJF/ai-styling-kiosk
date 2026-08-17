import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./hanger.css";
import { io } from "socket.io-client";

const translations = {
  ko: {
    eyebrow: "PRODUCT RECOGNITION",
    title: "제품을 선택해주세요",
    description: "행거에서 원하는 제품을 선택하면 자동으로 인식됩니다.",
    waiting: "제품 선택 대기 중",
    analyzing: "선택하신 제품을 분석 중입니다...",
    analyzingSub: "잠시만 기다려주세요.",
    next: "스타일 선택하기",
  },

  en: {
    eyebrow: "PRODUCT RECOGNITION",
    title: "SELECT YOUR ITEM",
    description:
      "Select an item from the hanger and it will be recognized automatically.",
    waiting: "WAITING FOR ITEM",
    analyzing: "Analyzing your selected item...",
    analyzingSub: "Please wait a moment.",
    next: "CHOOSE STYLE",
  },

  zh: {
    eyebrow: "PRODUCT RECOGNITION",
    title: "请选择商品",
    description: "从衣架上选择您想要的商品，系统将自动识别。",
    waiting: "等待选择商品",
    analyzing: "正在分析您选择的商品...",
    analyzingSub: "请稍候。",
    next: "选择风格",
  },

  ja: {
    eyebrow: "PRODUCT RECOGNITION",
    title: "商品を選択してください",
    description: "ハンガーから商品を選ぶと自動的に認識されます。",
    waiting: "商品を待っています",
    analyzing: "選択した商品を分析しています...",
    analyzingSub: "少々お待ちください。",
    next: "スタイルを選択",
  },
};

function App() {
  const navigate = useNavigate();
  // 👇 여기서부터 딱 복사해서 navigate 아래에 붙여넣기 👇
  useEffect(() => {
    // 5000번 포트의 Node.js 서버와 연결
    const socket = io('https://ai-styling-kiosk.onrender.com/api/trigger'); 

    // 서버가 'open-url' 방송을 하면 실행됨
    socket.on('open-url', () => {
      console.log("🎯 센서 신호 도착! 다음 페이지로 이동합니다.");
      
      // 다음으로 넘어갈 페이지 주소
      navigate('/stylechoice'); 
    });

    return () => socket.disconnect(); // 화면 벗어나면 연결 끊기
  }, [navigate]);
  // 👆 여기까지 👆

  const savedLanguage = localStorage.getItem("language") || "ko";
  const text = translations[savedLanguage] || translations.ko;

  const detectedHangerCode = new URLSearchParams(window.location.search).get("hangerCode");
  const demoHangerCode = import.meta.env.VITE_DEMO_HANGER_CODE || "H-0001";
  const [status, setStatus] = useState(detectedHangerCode ? "analyzing" : "waiting");

  useEffect(() => {
    if (detectedHangerCode) {
      localStorage.setItem("selectedHangerCode", detectedHangerCode);
    }
  }, [detectedHangerCode]);

  // 실제 센서 연동 전 테스트용
  const handleTestDetection = () => {
    localStorage.setItem("selectedHangerCode", demoHangerCode);
    setStatus("analyzing");
  };

  // 임시 이동 버튼
  const handleNext = () => {
    navigate("/stylechoice");
  };

  return (
    <main className="hanger-screen">
      <section className="hanger-content">
        <p className="hanger-eyebrow">{text.eyebrow}</p>

        <h1 className="hanger-title">{text.title}</h1>

        <p className="hanger-description">{text.description}</p>

        <div className="hanger-visual">
          {status === "waiting" && (
            <>
              <svg
                className="hanger-icon"
                viewBox="0 0 240 180"
                fill="none"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M120 25C120 8 145 7 145 25C145 38 125 40 120 55"
                  stroke="currentColor"
                  strokeWidth="3"
                  strokeLinecap="round"
                />

                <path
                  d="M120 55L35 125"
                  stroke="currentColor"
                  strokeWidth="3"
                  strokeLinecap="round"
                />

                <path
                  d="M120 55L205 125"
                  stroke="currentColor"
                  strokeWidth="3"
                  strokeLinecap="round"
                />

                <path
                  d="M35 125H205"
                  stroke="currentColor"
                  strokeWidth="3"
                  strokeLinecap="round"
                />
              </svg>

              <p className="waiting-text">{text.waiting}</p>

              <div className="waiting-indicator">
                <span />
                <span />
                <span />
              </div>
            </>
          )}

          {status === "analyzing" && (
            <div className="analyzing-area">
              <div className="loading-ring" />

              <p className="analyzing-text">{text.analyzing}</p>

              <p className="analyzing-sub">{text.analyzingSub}</p>

              {/* 실제 센서/API 연결 전 임시 버튼 */}
              <button
                type="button"
                className="temporary-next-button"
                onClick={handleNext}
              >
                <span>{text.next}</span>
                <span>→</span>
              </button>
            </div>
          )}
        </div>

        {status === "waiting" && (
          <button
            type="button"
            className="sensor-test-button"
            onClick={handleTestDetection}
          >
            SENSOR TEST
          </button>
        )}
      </section>
    </main>
  );
}

export default App;

