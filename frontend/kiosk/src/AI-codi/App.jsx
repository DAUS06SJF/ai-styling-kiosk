import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { createStylingRecommendation } from "../shared/stylingApi";
import "./AI-codi.css";

const styleOptions = [
  ["MINIMAL", "깔끔하고 절제된 스타일"],
  ["STREET", "자유롭고 개성 있는 스타일"],
  ["LUXURY", "고급스럽고 세련된 스타일"],
  ["VINTAGE", "클래식하고 감성적인 스타일"],
  ["Y2K", "트렌디하고 개성 있는 스타일"],
];

const pendingRequests = new Map();
const missingProductMessage = "선택된 상품 정보가 없습니다. 행거 화면에서 상품을 먼저 인식해 주세요.";

function createOnce(key, input) {
  if (!pendingRequests.has(key)) {
    const request = createStylingRecommendation(input)
      .finally(() => pendingRequests.delete(key));
    pendingRequests.set(key, request);
  }
  return pendingRequests.get(key);
}

function App() {
  const navigate = useNavigate();
  const selectedStyle = localStorage.getItem("selectedStyle") || "MINIMAL";
  const hangerCode = localStorage.getItem("selectedHangerCode");
  const [attempt, setAttempt] = useState(0);
  const [status, setStatus] = useState("AI가 코디와 마네킹 이미지를 생성하고 있습니다.");
  const [error, setError] = useState(hangerCode ? "" : missingProductMessage);

  useEffect(() => {
    if (!hangerCode) {
      return undefined;
    }

    let disposed = false;

    const input = {
      hangerCode,
      occasion: "매장 방문",
      mood: selectedStyle,
      preferredColors: [],
    };

    createOnce(`${hangerCode}:${selectedStyle}:${attempt}`, input)
      .then((recommendation) => {
        if (disposed) return;
        localStorage.setItem("latestRecommendationId", String(recommendation.id));
        setStatus("코디 생성이 완료되었습니다. 마네킹 화면으로 이동합니다.");
        navigate("/mannequin", { replace: true, state: { recommendation } });
      })
      .catch((requestError) => {
        if (!disposed) {
          setError(requestError.message || "코디 생성에 실패했습니다.");
        }
      });

    return () => {
      disposed = true;
    };
  }, [attempt, hangerCode, navigate, selectedStyle]);

  const retry = () => {
    setError("");
    setStatus("AI가 코디와 마네킹 이미지를 생성하고 있습니다.");
    setAttempt((value) => value + 1);
  };

  return (
    <div className="ai-codi-screen">
      <div className="ai-codi-content">
        <p className="ai-codi-label">AI STYLING</p>

        <h1>
          선택한 스타일로<br />
          코디를 준비하고 있습니다
        </h1>

        <p className={`ai-codi-description ${error ? "error" : ""}`}>
          {error || status}
        </p>

        <div className="style-list">
          {styleOptions.map(([style, description]) => (
            <button
              type="button"
              className={`style-card ${selectedStyle === style ? "selected" : ""}`}
              disabled
              key={style}
            >
              <strong>{style}</strong>
              <span>{description}</span>
            </button>
          ))}
        </div>

        {error && (
          <div className="ai-codi-actions">
            <button type="button" onClick={retry}>
              다시 시도
            </button>
            <button type="button" onClick={() => navigate("/mannequin")}>
              저장된 코디 보기
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default App;
