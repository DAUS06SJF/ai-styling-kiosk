const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors'); // 1. cors 불러오기

const app = express();
const server = http.createServer(app);

// 2. 익스프레스(Express)에 CORS 적용 (모든 곳에서 접근 허용)
app.use(cors());
app.use(express.json());

// 3. 소켓(Socket.io)에도 CORS 전면 허용 (*) 적용
const io = new Server(server, {
    cors: {
        origin: "*", // 모든 주소(로컬, Vercel 등) 허용
        methods: ["GET", "POST"]
    }
});

// 파이썬이 "센서 감지됐어!" 하고 찌르는 곳
app.post('/api/trigger', (req, res) => {
    if (req.body.event === 'CHECK') {
        console.log('⚡ 센서 감지! 리액트 화면들에 페이지 이동 명령 전송');
        io.emit('open-url'); // 리액트로 'open-url' 방송 쏘기!
        return res.json({ success: true });
    }
});

const PORT = process.env.PORT || 5000;
server.listen(PORT, () => {
    console.log(`🚀 서버 실행 중: http://localhost:${PORT}`);
});