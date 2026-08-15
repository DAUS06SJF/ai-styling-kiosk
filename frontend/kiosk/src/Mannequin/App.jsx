import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Mannequin.css";

const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080")
  .replace(/\/$/, "");
const WEB_SOCKET_URL = import.meta.env.VITE_WS_URL
  || `${API_BASE_URL.replace(/^http/i, "ws")}/ws/styling`;
const MAX_LOOKS = 4;

const moodAliases = {
  MINIMAL: ["MINIMAL", "미니멀"],
  STREET: ["STREET", "스트리트"],
  LUXURY: ["LUXURY", "럭셔리"],
  VINTAGE: ["VINTAGE", "빈티지"],
  Y2K: ["Y2K"],
};

function resolveImageUrl(imageUrl) {
  if (!imageUrl) return "";

  try {
    const image = new URL(imageUrl);
    const api = new URL(API_BASE_URL);
    if (["localhost", "127.0.0.1"].includes(image.hostname)
      && !["localhost", "127.0.0.1"].includes(api.hostname)) {
      image.protocol = api.protocol;
      image.host = api.host;
    }
    return image.toString();
  } catch {
    return imageUrl;
  }
}

function categoryLabel(category = "") {
  const normalized = category.toUpperCase();
  if (normalized.includes("BAG") || normalized.includes("BACKPACK")) return "가방";
  if (normalized.includes("SHOE") || normalized.includes("SNEAKER")) return "신발";
  if (normalized.includes("PANT") || normalized.includes("TROUSER") || normalized.includes("BOTTOM")) return "하의";
  if (normalized.includes("ACCESS") || normalized.includes("JEWEL") || normalized.includes("BELT")) return "액세서리";
  return "상의";
}

function toLook(recommendation) {
  const products = [
    recommendation.selectedProduct,
    ...(recommendation.recommendations || []).map((item) => item.product),
  ].filter(Boolean);
  const uniqueProducts = products.filter(
    (product, index) => products.findIndex((candidate) => candidate.id === product.id) === index,
  );

  return {
    id: recommendation.id,
    name: recommendation.lookName || `LOOK ${recommendation.id}`,
    mood: recommendation.mood || "",
    image: resolveImageUrl(recommendation.kodi),
    kodiSelected: recommendation.kodiSelected,
    selectedProductId: recommendation.selectedProduct?.id,
    products: uniqueProducts,
  };
}

function sameMood(responseMood, selectedMood) {
  const aliases = moodAliases[selectedMood] || [selectedMood];
  return aliases.some((alias) => alias.toLowerCase() === (responseMood || "").toLowerCase());
}

