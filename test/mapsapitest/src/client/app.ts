// ═══════════════════════════════════════════
// 길찾기 앱 - Naver Maps + ODsay + OSRM
// ═══════════════════════════════════════════

declare const naver: any;

function showFatalError(message: string): void {
  console.error(message);
  const existing = document.querySelector('#fatalErrorBox');
  if (existing) existing.remove();
  const box = document.createElement('div');
  box.id = 'fatalErrorBox';
  box.className = 'error-msg';
  box.style.margin = '12px 20px';
  box.innerHTML = `
    <div style="font-weight:700;margin-bottom:6px;">지도 초기화 실패</div>
    <div style="white-space:pre-wrap;word-break:break-word;">${message}</div>
  `;
  const sidePanel = document.querySelector('#sidePanel');
  if (sidePanel?.firstChild) sidePanel.insertBefore(box, sidePanel.firstChild.nextSibling);
  else sidePanel?.appendChild(box);
}

window.addEventListener('error', (e) => {
  const msg = e.error instanceof Error ? e.error.stack || e.error.message : String(e.message ?? e);
  showFatalError(msg);
});

window.addEventListener('unhandledrejection', (e: PromiseRejectionEvent) => {
  const reason = e.reason instanceof Error ? e.reason.stack || e.reason.message : String(e.reason);
  showFatalError(reason);
});

function isEnglish(): boolean {
  return state.lang === 1;
}

// ── 타입 ──
interface Place {
  name: string;
  lat: number;
  lng: number;
  type: string;
  id?: number;
}

interface SubPath {
  trafficType: number;
  distance: number;
  sectionTime: number;
  stationCount?: number;
  lane?: { busNo?: string; name?: string; subwayCode?: number }[];
  startName?: string;
  endName?: string;
  startX?: number;
  startY?: number;
  endX?: number;
  endY?: number;
}

interface RouteInfo {
  totalTime: number;
  payment: number;
  busTransitCount: number;
  subwayTransitCount: number;
  mapObj: string;
  totalWalk: number;
  totalDistance: number;
}

interface RoutePath {
  pathType: number;
  info: RouteInfo;
  subPath: SubPath[];
}

type TransportMode = 'transit' | 'driving';
type ClickMode = 'start' | 'end';

interface AppState {
  startPlace: Place | null;
  endPlace: Place | null;
  transitRoutes: RoutePath[];
  drivingResult: any;
  mode: TransportMode;
  clickMode: ClickMode;
  lang: number; // ODsay lang: 0..5
  searchTimeout: ReturnType<typeof setTimeout> | null;
  lastSearchKeyword: { start: string; end: string };
  overlays: any[];
  startMarker: any;
  endMarker: any;
}

// ══════════════════════════════════════
// 상태
// ══════════════════════════════════════
const state: AppState = {
  startPlace: null,
  endPlace: null,
  transitRoutes: [],
  drivingResult: null,
  mode: 'transit',
  clickMode: 'start',
  lang: 0,
  searchTimeout: null,
  lastSearchKeyword: { start: '', end: '' },
  overlays: [],
  startMarker: null,
  endMarker: null,
};

// ══════════════════════════════════════
// DOM
// ══════════════════════════════════════
function $<T extends HTMLElement>(sel: string): T {
  const el = document.querySelector<T>(sel);
  if (!el) throw new Error(`Element not found: ${sel}`);
  return el;
}

const startInput = $<HTMLInputElement>('#startInput');
const endInput = $<HTMLInputElement>('#endInput');
const startList = $<HTMLDivElement>('#startList');
const endList = $<HTMLDivElement>('#endList');
const startInfo = $<HTMLDivElement>('#startInfo');
const endInfo = $<HTMLDivElement>('#endInfo');
const clearStart = $<HTMLButtonElement>('#clearStart');
const clearEnd = $<HTMLButtonElement>('#clearEnd');
const swapBtn = $<HTMLButtonElement>('#swapBtn');
const searchBtn = $<HTMLButtonElement>('#searchBtn');
const modeStart = $<HTMLButtonElement>('#modeStart');
const modeEnd = $<HTMLButtonElement>('#modeEnd');
const loading = $<HTMLDivElement>('#loading');
const resultsSection = $<HTMLDivElement>('#resultsSection');
const routeList = $<HTMLDivElement>('#routeList');
const routeDetail = $<HTMLDivElement>('#routeDetail');
const detailContent = $<HTMLDivElement>('#detailContent');
const backBtn = $<HTMLButtonElement>('#backBtn');
const panelToggle = $<HTMLButtonElement>('#panelToggle');
const sidePanel = $<HTMLDivElement>('#sidePanel');
const tabTransit = $<HTMLButtonElement>('#tabTransit');
const tabDriving = $<HTMLButtonElement>('#tabDriving');
const langSelect = document.querySelector<HTMLSelectElement>('#langSelect');

