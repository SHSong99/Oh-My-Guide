"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const express_1 = __importDefault(require("express"));
const cors_1 = __importDefault(require("cors"));
const axios_1 = __importDefault(require("axios"));
const path_1 = __importDefault(require("path"));
// ══════════════════════════════════════
// 설정
// ══════════════════════════════════════
const app = (0, express_1.default)();
const PORT = 3000;
// ODsay API
const ODSAY_API_KEY = 'jxt1l35mqg6RryD4p2/3QQ';
const ODSAY_HEADERS = {
    headers: { Referer: 'http://localhost:3000' },
};
app.use((0, cors_1.default)());
app.use(express_1.default.json());
app.use(express_1.default.static(path_1.default.join(__dirname, '..', '..', 'public')));
function parseLang(raw) {
    // ODsay: 0=ko, 1=en, 2=ja, 3=zh-cn, 4=zh-tw, 5=vi(수도권 제한)
    const n = typeof raw === 'string' ? Number(raw) : Array.isArray(raw) ? Number(raw[0]) : NaN;
    if (![0, 1, 2, 3, 4, 5].includes(n))
        return 0;
    return n;
}
// ────────────────────────────────────────
// 1. 장소 검색 (정류장 검색) - ODsay 키워드 검색
// ────────────────────────────────────────
app.get('/api/search', async (req, res) => {
    try {
        const keyword = req.query.keyword;
        const lang = parseLang(req.query.lang);
        if (!keyword) {
            res.status(400).json({ error: '검색어를 입력하세요' });
            return;
        }
        const response = await axios_1.default.get('https://api.odsay.com/v1/api/searchStation', {
            params: {
                lang,
                stationName: keyword,
                stationClass: '',
                apiKey: ODSAY_API_KEY,
            },
            ...ODSAY_HEADERS,
        });
        if (response.data.error) {
            console.error('ODsay 검색 API 오류:', JSON.stringify(response.data.error));
            res.status(502).json({ error: response.data.error[0]?.message || 'ODsay API 오류' });
            return;
        }
        if (response.data.result?.station) {
            const stations = response.data.result.station.map((s) => ({
                name: s.stationName,
                lat: parseFloat(s.y),
                lng: parseFloat(s.x),
                type: s.stationClass === 1 ? '버스' : s.stationClass === 2 ? '지하철' : '기타',
                id: s.stationID,
            }));
            res.json({ results: stations });
            return;
        }
        res.json({ results: [] });
    }
    catch (err) {
        const message = err instanceof Error ? err.message : String(err);
        console.error('검색 오류:', message);
        res.status(500).json({ error: '검색 중 오류 발생' });
    }
});
// ────────────────────────────────────────
// 2. 대중교통 길찾기 (ODsay)
// ────────────────────────────────────────
app.get('/api/route/transit', async (req, res) => {
    try {
        const { sx, sy, ex, ey } = req.query;
        const lang = parseLang(req.query.lang);
        if (!sx || !sy || !ex || !ey) {
            res.status(400).json({ error: '출발지와 도착지 좌표가 필요합니다' });
            return;
        }
        const response = await axios_1.default.get('https://api.odsay.com/v1/api/searchPubTransPathT', {
            params: {
                SX: sx,
                SY: sy,
                EX: ex,
                EY: ey,
                lang,
                apiKey: ODSAY_API_KEY,
            },
            ...ODSAY_HEADERS,
        });
        if (response.data.error) {
            console.error('ODsay 경로 API 오류:', JSON.stringify(response.data.error));
            res.status(502).json({ error: response.data.error[0]?.message || 'ODsay API 오류' });
            return;
        }
        if (response.data.result) {
            res.json(response.data.result);
        }
        else {
            res.json({ error: '경로를 찾을 수 없습니다' });
        }
    }
    catch (err) {
        const message = err instanceof Error ? err.message : String(err);
        console.error('경로 검색 오류:', message);
        res.status(500).json({ error: '경로 검색 중 오류 발생' });
    }
});
// ────────────────────────────────────────
// 3. 대중교통 경로 상세 좌표 (polyline)
// ────────────────────────────────────────
app.get('/api/route/detail', async (req, res) => {
    try {
        const mapObject = req.query.mapObject;
        if (!mapObject) {
            res.status(400).json({ error: 'mapObject가 필요합니다' });
            return;
        }
        const response = await axios_1.default.get('https://api.odsay.com/v1/api/loadLane', {
            params: {
                mapObject,
                apiKey: ODSAY_API_KEY,
            },
            ...ODSAY_HEADERS,
        });
        res.json(response.data);
    }
    catch (err) {
        const message = err instanceof Error ? err.message : String(err);
        console.error('경로 상세 오류:', message);
        res.status(500).json({ error: '경로 상세 조회 중 오류 발생' });
    }
});
// ────────────────────────────────────────
// 4. 자동차 길찾기 (OSRM 무료 API)
// ────────────────────────────────────────
app.get('/api/route/driving', async (req, res) => {
    try {
        const { start, goal } = req.query;
        if (!start || !goal) {
            res.status(400).json({ error: 'start와 goal 좌표가 필요합니다 (lng,lat 형식)' });
            return;
        }
        // OSRM format: /route/v1/driving/lng,lat;lng,lat
        const url = `https://router.project-osrm.org/route/v1/driving/${start};${goal}?overview=full&geometries=geojson&steps=true`;
        const response = await axios_1.default.get(url);
        if (response.data.code !== 'Ok') {
            console.error('OSRM 오류:', JSON.stringify(response.data));
            res.status(502).json({ error: response.data.message || 'OSRM API 오류' });
            return;
        }
        res.json(response.data);
    }
    catch (err) {
        const message = err instanceof Error ? err.message : String(err);
        console.error('자동차 경로 오류:', message);
        res.status(500).json({ error: '자동차 경로 검색 중 오류 발생' });
    }
});
// ────────────────────────────────────────
// SPA fallback
// ────────────────────────────────────────
app.get('*', (_req, res) => {
    res.sendFile(path_1.default.join(__dirname, '..', '..', 'public', 'index.html'));
});
// ── 서버 시작 ──
app.listen(PORT, () => {
    console.log(`✅ 서버 실행 중: http://localhost:${PORT}`);
});
//# sourceMappingURL=server.js.map