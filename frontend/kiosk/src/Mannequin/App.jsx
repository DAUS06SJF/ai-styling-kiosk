import { useState } from "react";
import "./Mannequin.css";

import bodyImage from "./image/body.png";

const looks = [
  {
    id: 1,
    name: "LOOK 1",
    top: "Oversized Jacket",
    bottom: "Wide Trousers",
    shoes: "Classic Sneakers",
    bag: "Leather Mini Bag",
    accessory: "Silver Necklace",
    price: "₩428,000",
  },
  {
    id: 2,
    name: "LOOK 2",
    top: "Minimal Shirt",
    bottom: "Straight Pants",
    shoes: "Leather Loafer",
    bag: "Mini Shoulder Bag",
    accessory: "Gold Necklace",
    price: "₩398,000",
  },
  {
    id: 3,
    name: "LOOK 3",
    top: "Vintage Cardigan",
    bottom: "Denim Jeans",
    shoes: "Retro Sneakers",
    bag: "Vintage Bag",
    accessory: "Silver Ring",
    price: "₩365,000",
  },
  {
    id: 4,
    name: "LOOK 4",
    top: "Luxury Blazer",
    bottom: "Tailored Pants",
    shoes: "Pointed Shoes",
    bag: "Luxury Bag",
    accessory: "Gold Earrings",
    price: "₩612,000",
  },
];

function App() {
  const [selectedLook, setSelectedLook] = useState(1);

  const currentLook = looks.find(
    (look) => look.id === selectedLook
  );

  return (
    <div className="mannequin-screen">

      {/* ================= HEADER ================= */}

      <header className="mannequin-header">

        <div className="brand-area">
          <p className="brand-name">AI STYLING</p>

          <h1>AI LIVE MANNEQUIN</h1>
        </div>

        <div className="live-status">
          <span></span>
          LIVE
        </div>

      </header>


      {/* ================= MAIN ================= */}

      <main className="mannequin-main">

        {/* 마네킹 영역 */}

        <section className="mannequin-view">

          <div className="view-label">
            REAL-TIME AI STYLING
          </div>

          <div className="mannequin-stage">

            <div className="mannequin-character">

              <img
                src={bodyImage}
                alt="AI Fashion Mannequin"
                className="mannequin-body-image"
              />

            </div>

            <div className="character-shadow"></div>

          </div>


          {/* AI 상태 */}

          <div className="ai-status">

            <span></span>

            <div>
              <strong>AI STYLING LIVE</strong>

              <p>
                현재 선택한 코디를 적용하고 있습니다.
              </p>
            </div>

          </div>

        </section>


        {/* ================= LOOK ================= */}

        <aside className="look-panel">

          <div className="look-title">

            <span>AI RECOMMENDATION</span>

            <strong>LOOK</strong>

          </div>


          <div className="look-list">

            {looks.map((look) => (

              <button
                key={look.id}
                className={`look-card ${
                  selectedLook === look.id ? "active" : ""
                }`}
                onClick={() => setSelectedLook(look.id)}
              >

                <div className="look-number">
                  0{look.id}
                </div>

                <div className="look-preview">

                  <div className="mini-head"></div>

                  <div className="mini-body"></div>

                </div>

                <span>{look.name}</span>

              </button>

            ))}

          </div>

        </aside>

      </main>


      {/* ================= PRODUCT ================= */}

      <section className="product-section">

        <div className="product-header">

          <div>
            <span>SELECTED LOOK</span>

            <strong>{currentLook.name}</strong>
          </div>

          <span>5 ITEMS</span>

        </div>


        <div className="product-list">

          <div className="product-row">
            <span>가방</span>
            <strong>{currentLook.bag}</strong>
          </div>

          <div className="product-row">
            <span>상의</span>
            <strong>{currentLook.top}</strong>
          </div>

          <div className="product-row">
            <span>하의</span>
            <strong>{currentLook.bottom}</strong>
          </div>

          <div className="product-row">
            <span>신발</span>
            <strong>{currentLook.shoes}</strong>
          </div>

          <div className="product-row">
            <span>액세서리</span>
            <strong>{currentLook.accessory}</strong>
          </div>

        </div>


        <div className="total-price">

          <span>총 가격</span>

          <strong>{currentLook.price}</strong>

        </div>

      </section>


      {/* ================= SAVE ================= */}

      <button className="save-button">

        <span>♡</span>

        저장하기

      </button>

    </div>
  );
}

export default App;