// ══════════════════════════════════════
// Naver 지도 초기화
// ══════════════════════════════════════
let map: any;
try {
  if (typeof naver === 'undefined' || !naver.maps) {
    throw new Error('Naver Maps SDK가 로드되지 않았습니다. (네트워크/인증/URL 등록을 확인)');
  }

  map = new naver.maps.Map('map', {
    center: new naver.maps.LatLng(37.5665, 126.978),
    zoom: 13,
    minZoom: 6,
    maxZoom: 20,
    zoomControl: true,
    zoomControlOptions: { position: naver.maps.Position.TOP_RIGHT },
  });
} catch (err: unknown) {
  const msg = err instanceof Error ? err.stack || err.message : String(err);
  showFatalError(msg);
  // 초기화 실패 시 이후 로직이 map을 사용하지 않도록 방지
  throw err;
}

function markerHtml(label: 'S' | 'E'): string {
  const bg = label === 'S' ? '#43a047' : '#e53935';
  return `<div style="background:${bg};color:white;width:32px;height:32px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:16px;font-weight:bold;border:3px solid white;box-shadow:0 2px 8px rgba(0,0,0,0.3);">${label}</div>`;
}

// ══════════════════════════════════════
// API 호출
// ══════════════════════════════════════

async function searchStation(keyword: string): Promise<Place[]> {
  try {
    const res = await fetch(`/api/search?keyword=${encodeURIComponent(keyword)}&lang=${state.lang}`);
    const data = await res.json();
    return data.results ?? [];
  } catch (e) {
    console.error('검색 오류:', e);
    return [];
  }
}

async function searchTransitRoute(sx: number, sy: number, ex: number, ey: number): Promise<any> {
  try {
    const res = await fetch(`/api/route/transit?sx=${sx}&sy=${sy}&ex=${ex}&ey=${ey}&lang=${state.lang}`);
    return await res.json();
  } catch (e) {
    console.error('대중교통 경로 오류:', e);
    return null;
  }
}

async function searchDrivingRoute(startLng: number, startLat: number, endLng: number, endLat: number): Promise<any> {
  try {
    const start = `${startLng},${startLat}`;
    const goal = `${endLng},${endLat}`;
    const res = await fetch(`/api/route/driving?start=${start}&goal=${goal}`);
    return await res.json();
  } catch (e) {
    console.error('자동차 경로 오류:', e);
    return null;
  }
}

async function loadTransitDetail(mapObject: string): Promise<any> {
  try {
    const res = await fetch(`/api/route/detail?mapObject=${encodeURIComponent(mapObject)}`);
    return await res.json();
  } catch (e) {
    console.error('경로 상세 오류:', e);
    return null;
  }
}

// ══════════════════════════════════════
// 자동완성
// ══════════════════════════════════════

function setupAutocomplete(input: HTMLInputElement, listEl: HTMLDivElement, type: 'start' | 'end'): void {
  input.addEventListener('input', (): void => {
    if (state.searchTimeout) clearTimeout(state.searchTimeout);
    const val = input.value.trim();
    if (val.length < 2) {
      listEl.classList.remove('show');
      return;
    }
    if (state.lastSearchKeyword[type] === val) return;

    state.searchTimeout = setTimeout(async (): Promise<void> => {
      state.lastSearchKeyword[type] = val;
      const results = await searchStation(val);
      renderAutocomplete(results, listEl, type);
    }, 500);
  });

  input.addEventListener('focus', (): void => {
    if (listEl.children.length > 0) listEl.classList.add('show');
  });

  document.addEventListener('click', (e: MouseEvent): void => {
    const target = e.target as Node;
    if (!input.contains(target) && !listEl.contains(target)) {
      listEl.classList.remove('show');
    }
  });
}

