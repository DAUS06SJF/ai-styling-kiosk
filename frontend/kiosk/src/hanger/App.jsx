import "./hanger.css";

function App() {
  return (
    <div className="hanger-screen">

      <div className="hanger-content">

        <p className="hanger-label">
          AI STYLING KIOSK
        </p>

        <h1>
          옷을 선택해주세요
        </h1>

        <p className="hanger-description">
          행거에서 원하는 옷을 꺼내주세요.
        </p>

        {/* 센서 영역 */}
        <div className="hanger-sensor">

          <div className="sensor-circle">
            <div className="sensor-inner"></div>
          </div>

          <p>
            WAITING FOR ITEM
          </p>

        </div>

        <div className="hanger-status">
          <span></span>
          RFID SENSOR ACTIVE
        </div>

      </div>

    </div>
  );
}

export default App;