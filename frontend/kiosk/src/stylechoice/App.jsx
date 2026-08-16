import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./stylechoice.css";

const styleOptions = [
  {
    id: "MINIMAL",
    number: "01",
  },
  {
    id: "STREET",
    number: "02",
  },
  {
    id: "LUXURY",
    number: "03",
  },
  {
    id: "VINTAGE",
    number: "04",
  },
  {
    id: "Y2K",
    number: "05",
  },
];

const translations = {
  ko: {
    eyebrow: "STYLE SELECTION",
    title: "원하는 스타일을 선택해주세요",
    description: "선택하신 제품을 중심으로 AI가 코디를 제안합니다.",

    styles: {
      MINIMAL: {
        title: "미니멀",
        description: "절제된 색감과 간결한 실루엣",
      },
      STREET: {
        title: "스트리트",
        description: "자유롭고 개성 있는 스타일",
      },
      LUXURY: {
        title: "럭셔리",
        description: "정제된 디테일과 고급스러운 분위기",
      },
      VINTAGE: {
        title: "빈티지",
        description: "시간의 감성을 담은 클래식 스타일",
      },
      Y2K: {
        title: "Y2K",
        description: "대담하고 감각적인 2000년대 무드",
      },
    },

    next: "AI 스타일링 시작",
  },

  en: {
    eyebrow: "STYLE SELECTION",
    title: "CHOOSE YOUR STYLE",
    description:
      "AI will create a styling recommendation around your selected item.",

    styles: {
      MINIMAL: {
        title: "MINIMAL",
        description: "Refined colors and clean silhouettes",
      },
      STREET: {
        title: "STREET",
        description: "Expressive and contemporary street styling",
      },
      LUXURY: {
        title: "LUXURY",
        description: "Sophisticated details and elegant styling",
      },
      VINTAGE: {
        title: "VINTAGE",
        description: "Classic styling inspired by timeless aesthetics",
      },
      Y2K: {
        title: "Y2K",
        description: "Bold styling inspired by the early 2000s",
      },
    },

    next: "START AI STYLING",
  },

  zh: {
    eyebrow: "STYLE SELECTION",
    title: "请选择您喜欢的风格",
    description: "AI将以您选择的商品为中心推荐搭配。",

    styles: {
      MINIMAL: {
        title: "极简",
        description: "简洁的轮廓与克制的色彩",
      },
      STREET: {
        title: "街头",
        description: "自由而富有个性的现代风格",
      },
      LUXURY: {
        title: "奢华",
        description: "精致细节与优雅氛围",
      },
      VINTAGE: {
        title: "复古",
        description: "具有经典年代感的风格",
      },
      Y2K: {
        title: "Y2K",
        description: "大胆而独特的千禧年代风格",
      },
    },

    next: "开始AI搭配",
  },

  ja: {
    eyebrow: "STYLE SELECTION",
    title: "お好みのスタイルを選択してください",
    description:
      "選択したアイテムを中心にAIがコーディネートを提案します。",

    styles: {
      MINIMAL: {
        title: "ミニマル",
        description: "洗練された色とシンプルなシルエット",
      },
      STREET: {
        title: "ストリート",
        description: "自由で個性的な現代的スタイル",
      },
      LUXURY: {
        title: "ラグジュアリー",
        description: "上品なディテールと高級感のあるスタイル",
      },
      VINTAGE: {
        title: "ヴィンテージ",
        description: "時代を超えたクラシックなスタイル",
      },
      Y2K: {
        title: "Y2K",
        description: "2000年代をイメージした大胆なスタイル",
      },
    },

    next: "AIスタイリングを開始",
  },
};

function App() {
  const navigate = useNavigate();

  const savedLanguage = localStorage.getItem("language") || "ko";
  const text = translations[savedLanguage] || translations.ko;

  const [selectedStyle, setSelectedStyle] = useState(null);

  const handleStyleSelect = (styleId) => {
    setSelectedStyle(styleId);
  };

  const handleNext = () => {
    if (!selectedStyle) {
      return;
    }

    // AI 코디 생성 화면과 마네킹 화면에서 사용
    localStorage.setItem("selectedStyle", selectedStyle);

    // 스타일 선택 후 바로 마네킹 화면으로 이동
    navigate("/mannequin");
  };

  return (
    <main className="style-screen">
      <section className="style-content">
        <header className="style-header">
          <p className="style-eyebrow">{text.eyebrow}</p>

          <h1 className="style-title">{text.title}</h1>

          <p className="style-description">{text.description}</p>
        </header>

        <div className="style-grid">
          {styleOptions.map((style) => {
            const translatedStyle = text.styles[style.id];
            const isSelected = selectedStyle === style.id;

            return (
              <button
                key={style.id}
                type="button"
                className={`style-card ${
                  isSelected ? "selected" : ""
                }`}
                aria-pressed={isSelected}
                onClick={() => handleStyleSelect(style.id)}
              >
                <div className="style-card-top">
                  <span className="style-number">{style.number}</span>

                  <span className="style-check">
                    {isSelected ? "✓" : ""}
                  </span>
                </div>

                <div className="style-card-bottom">
                  <h2>{translatedStyle.title}</h2>

                  <p>{translatedStyle.description}</p>
                </div>
              </button>
            );
          })}
        </div>

        <button
          type="button"
          className="style-next-button"
          disabled={!selectedStyle}
          onClick={handleNext}
        >
          <span>{text.next}</span>
          <span className="style-next-arrow">→</span>
        </button>
      </section>
    </main>
  );
}

export default App;