function renderAutocomplete(results: Place[], listEl: HTMLDivElement, type: 'start' | 'end'): void {
  listEl.innerHTML = '';
  if (results.length === 0) {
    listEl.innerHTML = `<div class="autocomplete-item" style="color:#999;cursor:default;">${isEnglish() ? 'No results' : '검색 결과가 없습니다'}</div>`;
    listEl.classList.add('show');
    return;
  }
  results.forEach((r: Place): void => {
    const item = document.createElement('div');
    item.className = 'autocomplete-item';
    const badgeClass = r.type === '버스' ? 'bus' : r.type === '지하철' ? 'subway' : 'other';
    item.innerHTML = `<span class="type-badge ${badgeClass}">${r.type}</span><span>${r.name}</span>`;
    item.addEventListener('click', (): void => {
      selectPlace(type, r);
      listEl.classList.remove('show');
    });
    listEl.appendChild(item);
  });
  listEl.classList.add('show');
}

// ══════════════════════════════════════
// 장소 선택
// ══════════════════════════════════════

function selectPlace(type: 'start' | 'end', place: Place): void {
  if (type === 'start') {
    state.startPlace = place;
    startInput.value = place.name;
    startInfo.textContent = `📍 ${place.lat.toFixed(5)}, ${place.lng.toFixed(5)}`;
  } else {
    state.endPlace = place;
    endInput.value = place.name;
    endInfo.textContent = `📍 ${place.lat.toFixed(5)}, ${place.lng.toFixed(5)}`;
  }
  updateMarkers();
  checkSearchReady();
}

function selectPlaceByCoord(type: 'start' | 'end', lat: number, lng: number): void {
  const place: Place = { name: `${lat.toFixed(5)}, ${lng.toFixed(5)}`, lat, lng, type: '지도선택' };
  if (type === 'start') {
    state.startPlace = place;
    startInput.value = isEnglish() ? '📍 Selected location' : '📍 선택한 위치';
    startInfo.textContent = isEnglish()
      ? `Lat: ${lat.toFixed(5)}, Lng: ${lng.toFixed(5)}`
      : `위도: ${lat.toFixed(5)}, 경도: ${lng.toFixed(5)}`;
  } else {
    state.endPlace = place;
    endInput.value = isEnglish() ? '📍 Selected location' : '📍 선택한 위치';
    endInfo.textContent = isEnglish()
      ? `Lat: ${lat.toFixed(5)}, Lng: ${lng.toFixed(5)}`
      : `위도: ${lat.toFixed(5)}, 경도: ${lng.toFixed(5)}`;
  }
  updateMarkers();
  checkSearchReady();
}

// ══════════════════════════════════════
// 마커 관리
// ══════════════════════════════════════

function updateMarkers(): void {
  if (state.startMarker) { state.startMarker.setMap(null); state.startMarker = null; }
  if (state.endMarker) { state.endMarker.setMap(null); state.endMarker = null; }

  if (state.startPlace) {
    state.startMarker = new naver.maps.Marker({
      map,
      position: new naver.maps.LatLng(state.startPlace.lat, state.startPlace.lng),
      icon: {
        content: markerHtml('S'),
        anchor: new naver.maps.Point(16, 16),
      },
    });
  }

  if (state.endPlace) {
    state.endMarker = new naver.maps.Marker({
      map,
      position: new naver.maps.LatLng(state.endPlace.lat, state.endPlace.lng),
      icon: {
        content: markerHtml('E'),
        anchor: new naver.maps.Point(16, 16),
      },
    });
  }

  if (state.startPlace && state.endPlace) {
    const bounds = new naver.maps.LatLngBounds(
      new naver.maps.LatLng(state.startPlace.lat, state.startPlace.lng),
      new naver.maps.LatLng(state.endPlace.lat, state.endPlace.lng)
    );
    map.fitBounds(bounds, { top: 60, right: 60, bottom: 60, left: 60 });
  } else if (state.startPlace) {
    map.setCenter(new naver.maps.LatLng(state.startPlace.lat, state.startPlace.lng));
    map.setZoom(15, true);
  } else if (state.endPlace) {
    map.setCenter(new naver.maps.LatLng(state.endPlace.lat, state.endPlace.lng));
    map.setZoom(15, true);
  }
}

