const express = require('express');
const http = require('http');
const { Server } = require('socket.io');

const app = express();
const server = http.createServer(app);
const io = new Server(server, {
  cors: { origin: "*" } // 모든 기기 접속 허용
});

app.use(express.json());

// 1. 센서 신호를 받는 API (파이썬에서 호출)
app.post('/api/trigger', (req, res) => {
  const { event } = req.body;
  
  if (event === 'CHECK') {
    console.log('⚡ 센서 감지! 모든 클라이언트에 페이지 열기 명령 전송');
    // 연결된 모든 브라우저에 'open-url' 이벤트 전송
    io.emit('open-url', { url: 'https://www.google.com' });
    return res.json({ success: true, message: 'Signal sent' });
  }
  
  res.status(400).json({ success: false });
});

// 2. 웹소켓 클라이언트 연결 처리
io.on('connection', (socket) => {
  console.log('📱 새로운 기기 연결됨 ID:', socket.id);
  
  socket.on('disconnect', () => {
    console.log('❌ 기기 연결 해제됨:', socket.id);
  });
});

const PORT = 3000;
server.listen(PORT, () => {
  console.log(`🚀 서버 실행 중: http://localhost:${PORT}`);
});