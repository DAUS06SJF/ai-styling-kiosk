import React, { useState, useEffect } from 'react';
import { useLocation } from "react-router-dom";

function App() {
  const [qrImage, setQrImage] = useState('');
  const [statusText, setStatusText] = useState('QR 연결 대기 중...');
  const [currentPhotoId, setCurrentPhotoId] = useState(null);

  // ⚠️ 백엔드 API 주소 (동일한 공유기/네트워크 환경 기준)
  const API_URL = 'http://172.30.1.26:3000/api/latest-qr';

  useEffect(() => {
    const fetchLatestQR = async () => {
      try {
        const response = await fetch(API_URL);
        const result = await response.json();

        if (result.success && result.data) {
          // 새로운 photoId가 들어왔을 때만 상태 업데이트
          if (currentPhotoId !== result.data.photoId) {
            setCurrentPhotoId(result.data.photoId);
            setQrImage(result.data.qrCodeImage);
            setStatusText('📱 스캔하여 사진 저장');
          }
        } else {
          setStatusText('아직 생성된 QR이 없습니다.');
        }
      } catch (error) {
        console.error('QR 통신 오류:', error);
        setStatusText('서버 연결 실패');
      }
    };

    // 처음 켜졌을 때 1회 즉시 실행
    fetchLatestQR();

    // 2초마다 백엔드 서버에서 최신 QR 불러오기
    const interval = setInterval(fetchLatestQR, 2000);

    // 컴포넌트 종료 시 타이머 해제
    return () => clearInterval(interval);
  }, [currentPhotoId]);

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      {/* 헤더 부분 */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
        <h1 style={{ margin: 0, fontSize: '28px', color: '#333' }}>
          AI LIVE MANNEQUIN
        </h1>

        {/* QR 코드 표시 영역 */}
        {qrImage && (
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <img
              src={qrImage}
              alt="최신 QR 코드"
              style={{
                width: '80px',
                height: '80px',
                borderRadius: '8px',
                border: '1px solid #ddd',
                boxShadow: '0 2px 5px rgba(0,0,0,0.1)',
              }}
            />
            <span style={{ fontSize: '13px', color: '#555', fontWeight: 'bold' }}>
              {statusText}
            </span>
          </div>
        )}
      </div>

      {/* 메인 콘텐츠 들어갈 영역 */}
      <div style={{ marginTop: '20px' }}>
        {/* 상대방이 여기에 기존 화면 요소(룩북, 의류 목록 등)를 배치하면 됩니다. */}
      </div>
    </div>
  );
}
export default App;