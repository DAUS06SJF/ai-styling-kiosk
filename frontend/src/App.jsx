import { Routes, Route, Link } from "react-router-dom";

import Start from "../kiosk/src/start/App.jsx";
import Hanger from "../kiosk/src/hanger/App.jsx";
import StyleChoice from "../kiosk/src/stylechoice/App.jsx";
import AICodi from "../kiosk/src/AI-codi/App.jsx";
import Mannequin from "../kiosk/src/Mannequin/App.jsx";
import QRShare from "../kiosk/src/QR-share/App.jsx";


function Home() {
  return (
    <div>
      <h1>AI STYLING KIOSK</h1>

      <div>
        <Link to="/start">START</Link>
      </div>

      <div>
        <Link to="/hanger">HANGER</Link>
      </div>

      <div>
        <Link to="/stylechoice">STYLE CHOICE</Link>
      </div>

      <div>
        <Link to="/ai-codi">AI-CODI</Link>
      </div>

      <div>
        <Link to="/mannequin">MANNEQUIN</Link>
      </div>

      <div>
        <Link to="/qr-share">QR-SHARE</Link>
      </div>
    </div>
  );
}


function App() {
  return (
    <Routes>

      <Route path="/" element={<Home />} />

      <Route path="/start" element={<Start />} />

      <Route path="/hanger" element={<Hanger />} />

      <Route path="/stylechoice" element={<StyleChoice />} />

      <Route path="/ai-codi" element={<AICodi />} />

      <Route path="/mannequin" element={<Mannequin />} />

      <Route path="/qr-share" element={<QRShare />} />

    </Routes>
  );
}


export default App;