import { Routes, Route, Navigate } from "react-router-dom";

import Start from "../kiosk/src/start/App.jsx";
import Hanger from "../kiosk/src/hanger/App.jsx";
import StyleChoice from "../kiosk/src/stylechoice/App.jsx";
import AICodi from "../kiosk/src/AI-codi/App.jsx";
import Mannequin from "../kiosk/src/Mannequin/App.jsx";
import QRShare from "../kiosk/src/QR-share/App.jsx";

function App() {
  return (
    <Routes>
      {/* 첫 접속 시 시작 화면으로 이동 */}
      <Route path="/" element={<Navigate to="/start" replace />} />

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