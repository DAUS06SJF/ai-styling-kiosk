import "./ai-codi.css";

function App() {
  return (
    <div className="ai-codi-screen">

      <div className="ai-codi-content">

        <p className="ai-codi-label">
          AI STYLING
        </p>

        <h1>
          당신의 스타일을<br />
          선택해주세요
        </h1>

        <p className="ai-codi-description">
          원하는 스타일을 선택하면<br />
          AI가 맞춤형 코디를 추천합니다.
        </p>


        <div className="style-list">

          <button className="style-card">
            <strong>MINIMAL</strong>
            <span>깔끔하고 절제된 스타일</span>
          </button>

          <button className="style-card">
            <strong>STREET</strong>
            <span>자유롭고 개성 있는 스타일</span>
          </button>

          <button className="style-card">
            <strong>LUXURY</strong>
            <span>고급스럽고 세련된 스타일</span>
          </button>

          <button className="style-card">
            <strong>VINTAGE</strong>
            <span>클래식하고 감성적인 스타일</span>
          </button>

          <button className="style-card">
            <strong>Y2K</strong>
            <span>트렌디하고 개성 있는 스타일</span>
          </button>

        </div>

      </div>

    </div>
  );
}

export default App;