function App() {
  const navigate = useNavigate();
  const selectedMood = localStorage.getItem("selectedStyle") || "MINIMAL";
  const [looks, setLooks] = useState([]);
  const [selectedLookId, setSelectedLookId] = useState(null);
  const [connectionState, setConnectionState] = useState("loading");
  const [errorMessage, setErrorMessage] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const abortController = new AbortController();

    async function loadLooks() {
      try {
        const response = await fetch(
          `${API_BASE_URL}/api/styling/recommendations?mood=${encodeURIComponent(selectedMood)}&limit=${MAX_LOOKS}`,
          { signal: abortController.signal },
        );
        const body = await response.json();
        if (!response.ok || !body.success) {
          throw new Error(body.error?.message || "코디 목록을 불러오지 못했습니다.");
        }

        const loadedLooks = (body.data || []).map(toLook);
        setLooks(loadedLooks);
        setSelectedLookId(loadedLooks[0]?.id ?? null);
        setErrorMessage("");
      } catch (error) {
        if (error.name !== "AbortError") {
          setErrorMessage(error.message || "백엔드에 연결할 수 없습니다.");
        }
      }
    }

    loadLooks();
    return () => abortController.abort();
  }, [selectedMood]);

  useEffect(() => {
    let socket;
    let reconnectTimer;
    let disposed = false;

    const connect = () => {
      setConnectionState("connecting");
      socket = new WebSocket(WEB_SOCKET_URL);

      socket.onopen = () => setConnectionState("connected");
      socket.onmessage = (message) => {
        try {
          const event = JSON.parse(message.data);
          if (event.type !== "STYLING_RECOMMENDATION_CREATED"
            || !event.data
            || !sameMood(event.data.mood, selectedMood)) {
            return;
          }

          const newLook = toLook(event.data);
          setLooks((previous) => [
            newLook,
            ...previous.filter((look) => look.id !== newLook.id),
          ].slice(0, MAX_LOOKS));
          setSelectedLookId(newLook.id);
          setErrorMessage("");
        } catch {
          setErrorMessage("실시간 코디 데이터를 읽지 못했습니다.");
        }
      };
      socket.onerror = () => setConnectionState("disconnected");
      socket.onclose = () => {
        if (!disposed) {
          setConnectionState("disconnected");
          reconnectTimer = window.setTimeout(connect, 2000);
        }
      };
    };

    connect();
    return () => {
      disposed = true;
      window.clearTimeout(reconnectTimer);
      socket?.close();
    };
  }, [selectedMood]);

  const currentLook = looks.find((look) => look.id === selectedLookId) || looks[0];
  const totalPrice = useMemo(
    () => currentLook?.products.reduce((sum, product) => sum + (product.price || 0), 0) || 0,
    [currentLook],
  );

  const handleSave = async () => {
    if (!currentLook || saving) return;

    setSaving(true);
    setErrorMessage("");
    try {
      const response = await fetch(
        `${API_BASE_URL}/api/styling/recommendations/${currentLook.id}/select`,
        { method: "POST" },
      );
      const body = await response.json();
      if (!response.ok || !body.success) {
        throw new Error(body.error?.message || "코디 저장에 실패했습니다.");
      }

      localStorage.setItem("selectedKodi", body.data.kodiSelected);
      localStorage.setItem("selectedKodiId", String(body.data.id));
      navigate("/qr-share", {
        state: { kodiSelected: body.data.kodiSelected, recommendationId: body.data.id },
      });
    } catch (error) {
      setErrorMessage(error.message || "코디 저장에 실패했습니다.");
    } finally {
      setSaving(false);
    }
  };

  const statusText = errorMessage
    || (connectionState === "connected"
      ? `${selectedMood} 코디를 실시간으로 받고 있습니다.`
      : "백엔드 실시간 연결을 기다리고 있습니다.");

  return (
    <div className="mannequin-screen">
      <header className="mannequin-header">
        <div className="brand-area">
          <p className="brand-name">AI STYLING</p>
          <h1>AI LIVE MANNEQUIN</h1>
        </div>
      </header>

      <main className="mannequin-main">
        <section className="mannequin-view">
          <div className="mannequin-stage">
            {currentLook?.image ? (
              <img
                src={currentLook.image}
                alt={`${currentLook.name} AI 추천 코디`}
                className="ai-look-image"
              />
            ) : (
              <div className="ai-image-placeholder">
                <span>AI STYLING</span>
                <p>{connectionState === "loading" ? "코디를 불러오는 중입니다." : "추천 코디를 기다리고 있습니다."}</p>
              </div>
            )}
          </div>

          <div className={`ai-status ${errorMessage ? "error" : ""}`}>
            <span />
            <div>
              <strong>AI STYLING {connectionState === "connected" ? "LIVE" : "WAITING"}</strong>
              <p>{statusText}</p>
            </div>
          </div>
        </section>

        <aside className="look-panel">
          <div className="look-title">
            <span>AI RECOMMENDATION</span>
            <strong>LOOK</strong>
          </div>

          <div className="look-list">
            {looks.map((look, index) => (
              <button
                key={look.id}
                type="button"
                className={`look-card ${selectedLookId === look.id ? "active" : ""}`}
                onClick={() => setSelectedLookId(look.id)}
              >
                <div className="look-number">{String(index + 1).padStart(2, "0")}</div>
                <div className="look-preview">
                  <img src={look.image} alt={`${look.name} 미리보기`} />
                </div>
                <span>{look.name}</span>
              </button>
            ))}
          </div>
        </aside>
      </main>

      <section className="product-section">
        <div className="product-header">
          <div>
            <span>SELECTED LOOK</span>
            <strong>{currentLook?.name || "추천 대기 중"}</strong>
          </div>
          <span>{currentLook?.products.length || 0} ITEMS</span>
        </div>

        <div className="product-list">
          {currentLook?.products.map((product) => (
            <div className="product-row" key={product.id}>
              <span>{categoryLabel(product.category)}</span>
              <strong>{product.name}</strong>
              {product.id === currentLook.selectedProductId && (
                <em className="selected-badge">선택</em>
              )}
              <b>₩{(product.price || 0).toLocaleString()}</b>
            </div>
          ))}
        </div>

        <div className="total-price">
          <span>총 가격</span>
          <strong>₩{totalPrice.toLocaleString()}</strong>
        </div>
      </section>

      <button
        type="button"
        className="save-button"
        disabled={!currentLook || saving}
        onClick={handleSave}
      >
        {saving ? "저장 중..." : "저장하기"}
      </button>
    </div>
  );
}

export default App;
