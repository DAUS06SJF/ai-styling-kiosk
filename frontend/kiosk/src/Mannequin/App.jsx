import { useState } from "react";
import "./Mannequin.css";

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

    image: "/images/ai-look-01.png",
  },

  {
    id: 2,
    name: "LOOK 2",

    top: {
      name: "Minimal Shirt",
      price: 159000,
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
      name: "Mini Shoulder Bag",
      price: 139000,
    },

    accessory: {
      name: "Gold Necklace",
      price: 59000,
    },

    image: "/images/ai-look-02.png",
  },

  {
    id: 3,
    name: "LOOK 3",

    top: {
      name: "Vintage Cardigan",
      price: 179000,
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

    image: "/images/ai-look-03.png",
  },

  {
    id: 4,
    name: "LOOK 4",

    top: {
      name: "Luxury Blazer",
      price: 289000,
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

    image: "/images/ai-look-04.png",
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
  {
    category: "가방",
    name: "Leather Mini Bag",
    price: 129000,
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

          <p className="brand-name">
            AI STYLING
          </p>

          <h1>
            AI LIVE MANNEQUIN
          </h1>

        </div>

      </header>


      {/* ================= MAIN ================= */}

      <main className="mannequin-main">


        {/* ================= AI COORDI IMAGE ================= */}

        <section className="mannequin-view">


          {/* AI 생성 이미지 */}

          <div className="mannequin-stage">

            {currentLook.image ? (

              <img
                src={currentLook.image}
                alt={`${currentLook.name} AI 추천 코디`}
                className="ai-look-image"
              />

            ) : (

              <div className="ai-image-placeholder">

                <span>
                  AI STYLING
                </span>

                <p>
                  AI 코디 이미지가 표시됩니다.
                </p>

              </div>

            )}

          </div>


          {/* AI 상태 */}

          <div className="ai-status">

            <span></span>

            <div>

              <strong>
                AI STYLING LIVE
              </strong>

              <p>
                선택하신 상품을 기반으로 코디를 추천했습니다.
              </p>

            </div>

          </div>

        </section>


        {/* ================= LOOK PANEL ================= */}

        <aside className="look-panel">


          <div className="look-title">

            <span>
              AI RECOMMENDATION
            </span>

            <strong>
              LOOK
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


                {/* 기존 디자인 유지 */}

                <div className="look-preview">
                  <img src={look.image}/></div>
                <span>
                  {look.name}
                </span>

              </button>

            ))}

          </div>

        </aside>

      </main>


      {/* ================= SELECTED ITEM ================= */}

      <section className="product-section">

  <div className="product-header">

    <div>
      <span>SELECTED LOOK</span>

      <strong>
        {currentLook.name}
      </strong>
    </div>

    <span>
      5 ITEMS
    </span>

  </div>


  <div className="product-list">


    {/* 상의 */}

    <div className="product-row">

      <span>상의</span>

      <strong>
        {currentLook.top.name}
      </strong>

      {selectedItems.some(
        item => item.name === currentLook.top.name
      ) && (
        <em className="selected-badge">
          선택
        </em>
      )}

      <b>
        ₩{currentLook.top.price.toLocaleString()}
      </b>

    </div>


    {/* 하의 */}

    <div className="product-row">

      <span>하의</span>

      <strong>
        {currentLook.bottom.name}
      </strong>

      {selectedItems.some(
        item => item.name === currentLook.bottom.name
      ) && (
        <em className="selected-badge">
          선택
        </em>
      )}

      <b>
        ₩{currentLook.bottom.price.toLocaleString()}
      </b>

    </div>


    {/* 신발 */}

    <div className="product-row">

      <span>신발</span>

      <strong>
        {currentLook.shoes.name}
      </strong>

      {selectedItems.some(
        item => item.name === currentLook.shoes.name
      ) && (
        <em className="selected-badge">
          선택
        </em>
      )}

      <b>
        ₩{currentLook.shoes.price.toLocaleString()}
      </b>

    </div>


    {/* 가방 */}

    <div className="product-row">

      <span>가방</span>

      <strong>
        {currentLook.bag.name}
      </strong>

      {selectedItems.some(
        item => item.name === currentLook.bag.name
      ) && (
        <em className="selected-badge">
          선택
        </em>
      )}

      <b>
        ₩{currentLook.bag.price.toLocaleString()}
      </b>

    </div>


    {/* 액세서리 */}

    <div className="product-row">

      <span>액세서리</span>

      <strong>
        {currentLook.accessory.name}
      </strong>

      {selectedItems.some(
        item => item.name === currentLook.accessory.name
      ) && (
        <em className="selected-badge">
          선택
        </em>
      )}

      <b>
        ₩{currentLook.accessory.price.toLocaleString()}
      </b>

    </div>


  </div>


  {/* 총 가격 */}

  <div className="total-price">

    <span>
      총 가격
    </span>

    <strong>
      ₩
      {(
        currentLook.top.price +
        currentLook.bottom.price +
        currentLook.shoes.price +
        currentLook.bag.price +
        currentLook.accessory.price
      ).toLocaleString()}
    </strong>

  </div>

</section>


      {/* ================= SAVE ================= */}

      <button className="save-button">

        저장하기

      </button>


    </div>
  );
}


export default App;