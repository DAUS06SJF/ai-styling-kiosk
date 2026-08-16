const browserBackendUrl = `${window.location.protocol}//${window.location.hostname}:8080`;

export const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || browserBackendUrl)
  .replace(/\/$/, "");

export const WEB_SOCKET_URL = import.meta.env.VITE_WS_URL
  || `${API_BASE_URL.replace(/^http/i, "ws")}/ws/styling`;

async function request(path, init) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        "Content-Type": "application/json",
        ...init?.headers,
      },
    });
  } catch (error) {
    if (error?.name === "AbortError") {
      throw error;
    }
    throw new Error("백엔드에 연결할 수 없습니다. 서버 실행 상태를 확인해 주세요.");
  }
  const body = await response.json().catch(() => null);

  if (!response.ok || !body?.success) {
    throw new Error(body?.error?.message || "서버 요청을 처리하지 못했습니다.");
  }
  return body.data;
}

export function createStylingRecommendation(input) {
  return request("/api/styling/recommendations", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function getLatestStylingRecommendations(mood, limit = 4, signal) {
  const params = new URLSearchParams({ mood, limit: String(limit) });
  return request(`/api/styling/recommendations?${params}`, { signal });
}

export function selectStylingRecommendation(id) {
  return request(`/api/styling/recommendations/${id}/select`, { method: "POST" });
}

export function resolveBackendAssetUrl(assetUrl) {
  if (!assetUrl) return "";

  try {
    const asset = new URL(assetUrl, API_BASE_URL);
    const api = new URL(API_BASE_URL);
    if (["localhost", "127.0.0.1"].includes(asset.hostname)
      && !["localhost", "127.0.0.1"].includes(api.hostname)) {
      asset.protocol = api.protocol;
      asset.host = api.host;
    }
    return asset.toString();
  } catch {
    return assetUrl;
  }
}