function checkSearchReady(): void {
  searchBtn.disabled = !(state.startPlace && state.endPlace);
}

function clearOverlays(): void {
  state.overlays.forEach((o: any) => o.setMap?.(null));
  state.overlays = [];
}

// ══════════════════════════════════════
// 경로 검색
// ══════════════════════════════════════

async function doSearch(): Promise<void> {
  if (!state.startPlace || !state.endPlace) return;

  loading.classList.add('show');
  resultsSection.classList.remove('show');
  routeDetail.classList.remove('show');
  clearOverlays();

  if (state.mode === 'transit') {
    await doTransitSearch();
  } else {
    await doDrivingSearch();
  }

  loading.classList.remove('show');
}

async function doTransitSearch(): Promise<void> {
  const result = await searchTransitRoute(
    state.startPlace!.lng, state.startPlace!.lat,
    state.endPlace!.lng, state.endPlace!.lat
  );

  if (!result || !result.path) {
    showNoResult();
    return;
  }

  state.transitRoutes = result.path;
  renderTransitList();
}

async function doDrivingSearch(): Promise<void> {
  const result = await searchDrivingRoute(
    state.startPlace!.lng, state.startPlace!.lat,
    state.endPlace!.lng, state.endPlace!.lat
  );

  if (!result || result.code !== 'Ok' || !result.routes?.length) {
    showNoResult();
    return;
  }

  state.drivingResult = result;
  renderDrivingList();
}

function showNoResult(): void {
  resultsSection.classList.add('show');
  routeList.innerHTML = isEnglish()
    ? `
      <div class="no-result">
        <div class="icon">🚫</div>
        <p>No route found.<br>Please check your start and destination.</p>
      </div>
    `
    : `
      <div class="no-result">
        <div class="icon">🚫</div>
        <p>경로를 찾을 수 없습니다.<br>출발지와 도착지를 다시 확인해주세요.</p>
      </div>
    `;
}

// ══════════════════════════════════════
// 대중교통 결과 목록
// ══════════════════════════════════════

function renderTransitList(): void {
  resultsSection.classList.add('show');
  routeList.innerHTML = '';

  state.transitRoutes.forEach((route: RoutePath, idx: number): void => {
    const { info } = route;
    const subPaths = route.subPath;
    const card = document.createElement('div');
    card.className = 'route-card';

    const totalTime = info.totalTime;
    let barHTML = '';
    let stepsHTML = '';

    subPaths.forEach((sp, i) => {
      const ratio = (sp.sectionTime / totalTime) * 100;
      if (sp.trafficType === 3) {
        barHTML += `<div class="route-bar-segment walk" style="width:${Math.max(ratio, 2)}%"></div>`;
        if (sp.sectionTime > 0) stepsHTML += `<span class="step-badge walk">🚶 ${sp.sectionTime}분</span>`;
      } else if (sp.trafficType === 2) {
        const busName = sp.lane?.[0]?.busNo ?? '버스';
        barHTML += `<div class="route-bar-segment bus" style="width:${Math.max(ratio, 2)}%"></div>`;
        stepsHTML += `<span class="step-badge bus">🚌 ${busName}</span>`;
      } else if (sp.trafficType === 1) {
        const lineName = sp.lane?.[0]?.name ?? '지하철';
        const lineNum = sp.lane?.[0]?.subwayCode ?? 0;
        const lineClass = lineNum >= 1 && lineNum <= 9 ? `subway-line-${lineNum}` : 'subway';
        barHTML += `<div class="route-bar-segment ${lineClass}" style="width:${Math.max(ratio, 2)}%"></div>`;
        stepsHTML += `<span class="step-badge subway">🚇 ${lineName}</span>`;
      }
      if (i < subPaths.length - 1 && sp.sectionTime > 0) stepsHTML += '<span class="step-arrow">→</span>';
    });

    const transferCount = info.busTransitCount + info.subwayTransitCount - 1;

    card.innerHTML = `
      <div class="route-card-header">
        <div class="route-time">${totalTime}<span>분</span></div>
        <div class="route-meta">
          <div class="cost">${info.payment.toLocaleString()}원</div>
          <div>도보 ${info.totalWalk}m${transferCount > 0 ? ` · 환승 ${transferCount}회` : ''}</div>
        </div>
      </div>
      <div class="route-bar">${barHTML}</div>
      <div class="route-steps">${stepsHTML}</div>
    `;

    card.addEventListener('click', () => showTransitDetail(idx));
    routeList.appendChild(card);
  });
}

