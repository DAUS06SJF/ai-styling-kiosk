import { useEffect, useState } from "react";
import { useLocation } from "react-router-dom";

const QR_API_URL = import.meta.env.VITE_QR_API_URL
  || "https://qr-site-e7tc.vercel.app/api/prepare-qr";

function App() {
  const location = useLocation();
  const [qrImage, setQrImage] = useState("");
  const [statusText, setStatusText] = useState("QR 코드를 준비하고 있습니다...");
  const [retryCount, setRetryCount] = useState(0);
  const [loading, setLoading] = useState(true);

  const selectedPhotoUrl = location.state?.kodiSelected
    || localStorage.getItem("selectedKodi")
    || "";
  const recommendationId = location.state?.recommendationId
    || localStorage.getItem("selectedKodiId")
    || "";

  useEffect(() => {
    if (!selectedPhotoUrl) {
      return undefined;
    }

    const controller = new AbortController();

    const prepareQr = async () => {
      setLoading(true);
      setQrImage("");
      setStatusText("사진을 저장하고 QR 코드를 만들고 있습니다...");

      try {
        const response = await fetch(QR_API_URL, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ photoUrl: selectedPhotoUrl, recommendationId }),
          signal: controller.signal,
        });
        const result = await response.json().catch(() => null);

        if (!response.ok || !result?.success || !result?.data?.qrCodeImage) {
          throw new Error(result?.message || "QR 서버가 요청을 처리하지 못했습니다.");
        }

        setQrImage(result.data.qrCodeImage);
        setStatusText("휴대폰으로 스캔하여 사진을 저장하세요.");
      } catch (error) {
        if (error?.name === "AbortError") return;
        console.error("QR 생성 오류:", error);
        setStatusText(error.message || "QR 서버에 연결하지 못했습니다.");
      } finally {
        if (!controller.signal.aborted) setLoading(false);
      }
    };

    prepareQr();
    return () => controller.abort();
  }, [selectedPhotoUrl, recommendationId, retryCount]);

  return (
    <div style={{
      minHeight: "100vh",
      padding: "40px 20px",
      fontFamily: "sans-serif",
      background: "#f7f5ef",
      color: "#4a3b32",
      textAlign: "center",
    }}>
      <h1 style={{ margin: "0 0 12px", fontSize: "32px" }}>AI LIVE MANNEQUIN</h1>
      <p style={{ margin: "0 0 28px", fontSize: "16px" }}>
        {selectedPhotoUrl
          ? statusText
          : "선택된 코디 사진을 찾을 수 없습니다. 이전 화면에서 다시 선택해 주세요."}
      </p>

      {qrImage && (
        <img
          src={qrImage}
          alt="사진 저장 QR 코드"
          style={{
            width: "260px",
            height: "260px",
            padding: "12px",
            borderRadius: "18px",
            background: "#fff",
            boxShadow: "0 8px 30px rgba(74, 59, 50, 0.12)",
          }}
        />
      )}

      {!loading && !qrImage && selectedPhotoUrl && (
        <div>
          <button
            type="button"
            onClick={() => setRetryCount((count) => count + 1)}
            style={{
              padding: "12px 22px",
              border: 0,
              borderRadius: "12px",
              background: "#6b5344",
              color: "#fff",
              fontSize: "15px",
              cursor: "pointer",
            }}
          >
            다시 시도
          </button>
        </div>
      )}
    </div>
  );
}

export default App;
