import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./start.css";

const languages = [
  { code: "ko", label: "KR" },
  { code: "en", label: "EN" },
  { code: "zh", label: "CN" },
  { code: "ja", label: "JP" },
];

const translations = {
  ko: {
    eyebrow: "AI STYLING EXPERIENCE",
    title: "온라인 마네킹",
    start: "시작하기",
  },
  en: {
    eyebrow: "AI STYLING EXPERIENCE",
    title: "ONLINE MANNEQUIN",
    start: "START",
  },
  zh: {
    eyebrow: "AI STYLING EXPERIENCE",
    title: "在线模特",
    start: "开始",
  },
  ja: {
    eyebrow: "AI STYLING EXPERIENCE",
    title: "オンラインマネキン",
    start: "はじめる",
  },
};

function App() {
  const navigate = useNavigate();
  const [language, setLanguage] = useState("ko");

  const text = translations[language];

  const handleLanguageChange = (code) => {
    setLanguage(code);
    localStorage.setItem("language", code);
  };

  const handleStart = () => {
    localStorage.setItem("language", language);
    navigate("/hanger");
  };

  return (
    <main className="start-screen">
      <div className="language-selector">
        {languages.map((item) => (
          <button
            key={item.code}
            type="button"
            className={`language-option ${
              language === item.code ? "active" : ""
            }`}
            onClick={() => handleLanguageChange(item.code)}
          >
            {item.label}
          </button>
        ))}
      </div>

      <section className="start-content">
        <p className="hero-eyebrow">{text.eyebrow}</p>

        <h1 className="hero-title">{text.title}</h1>

        <div className="hero-divider" />

        <button
          type="button"
          className="start-button"
          onClick={handleStart}
        >
          <span>{text.start}</span>
          <span className="start-arrow">→</span>
        </button>
      </section>
    </main>
  );
}

export default App;