// ══════════════════════════════════════
// 자동차 결과 목록
// ══════════════════════════════════════

function renderDrivingList(): void {
  resultsSection.classList.add('show');
  routeList.innerHTML = '';

  const routes = state.drivingResult.routes as any[];

  routes.forEach((route: any, idx: number): void => {
    const card = document.createElement('div');
    card.className = 'route-card';

    const durationMin = Math.round(route.duration / 60);
    const distanceKm = (route.distance / 1000).toFixed(1);

    card.innerHTML = `
      <div class="route-card-header">
        <div class="route-time">${durationMin}<span>분</span></div>
        <div class="route-meta">
          <div class="cost">${distanceKm}km</div>
          <div>🚗 자동차 경로</div>
        </div>
      </div>
      <div class="driving-info">
        <span class="driving-tag">📏 ${distanceKm}km</span>
        <span class="driving-tag">⏱️ 약 ${durationMin}분</span>
      </div>
    `;

    card.addEventListener('click', () => showDrivingDetail(idx));
    routeList.appendChild(card);
  });
}

// ══════════════════════════════════════
// 대중교통 상세
// ══════════════════════════════════════

const SUBWAY_COLORS: Record<number, string> = {
  1: '#0052A4', 2: '#009B3E', 3: '#EF7C1C', 4: '#00A2D1',
  5: '#996CAC', 6: '#CD7C2F', 7: '#747F00', 8: '#E6186C', 9: '#BDB092',
};

async function showTransitDetail(idx: number): Promise<void> {
  const route = state.transitRoutes[idx];
  resultsSection.classList.remove('show');
  routeDetail.classList.add('show');

  const { info } = route;
  const subPaths = route.subPath;

  let html = `
    <div class="detail-summary">
      <div class="total-time">${info.totalTime}<span>분</span></div>
      <div class="summary-meta">
        <span>💰 ${isEnglish() ? `₩${info.payment.toLocaleString()}` : `${info.payment.toLocaleString()}원`}</span>
        <span>🚶 ${isEnglish() ? 'Walk' : '도보'} ${info.totalWalk}m</span>
        <span>🔄 ${isEnglish() ? 'Transfers' : '환승'} ${Math.max(0, info.busTransitCount + info.subwayTransitCount - 1)}${isEnglish() ? '' : '회'}</span>
      </div>
    </div>
    <div class="timeline">
      <div class="timeline-item">
        <div class="timeline-dot start"></div>
        <div class="timeline-content"><h4>${isEnglish() ? 'Start' : '출발'}</h4><p>${state.startPlace!.name}</p></div>
      </div>
  `;

  subPaths.forEach((sp) => {
    if (sp.trafficType === 3 && sp.sectionTime > 0) {
      html += isEnglish()
        ? `<div class="timeline-item"><div class="timeline-dot walk"></div><div class="timeline-content"><h4>🚶 Walk</h4><p>${sp.distance}m · ~${sp.sectionTime} min</p></div></div>`
        : `<div class="timeline-item"><div class="timeline-dot walk"></div><div class="timeline-content"><h4>🚶 도보 이동</h4><p>${sp.distance}m · 약 ${sp.sectionTime}분</p></div></div>`;
    } else if (sp.trafficType === 2) {
      const busName = sp.lane?.[0]?.busNo ?? '';
      html += isEnglish()
        ? `<div class="timeline-item"><div class="timeline-dot bus"></div><div class="timeline-content"><h4>Board at ${sp.startName ?? ''}</h4><div class="transport-info bus">🚌 ${busName}</div><p>${sp.stationCount ?? 0} stops · ${sp.sectionTime} min</p><h4 style="margin-top:8px;">Get off at ${sp.endName ?? ''}</h4></div></div>`
        : `<div class="timeline-item"><div class="timeline-dot bus"></div><div class="timeline-content"><h4>${sp.startName ?? ''} 승차</h4><div class="transport-info bus">🚌 ${busName}</div><p>${sp.stationCount ?? 0}개 정류장 · ${sp.sectionTime}분</p><h4 style="margin-top:8px;">${sp.endName ?? ''} 하차</h4></div></div>`;
    } else if (sp.trafficType === 1) {
      const lineName = sp.lane?.[0]?.name ?? '';
      html += isEnglish()
        ? `<div class="timeline-item"><div class="timeline-dot subway"></div><div class="timeline-content"><h4>Board at ${sp.startName ?? ''}</h4><div class="transport-info subway">🚇 ${lineName}</div><p>${sp.stationCount ?? 0} stations · ${sp.sectionTime} min</p><h4 style="margin-top:8px;">Get off at ${sp.endName ?? ''}</h4></div></div>`
        : `<div class="timeline-item"><div class="timeline-dot subway"></div><div class="timeline-content"><h4>${sp.startName ?? ''} 승차</h4><div class="transport-info subway">🚇 ${lineName}</div><p>${sp.stationCount ?? 0}개 역 · ${sp.sectionTime}분</p><h4 style="margin-top:8px;">${sp.endName ?? ''} 하차</h4></div></div>`;
    }
  });

  html += `
      <div class="timeline-item">
        <div class="timeline-dot end"></div>
        <div class="timeline-content"><h4>${isEnglish() ? 'Arrive' : '도착'}</h4><p>${state.endPlace!.name}</p></div>
      </div>
    </div>
  `;

  detailContent.innerHTML = html;
  await drawTransitRoute(route);
}

