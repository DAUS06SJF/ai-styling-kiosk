import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./Mannequin.css";


import look1 from "./images/ai-look-01.png";


const looks = [
  {
    id: 1,
    name: "LOOK 1",

    top: {
      name: "MCM Logo T-Shirt",
      price: 189000,
    },

    bottom: {
      name: "Wide Trousers",
      price: 129000,
    },

    shoes: {
      name: "Classic Sneakers",
      price: 69000,
    },

    bag: {
      name: "Leather Mini Bag",
      price: 129000,
    },

    accessory: {
      name: "Silver Necklace",
      price: 49000,
    },

    image: look1,
  },

  {
    id: 2,
    name: "LOOK 2",

    top: {
      name: "MCM Logo T-Shirt",
      price: 189000,
    },

    bottom: {
      name: "Straight Pants",
      price: 119000,
    },

    shoes: {
      name: "Leather Loafer",
      price: 99000,
    },

    bag: {
      name: "Leather Mini Bag",
      price: 129000,
    },

    accessory: {
      name: "Gold Necklace",
      price: 59000,
    },

    image: look1,
  },

  {
    id: 3,
    name: "LOOK 3",

    top: {
      name: "MCM Logo T-Shirt",
      price: 189000,
    },

    bottom: {
      name: "Denim Jeans",
      price: 119000,
    },

    shoes: {
      name: "Retro Sneakers",
      price: 89000,
    },

    bag: {
      name: "Vintage Bag",
      price: 149000,
    },

    accessory: {
      name: "Silver Ring",
      price: 39000,
    },

    image: look1,
  },

  {
    id: 4,
    name: "LOOK 4",

    top: {
      name: "MCM Logo T-Shirt",
      price: 189000,
    },

    bottom: {
      name: "Tailored Pants",
      price: 159000,
    },

    shoes: {
      name: "Pointed Shoes",
      price: 139000,
    },

    bag: {
      name: "Luxury Bag",
      price: 199000,
    },

    accessory: {
      name: "Gold Earrings",
      price: 79000,
    },

    image: look1,
  },
];


// 행거에서 고객이 직접 선택한 상품
// 나중에 RFID/NFC + DB 데이터로 연결
const selectedItems = [
  {
    category: "상의",
    name: "MCM Logo T-Shirt",
    price: 189000,
  },
];


