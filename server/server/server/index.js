const express = require('express');
const http = require('http');
const { Server } = require('socket.io');

const app = express();
const server = http.createServer(app);
const io = new Server(server, { cors: { 
    origin: "https://daus06sjf-ai-styling-khiosk-eosin.vercel.app", // 내 Vercel 주소 허용
    methods: ["GET", "POST"]
  } 
});

app.use(express.json());

// 파이썬이 "센서 감지됐어!"하고 찌르는 곳
app.post('/api/trigger', (req, res) => {
  if (req.body.event === 'CHECK') {
    console.log('⚡ 센서 감지! 리액트 화면들에 페이지 이동 명령 전송');
    io.emit('open-url'); // 리액트로 'open-url' 방송 쏘기!
    return res.json({ success: true });
  }
});

server.listen(5000, () => {
  console.log(`🚀 서버 실행 중: http://localhost:5000`);
});