async function drawTransitRoute(route: RoutePath): Promise<void> {
  clearOverlays();

  const detail = await loadTransitDetail(route.info.mapObj);

  if (detail?.result?.lane) {
    detail.result.lane.forEach((lane: any) => {
      if (lane.section) {
        lane.section.forEach((section: any) => {
          if (section.graphPos) {
            const path = section.graphPos.map((p: any) => new naver.maps.LatLng(p.y, p.x));
            let color = '#999';
            if (lane.class === 1) color = SUBWAY_COLORS[lane.type] ?? '#1e88e5';
            else if (lane.class === 2) color = '#43a047';

            const polyline = new naver.maps.Polyline({
              map,
              path,
              strokeColor: color,
              strokeOpacity: 0.8,
              strokeWeight: 5,
              strokeLineCap: 'round',
              strokeLineJoin: 'round',
            });
            state.overlays.push(polyline);
          }
        });
      }
    });
  }

  // 승하차 마커
  route.subPath.forEach((sp) => {
    if (sp.trafficType !== 3) {
      const dotColor = sp.trafficType === 1 ? '#1e88e5' : '#43a047';
      if (sp.startX && sp.startY) {
        const m = new naver.maps.Marker({
          map,
          position: new naver.maps.LatLng(sp.startY, sp.startX),
          icon: {
            content: `<div style="width:12px;height:12px;border-radius:50%;background:#fff;border:3px solid ${dotColor};box-shadow:0 1px 6px rgba(0,0,0,0.25);"></div>`,
            anchor: new naver.maps.Point(6, 6),
          },
        });
        state.overlays.push(m);
      }
      if (sp.endX && sp.endY) {
        const m = new naver.maps.Marker({
          map,
          position: new naver.maps.LatLng(sp.endY, sp.endX),
          icon: {
            content: `<div style="width:12px;height:12px;border-radius:50%;background:#fff;border:3px solid ${dotColor};box-shadow:0 1px 6px rgba(0,0,0,0.25);"></div>`,
            anchor: new naver.maps.Point(6, 6),
          },
        });
        state.overlays.push(m);
      }
    }
  });

  fitAllOverlays();
}

// ══════════════════════════════════════
// 자동차 상세
// ══════════════════════════════════════

