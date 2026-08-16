import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import {
  getLatestStylingRecommendations,
  resolveBackendAssetUrl,
  selectStylingRecommendation,
  WEB_SOCKET_URL,
} from "../shared/stylingApi";
import "./Mannequin.css";


import look1 from "./images/ai-look-01.png";


const MAX_LOOKS = 4;

const moodAliases = {
  MINIMAL: ["MINIMAL", "미니멀"],
  STREET: ["STREET", "스트리트"],
  LUXURY: ["LUXURY", "럭셔리"],
  VINTAGE: ["VINTAGE", "빈티지"],
  Y2K: ["Y2K"],
};

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
    image: resolveBackendAssetUrl(recommendation.kodi),
    kodiSelected: recommendation.kodiSelected,
    selectedProductId: recommendation.selectedProduct?.id,
    products: uniqueProducts,
  };
}

function sameMood(responseMood, selectedMood) {
  const aliases = moodAliases[selectedMood] || [selectedMood];
  return aliases.some((alias) => alias.toLowerCase() === (responseMood || "").toLowerCase());
}

function mergeLooks(...lookGroups) {
  const merged = lookGroups.flat().filter(Boolean);
  return merged.filter(
    (look, index) => merged.findIndex((candidate) => candidate.id === look.id) === index,
  ).slice(0, MAX_LOOKS);
}

const translations = {
  ko: {
    brand: "AI STYLING",
    title: "LIFE STYLING",

    recommendation: "AI RECOMMENDATION",
    look: "LOOK",

    aiLive: "AI STYLING LIVE",
    aiDescription:
      "선택하신 상품을 기반으로 코디를 추천했습니다.",

    aiImageAlt: "AI 추천 코디",
    aiImageTitle: "AI STYLING",
    aiImageDescription: "AI 코디 이미지가 표시됩니다.",

    selectedLook: "SELECTED LOOK",
    items: "ITEMS",

    top: "상의",
    bottom: "하의",
    shoes: "신발",
    bag: "가방",
    accessory: "액세서리",

    selected: "선택",
    total: "총 가격",

    save: "저장하기",
    saving: "저장 중...",

    saveSuccess: "코디가 저장되었습니다.",
    saveFail: "코디 저장에 실패했습니다.",
  },

  en: {
    brand: "AI STYLING",
    title: "LIFE STYLING",

    recommendation: "AI RECOMMENDATION",
    look: "LOOK",

    aiLive: "AI STYLING LIVE",
    aiDescription:
      "We created a styling recommendation based on your selected item.",

    aiImageAlt: "AI styling recommendation",
    aiImageTitle: "AI STYLING",
    aiImageDescription:
      "Your AI styling image will appear here.",

    selectedLook: "SELECTED LOOK",
    items: "ITEMS",

    top: "TOP",
    bottom: "BOTTOM",
    shoes: "SHOES",
    bag: "BAG",
    accessory: "ACCESSORY",

    selected: "SELECTED",
    total: "TOTAL",

    save: "SAVE",
    saving: "SAVING...",

    saveSuccess: "Your styling has been saved.",
    saveFail: "Failed to save the styling.",
  },

  zh: {
    brand: "AI STYLING",
    title: "LIFE STYLING",

    recommendation: "AI RECOMMENDATION",
    look: "LOOK",

    aiLive: "AI STYLING LIVE",
    aiDescription:
      "根据您选择的商品，我们为您推荐了搭配。",

    aiImageAlt: "AI 推荐搭配",
    aiImageTitle: "AI STYLING",
    aiImageDescription:
      "AI 搭配图片将在此显示。",

    selectedLook: "SELECTED LOOK",
    items: "ITEMS",

    top: "上装",
    bottom: "下装",
    shoes: "鞋子",
    bag: "包袋",
    accessory: "配饰",

    selected: "已选择",
    total: "总价",

    save: "保存",
    saving: "保存中...",

    saveSuccess: "搭配已保存。",
    saveFail: "搭配保存失败。",
  },

  ja: {
    brand: "AI STYLING",
    title: "LIFE STYLING",

    recommendation: "AI RECOMMENDATION",
    look: "LOOK",

    aiLive: "AI STYLING LIVE",
    aiDescription:
      "選択した商品をもとにコーディネートを提案しました。",

    aiImageAlt: "AIおすすめコーディネート",
    aiImageTitle: "AI STYLING",
    aiImageDescription:
      "AIコーディネート画像が表示されます。",

    selectedLook: "SELECTED LOOK",
    items: "ITEMS",

    top: "トップス",
    bottom: "ボトムス",
    shoes: "シューズ",
    bag: "バッグ",
    accessory: "アクセサリー",

    selected: "選択済み",
    total: "合計",

    save: "保存する",
    saving: "保存中...",

    saveSuccess: "コーディネートを保存しました。",
    saveFail: "コーディネートの保存に失敗しました。",
  },
};

function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const selectedMood = localStorage.getItem("selectedStyle") || "MINIMAL";
  const routeRecommendation = location.state?.recommendation;
  const routeLook = routeRecommendation ? toLook(routeRecommendation) : null;
  const [looks, setLooks] = useState(() => routeLook ? [routeLook] : []);
  const [selectedLookId, setSelectedLookId] = useState(routeLook?.id ?? null);
  const [connectionState, setConnectionState] = useState("loading");
  const [errorMessage, setErrorMessage] = useState("");
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    const abortController = new AbortController();

    async function loadLooks() {
      try {
        const recommendations = await getLatestStylingRecommendations(
          selectedMood,
          MAX_LOOKS,
          abortController.signal,
        );
        const loadedLooks = (recommendations || []).map(toLook);
        setLooks((previous) => mergeLooks(previous, loadedLooks));
        setSelectedLookId((previous) => previous ?? loadedLooks[0]?.id ?? null);
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
          setLooks((previous) => mergeLooks([newLook], previous));
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
      const selectedRecommendation = await selectStylingRecommendation(currentLook.id);
      localStorage.setItem("selectedKodi", selectedRecommendation.kodiSelected);
      localStorage.setItem("selectedKodiId", String(selectedRecommendation.id));
      navigate("/qr-share", {
        state: {
          kodiSelected: selectedRecommendation.kodiSelected,
          recommendationId: selectedRecommendation.id,
        },
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
                alt={`${currentLook.name} ${text.aiImageAlt}`}
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
