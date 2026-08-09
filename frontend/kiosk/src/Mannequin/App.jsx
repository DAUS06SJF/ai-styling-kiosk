import "./Mannequin.css";

function App() {
  return (
    <div className="mannequin-screen">

      {/* 제목 */}
      <h1>AI LIVE MANNEQUIN</h1>

      {/* 마네킹 */}
      <div className="mannequin-box">
        <div className="mannequin">
          <div className="head"></div>

          <div className="body"></div>

          <div className="leg left-leg"></div>

          <div className="leg right-leg"></div>
        </div>
      </div>

      {/* LOOK */}
      <div className="looks">
        <button>LOOK 1</button>
        <button>LOOK 2</button>
        <button>LOOK 3</button>
        <button>LOOK 4</button>
      </div>

      {/* 상품 정보 */}
      <div className="items">

        <div>
          <span>가방</span>
          <strong>Leather Mini Bag</strong>
        </div>

        <div>
          <span>상의</span>
          <strong>Oversized Jacket</strong>
        </div>

        <div>
          <span>하의</span>
          <strong>Wide Trousers</strong>
        </div>

        <div>
          <span>신발</span>
          <strong>Classic Sneakers</strong>
        </div>

        <div>
          <span>액세서리</span>
          <strong>Silver Necklace</strong>
        </div>

        <div>
          <span>가격</span>
          <strong>₩428,000</strong>
        </div>

      </div>

      {/* 저장 */}
      <button className="save-button">
        ♡ 저장하기
      </button>

    </div>
  );
}

export default App;