function showDrivingDetail(idx: number): void {
  const route = state.drivingResult.routes[idx];

  resultsSection.classList.remove('show');
  routeDetail.classList.add('show');

  const durationMin = Math.round(route.duration / 60);
  const distanceKm = (route.distance / 1000).toFixed(1);

  // 턴바이턴 안내 생성
  const steps = route.legs?.[0]?.steps ?? [];
  let stepsHTML = '';
  steps.forEach((step: any, i: number) => {
    const stepDist = step.distance >= 1000 ? `${(step.distance / 1000).toFixed(1)}km` : `${Math.round(step.distance)}m`;
    const stepTime = Math.round(step.duration / 60);
    const instruction = translateManeuver(step.maneuver?.type ?? '', step.name || '');
    stepsHTML += `
      <div class="timeline-item">
        <div class="timeline-dot ${i === 0 ? 'start' : i === steps.length - 1 ? 'end' : 'walk'}"></div>
        <div class="timeline-content">
          <h4>${instruction}</h4>
          <p>${stepDist} · 약 ${stepTime > 0 ? stepTime + '분' : '1분 미만'}</p>
        </div>
      </div>
    `;
  });

  detailContent.innerHTML = `
    <div class="detail-summary driving">
      <div class="total-time">${durationMin}<span>분</span></div>
      <div class="summary-meta">
        <span>📏 ${distanceKm}km</span>
        <span>🚗 ${isEnglish() ? 'Driving route' : '자동차 경로'}</span>
      </div>
    </div>
    <div class="driving-detail-info">
      <h3>🚗 ${isEnglish() ? 'Turn-by-turn' : '턴바이턴 안내'}</h3>
      <p>${isEnglish() ? 'From' : '출발'}: ${state.startPlace!.name}</p>
      <p>${isEnglish() ? 'To' : '도착'}: ${state.endPlace!.name}</p>
    </div>
    <div class="timeline" style="margin-top:16px;">
      ${stepsHTML}
    </div>
  `;

  drawDrivingRoute(route);
}

function translateManeuver(type: string, road: string): string {
  const roadLabel = road ? ` (${road})` : '';
  const maneuvers: Record<string, string> = {
    'depart': '🚗 출발',
    'arrive': '🏁 도착',
    'turn': '↪️ 회전',
    'new name': '➡️ 직진',
    'merge': '🔀 합류',
    'on ramp': '⬆️ 진입로',
    'off ramp': '⬇️ 출구',
    'fork': '🔀 분기',
    'end of road': '🔚 도로 끝',
    'continue': '➡️ 직진',
    'roundabout': '🔄 회전교차로',
    'rotary': '🔄 로터리',
    'roundabout turn': '🔄 회전교차로 회전',
    'notification': 'ℹ️ 알림',
    'exit roundabout': '↗️ 회전교차로 나가기',
  };
  return (maneuvers[type] ?? `➡️ ${type}`) + roadLabel;
}

function drawDrivingRoute(route: any): void {
  clearOverlays();

  const geojson = route.geometry;
  if (!geojson?.coordinates?.length) return;

  const path = geojson.coordinates.map((c: number[]) => new naver.maps.LatLng(c[1], c[0]));

  const polyline = new naver.maps.Polyline({
    map,
    path,
    strokeColor: '#4285f4',
    strokeOpacity: 0.9,
    strokeWeight: 6,
    strokeLineCap: 'round',
    strokeLineJoin: 'round',
  });
  state.overlays.push(polyline);

  fitAllOverlays();
}

function fitAllOverlays(): void {
  const bounds = new naver.maps.LatLngBounds();
  let count = 0;

  const extend = (lat: number, lng: number): void => {
    bounds.extend(new naver.maps.LatLng(lat, lng));
    count += 1;
  };

  if (state.startPlace) extend(state.startPlace.lat, state.startPlace.lng);
  if (state.endPlace) extend(state.endPlace.lat, state.endPlace.lng);

  state.overlays.forEach((o: any) => {
    if (o && typeof o.getPath === 'function') {
      const path = o.getPath();
      if (path && typeof path.getLength === 'function') {
        for (let i = 0; i < path.getLength(); i++) {
          const p = path.getAt(i);
          extend(p.lat(), p.lng());
        }
      }
      return;
    }
    if (o && typeof o.getPosition === 'function') {
      const p = o.getPosition();
      if (p) extend(p.lat(), p.lng());
    }
  });

  if (count > 1) {
    map.fitBounds(bounds, { top: 50, right: 50, bottom: 50, left: 50 });
  }
}

// ══════════════════════════════════════
// 이벤트 리스너
// ══════════════════════════════════════

