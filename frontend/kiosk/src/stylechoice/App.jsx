import "./stylechoice.css";

function App() {
  return (
    <div className="stylechoice-screen">

      <div className="stylechoice-container">

        <p className="stylechoice-label">
          AI STYLING
        </p>

        <h1>
          원하는 스타일을<br />
          선택해주세요
        </h1>

        <p className="stylechoice-description">
          선택하신 스타일을 기반으로<br />
          AI가 맞춤형 코디를 추천해드립니다.
        </p>

        <div className="style-list">

          <button className="style-card">
            <span className="style-number">01</span>

            <div>
              <strong>MINIMAL</strong>
              <p>깔끔하고 절제된 스타일</p>
            </div>
          </button>

          <button className="style-card">
            <span className="style-number">02</span>

            <div>
              <strong>STREET</strong>
              <p>자유롭고 개성 있는 스타일</p>
            </div>
          </button>

          <button className="style-card">
            <span className="style-number">03</span>

            <div>
              <strong>LUXURY</strong>
              <p>고급스럽고 세련된 스타일</p>
            </div>
          </button>

          <button className="style-card">
            <span className="style-number">04</span>

            <div>
              <strong>VINTAGE</strong>
              <p>클래식하고 감성적인 스타일</p>
            </div>
          </button>

          <button className="style-card">
            <span className="style-number">05</span>

            <div>
              <strong>Y2K</strong>
              <p>트렌디하고 개성 있는 스타일</p>
            </div>
          </button>

        </div>

      </div>

    </div>
  );
}

export default App;