// ================================
// 다국어
// ================================

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

  const savedLanguage =
    localStorage.getItem("language") || "ko";

  const text =
    translations[savedLanguage] || translations.ko;

  const [selectedLook, setSelectedLook] = useState(1);

  const [isSaving, setIsSaving] = useState(false);

  const [kodiImageUrl, setKodiImageUrl] = useState("");


  // =========================================
  // 코디 저장
  // =========================================

  const handleSave = async () => {
  try {
    setIsSaving(true);

    // 현재 선택한 코디 ID를 백엔드에 전달
    const imageUrl = await selectKodi(selectedLook);

    console.log("선택된 코디 이미지:", imageUrl);

    setKodiImageUrl(imageUrl);

    // QR 화면으로 이동하면서 AI 코디 이미지 주소 전달
    navigate("/qr-share", {
      state: {
        kodiImageUrl: imageUrl,
        lookId: selectedLook,
      },
    });

  } catch (error) {
    console.error("코디 저장 실패:", error);
    alert("코디 저장에 실패했습니다.");
  } finally {
    setIsSaving(false);
  }
};


  const currentLook = looks.find(
    (look) => look.id === selectedLook
  );


  // =========================================
  // 상품 선택 여부
  // =========================================

  const isSelected = (itemName) => {

    return selectedItems.some(
      (item) => item.name === itemName
    );

  };


  // =========================================
  // 총 가격
  // =========================================

  const totalPrice =
    currentLook.top.price +
    currentLook.bottom.price +
    currentLook.shoes.price +
    currentLook.bag.price +
    currentLook.accessory.price;


  return (

    <div className="mannequin-screen">


      {/* ================= HEADER ================= */}

      <header className="mannequin-header">

        <div className="brand-area">

          <p className="brand-name">
            {text.brand}
          </p>

          <h1>
            {text.title}
          </h1>

        </div>

      </header>


      {/* ================= MAIN ================= */}

      <main className="mannequin-main">


        {/* ================= AI COORDI IMAGE ================= */}

        <section className="mannequin-view">


          <div className="mannequin-stage">

            {currentLook.image ? (

              <img
                src={currentLook.image}
                alt={`${currentLook.name} ${text.aiImageAlt}`}
                className="ai-look-image"
              />

            ) : (

              <div className="ai-image-placeholder">

                <span>
                  {text.aiImageTitle}
                </span>

                <p>
                  {text.aiImageDescription}
                </p>

              </div>

            )}

          </div>


          {/* ================= AI STATUS ================= */}

          <div className="ai-status">

            <span></span>

            <div>

              <strong>
                {text.aiLive}
              </strong>

              <p>
                {text.aiDescription}
              </p>

            </div>

          </div>

        </section>


        {/* ================= LOOK PANEL ================= */}

        <aside className="look-panel">


          <div className="look-title">

            <span>
              {text.recommendation}
            </span>

            <strong>
              {text.look}
            </strong>

          </div>


          <div className="look-list">

            {looks.map((look) => (

              <button
                key={look.id}
                className={`look-card ${
                  selectedLook === look.id
                    ? "active"
                    : ""
                }`}
                onClick={() =>
                  setSelectedLook(look.id)
                }
              >

                <div className="look-number">
                  0{look.id}
                </div>


                <div className="look-preview">

                  <img
                    src={look.image}
                    alt={look.name}
                  />

                </div>


                <span>
                  {look.name}
                </span>

              </button>

            ))}

          </div>

        </aside>

      </main>


      {/* ================= PRODUCT INFO ================= */}

      <section className="product-section">


        <div className="product-header">

          <div>

            <span>
              {text.selectedLook}
            </span>

            <strong>
              {currentLook.name}
            </strong>

          </div>

          <span>
            5 {text.items}
          </span>

        </div>


        <div className="product-list">


          {/* ================= 상의 ================= */}

          <div className="product-row">

            <span>
              {text.top}
            </span>

            <strong>
              {currentLook.top.name}
            </strong>

            <b>
              ₩
              {currentLook.top.price.toLocaleString()}
            </b>

            {isSelected(currentLook.top.name) && (

              <em className="selected-badge">
                {text.selected}
              </em>

            )}

          </div>


          {/* ================= 하의 ================= */}

          <div className="product-row">

            <span>
              {text.bottom}
            </span>

            <strong>
              {currentLook.bottom.name}
            </strong>

            <b>
              ₩
              {currentLook.bottom.price.toLocaleString()}
            </b>

            {isSelected(currentLook.bottom.name) && (

              <em className="selected-badge">
                {text.selected}
              </em>

            )}

          </div>


          {/* ================= 신발 ================= */}

          <div className="product-row">

            <span>
              {text.shoes}
            </span>

            <strong>
              {currentLook.shoes.name}
            </strong>

            <b>
              ₩
              {currentLook.shoes.price.toLocaleString()}
            </b>

            {isSelected(currentLook.shoes.name) && (

              <em className="selected-badge">
                {text.selected}
              </em>

            )}

          </div>


          {/* ================= 가방 ================= */}

          <div className="product-row">

            <span>
              {text.bag}
            </span>

            <strong>
              {currentLook.bag.name}
            </strong>

            <b>
              ₩
              {currentLook.bag.price.toLocaleString()}
            </b>

            {isSelected(currentLook.bag.name) && (

              <em className="selected-badge">
                {text.selected}
              </em>

            )}

          </div>


          {/* ================= 액세서리 ================= */}

          <div className="product-row">

            <span>
              {text.accessory}
            </span>

            <strong>
              {currentLook.accessory.name}
            </strong>

            <b>
              ₩
              {currentLook.accessory.price.toLocaleString()}
            </b>

            {isSelected(currentLook.accessory.name) && (

              <em className="selected-badge">
                {text.selected}
              </em>

            )}

          </div>


        </div>


        {/* ================= TOTAL ================= */}

        <div className="total-price">

          <span>
            {text.total}
          </span>

          <strong>
            ₩{totalPrice.toLocaleString()}
          </strong>

        </div>

      </section>


      {/* ================= SAVE ================= */}

      <button
        className="save-button"
        onClick={handleSave}
        disabled={isSaving}
      >

        {isSaving
          ? text.saving
          : text.save}

      </button>


    </div>

  );

}


export default App;