function detectAutoLang(): number {
  const lang = (navigator.language || '').toLowerCase();
  if (lang.startsWith('ko')) return 0;
  if (lang.startsWith('ja')) return 2;
  if (lang.startsWith('zh')) {
    // zh-cn, zh-hans => 간체(3) / zh-tw, zh-hant, zh-hk => 번체(4)
    if (lang.includes('tw') || lang.includes('hant') || lang.includes('hk')) return 4;
    return 3;
  }
  if (lang.startsWith('vi')) return 5;
  return 1; // English default
}

function setLang(next: number): void {
  state.lang = next;
  // 언어가 바뀌면 자동완성 캐시를 비우고, 결과/오버레이도 정리
  state.lastSearchKeyword = { start: '', end: '' };
  startList.classList.remove('show');
  endList.classList.remove('show');
  resultsSection.classList.remove('show');
  routeDetail.classList.remove('show');
  clearOverlays();
}

if (langSelect) {
  // 외국인 타겟: English를 기본값으로 두고, 필요시 사용자가 변경
  setLang(1);
  langSelect.value = '1';

  langSelect.addEventListener('change', (): void => {
    const v = langSelect.value;
    if (v === 'auto') setLang(detectAutoLang());
    else setLang(Number(v));
  });
} else {
  setLang(1);
}

setupAutocomplete(startInput, startList, 'start');
setupAutocomplete(endInput, endList, 'end');

clearStart.addEventListener('click', (): void => {
  state.startPlace = null;
  startInput.value = '';
  startInfo.textContent = '';
  state.lastSearchKeyword.start = '';
  updateMarkers();
  checkSearchReady();
});

clearEnd.addEventListener('click', (): void => {
  state.endPlace = null;
  endInput.value = '';
  endInfo.textContent = '';
  state.lastSearchKeyword.end = '';
  updateMarkers();
  checkSearchReady();
});

swapBtn.addEventListener('click', (): void => {
  const temp = state.startPlace;
  state.startPlace = state.endPlace;
  state.endPlace = temp;
  startInput.value = state.startPlace?.name ?? '';
  endInput.value = state.endPlace?.name ?? '';
  startInfo.textContent = state.startPlace ? `📍 ${state.startPlace.lat.toFixed(5)}, ${state.startPlace.lng.toFixed(5)}` : '';
  endInfo.textContent = state.endPlace ? `📍 ${state.endPlace.lat.toFixed(5)}, ${state.endPlace.lng.toFixed(5)}` : '';
  updateMarkers();
  checkSearchReady();
});

searchBtn.addEventListener('click', doSearch);

[startInput, endInput].forEach((input) => {
  input.addEventListener('keydown', (e: KeyboardEvent): void => {
    if (e.key === 'Enter' && state.startPlace && state.endPlace) doSearch();
  });
});

// 탭 전환
tabTransit.addEventListener('click', (): void => {
  state.mode = 'transit';
  tabTransit.classList.add('active');
  tabDriving.classList.remove('active');
  resultsSection.classList.remove('show');
  routeDetail.classList.remove('show');
  clearOverlays();
});

tabDriving.addEventListener('click', (): void => {
  state.mode = 'driving';
  tabDriving.classList.add('active');
  tabTransit.classList.remove('active');
  resultsSection.classList.remove('show');
  routeDetail.classList.remove('show');
  clearOverlays();
});

// 지도 클릭
modeStart.addEventListener('click', (): void => {
  state.clickMode = 'start';
  modeStart.classList.add('active');
  modeEnd.classList.remove('active');
});

modeEnd.addEventListener('click', (): void => {
  state.clickMode = 'end';
  modeEnd.classList.add('active');
  modeStart.classList.remove('active');
});

naver.maps.Event.addListener(map, 'click', (e: any): void => {
  const lat = e.coord.lat();
  const lng = e.coord.lng();
  selectPlaceByCoord(state.clickMode, lat, lng);
  if (state.clickMode === 'start' && !state.endPlace) {
    state.clickMode = 'end';
    modeEnd.classList.add('active');
    modeStart.classList.remove('active');
  }
});

backBtn.addEventListener('click', (): void => {
  routeDetail.classList.remove('show');
  resultsSection.classList.add('show');
  clearOverlays();
});

panelToggle.addEventListener('click', (): void => {
  sidePanel.classList.toggle('open');
});

searchBtn.disabled = true;

console.log('🗺️ App loaded! (Naver Maps + ODsay + OSRM)');
