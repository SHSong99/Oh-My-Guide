"use strict";
(() => {
  // src/client/app.ts
  function showFatalError(message) {
    console.error(message);
    const existing = document.querySelector("#fatalErrorBox");
    if (existing)
      existing.remove();
    const box = document.createElement("div");
    box.id = "fatalErrorBox";
    box.className = "error-msg";
    box.style.margin = "12px 20px";
    box.innerHTML = `
    <div style="font-weight:700;margin-bottom:6px;">\uC9C0\uB3C4 \uCD08\uAE30\uD654 \uC2E4\uD328</div>
    <div style="white-space:pre-wrap;word-break:break-word;">${message}</div>
  `;
    const sidePanel2 = document.querySelector("#sidePanel");
    if (sidePanel2?.firstChild)
      sidePanel2.insertBefore(box, sidePanel2.firstChild.nextSibling);
    else
      sidePanel2?.appendChild(box);
  }
  window.addEventListener("error", (e) => {
    const msg = e.error instanceof Error ? e.error.stack || e.error.message : String(e.message ?? e);
    showFatalError(msg);
  });
  window.addEventListener("unhandledrejection", (e) => {
    const reason = e.reason instanceof Error ? e.reason.stack || e.reason.message : String(e.reason);
    showFatalError(reason);
  });
  function isEnglish() {
    return state.lang === 1;
  }
  var state = {
    startPlace: null,
    endPlace: null,
    transitRoutes: [],
    drivingResult: null,
    mode: "transit",
    clickMode: "start",
    lang: 0,
    searchTimeout: null,
    lastSearchKeyword: { start: "", end: "" },
    overlays: [],
    startMarker: null,
    endMarker: null
  };
  function $(sel) {
    const el = document.querySelector(sel);
    if (!el)
      throw new Error(`Element not found: ${sel}`);
    return el;
  }
  var startInput = $("#startInput");
  var endInput = $("#endInput");
  var startList = $("#startList");
  var endList = $("#endList");
  var startInfo = $("#startInfo");
  var endInfo = $("#endInfo");
  var clearStart = $("#clearStart");
  var clearEnd = $("#clearEnd");
  var swapBtn = $("#swapBtn");
  var searchBtn = $("#searchBtn");
  var modeStart = $("#modeStart");
  var modeEnd = $("#modeEnd");
  var loading = $("#loading");
  var resultsSection = $("#resultsSection");
  var routeList = $("#routeList");
  var routeDetail = $("#routeDetail");
  var detailContent = $("#detailContent");
  var backBtn = $("#backBtn");
  var panelToggle = $("#panelToggle");
  var sidePanel = $("#sidePanel");
  var tabTransit = $("#tabTransit");
  var tabDriving = $("#tabDriving");
  var langSelect = document.querySelector("#langSelect");
  var map;
  try {
    if (typeof naver === "undefined" || !naver.maps) {
      throw new Error("Naver Maps SDK\uAC00 \uB85C\uB4DC\uB418\uC9C0 \uC54A\uC558\uC2B5\uB2C8\uB2E4. (\uB124\uD2B8\uC6CC\uD06C/\uC778\uC99D/URL \uB4F1\uB85D\uC744 \uD655\uC778)");
    }
    map = new naver.maps.Map("map", {
      center: new naver.maps.LatLng(37.5665, 126.978),
      zoom: 13,
      minZoom: 6,
      maxZoom: 20,
      zoomControl: true,
      zoomControlOptions: { position: naver.maps.Position.TOP_RIGHT }
    });
  } catch (err) {
    const msg = err instanceof Error ? err.stack || err.message : String(err);
    showFatalError(msg);
    throw err;
  }
  function markerHtml(label) {
    const bg = label === "S" ? "#43a047" : "#e53935";
    return `<div style="background:${bg};color:white;width:32px;height:32px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:16px;font-weight:bold;border:3px solid white;box-shadow:0 2px 8px rgba(0,0,0,0.3);">${label}</div>`;
  }
  async function searchStation(keyword) {
    try {
      const res = await fetch(`/api/search?keyword=${encodeURIComponent(keyword)}&lang=${state.lang}`);
      const data = await res.json();
      return data.results ?? [];
    } catch (e) {
      console.error("\uAC80\uC0C9 \uC624\uB958:", e);
      return [];
    }
  }
  async function searchTransitRoute(sx, sy, ex, ey) {
    try {
      const res = await fetch(`/api/route/transit?sx=${sx}&sy=${sy}&ex=${ex}&ey=${ey}&lang=${state.lang}`);
      return await res.json();
    } catch (e) {
      console.error("\uB300\uC911\uAD50\uD1B5 \uACBD\uB85C \uC624\uB958:", e);
      return null;
    }
  }
  async function searchDrivingRoute(startLng, startLat, endLng, endLat) {
    try {
      const start = `${startLng},${startLat}`;
      const goal = `${endLng},${endLat}`;
      const res = await fetch(`/api/route/driving?start=${start}&goal=${goal}`);
      return await res.json();
    } catch (e) {
      console.error("\uC790\uB3D9\uCC28 \uACBD\uB85C \uC624\uB958:", e);
      return null;
    }
  }
  async function loadTransitDetail(mapObject) {
    try {
      const res = await fetch(`/api/route/detail?mapObject=${encodeURIComponent(mapObject)}`);
      return await res.json();
    } catch (e) {
      console.error("\uACBD\uB85C \uC0C1\uC138 \uC624\uB958:", e);
      return null;
    }
  }
  function setupAutocomplete(input, listEl, type) {
    input.addEventListener("input", () => {
      if (state.searchTimeout)
        clearTimeout(state.searchTimeout);
      const val = input.value.trim();
      if (val.length < 2) {
        listEl.classList.remove("show");
        return;
      }
      if (state.lastSearchKeyword[type] === val)
        return;
      state.searchTimeout = setTimeout(async () => {
        state.lastSearchKeyword[type] = val;
        const results = await searchStation(val);
        renderAutocomplete(results, listEl, type);
      }, 500);
    });
    input.addEventListener("focus", () => {
      if (listEl.children.length > 0)
        listEl.classList.add("show");
    });
    document.addEventListener("click", (e) => {
      const target = e.target;
      if (!input.contains(target) && !listEl.contains(target)) {
        listEl.classList.remove("show");
      }
    });
  }
  function renderAutocomplete(results, listEl, type) {
    listEl.innerHTML = "";
    if (results.length === 0) {
      listEl.innerHTML = `<div class="autocomplete-item" style="color:#999;cursor:default;">${isEnglish() ? "No results" : "\uAC80\uC0C9 \uACB0\uACFC\uAC00 \uC5C6\uC2B5\uB2C8\uB2E4"}</div>`;
      listEl.classList.add("show");
      return;
    }
    results.forEach((r) => {
      const item = document.createElement("div");
      item.className = "autocomplete-item";
      const badgeClass = r.type === "\uBC84\uC2A4" ? "bus" : r.type === "\uC9C0\uD558\uCCA0" ? "subway" : "other";
      item.innerHTML = `<span class="type-badge ${badgeClass}">${r.type}</span><span>${r.name}</span>`;
      item.addEventListener("click", () => {
        selectPlace(type, r);
        listEl.classList.remove("show");
      });
      listEl.appendChild(item);
    });
    listEl.classList.add("show");
  }
  function selectPlace(type, place) {
    if (type === "start") {
      state.startPlace = place;
      startInput.value = place.name;
      startInfo.textContent = `\u{1F4CD} ${place.lat.toFixed(5)}, ${place.lng.toFixed(5)}`;
    } else {
      state.endPlace = place;
      endInput.value = place.name;
      endInfo.textContent = `\u{1F4CD} ${place.lat.toFixed(5)}, ${place.lng.toFixed(5)}`;
    }
    updateMarkers();
    checkSearchReady();
  }
  function selectPlaceByCoord(type, lat, lng) {
    const place = { name: `${lat.toFixed(5)}, ${lng.toFixed(5)}`, lat, lng, type: "\uC9C0\uB3C4\uC120\uD0DD" };
    if (type === "start") {
      state.startPlace = place;
      startInput.value = isEnglish() ? "\u{1F4CD} Selected location" : "\u{1F4CD} \uC120\uD0DD\uD55C \uC704\uCE58";
      startInfo.textContent = isEnglish() ? `Lat: ${lat.toFixed(5)}, Lng: ${lng.toFixed(5)}` : `\uC704\uB3C4: ${lat.toFixed(5)}, \uACBD\uB3C4: ${lng.toFixed(5)}`;
    } else {
      state.endPlace = place;
      endInput.value = isEnglish() ? "\u{1F4CD} Selected location" : "\u{1F4CD} \uC120\uD0DD\uD55C \uC704\uCE58";
      endInfo.textContent = isEnglish() ? `Lat: ${lat.toFixed(5)}, Lng: ${lng.toFixed(5)}` : `\uC704\uB3C4: ${lat.toFixed(5)}, \uACBD\uB3C4: ${lng.toFixed(5)}`;
    }
    updateMarkers();
    checkSearchReady();
  }
  function updateMarkers() {
    if (state.startMarker) {
      state.startMarker.setMap(null);
      state.startMarker = null;
    }
    if (state.endMarker) {
      state.endMarker.setMap(null);
      state.endMarker = null;
    }
    if (state.startPlace) {
      state.startMarker = new naver.maps.Marker({
        map,
        position: new naver.maps.LatLng(state.startPlace.lat, state.startPlace.lng),
        icon: {
          content: markerHtml("S"),
          anchor: new naver.maps.Point(16, 16)
        }
      });
    }
    if (state.endPlace) {
      state.endMarker = new naver.maps.Marker({
        map,
        position: new naver.maps.LatLng(state.endPlace.lat, state.endPlace.lng),
        icon: {
          content: markerHtml("E"),
          anchor: new naver.maps.Point(16, 16)
        }
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
  function checkSearchReady() {
    searchBtn.disabled = !(state.startPlace && state.endPlace);
  }
  function clearOverlays() {
    state.overlays.forEach((o) => o.setMap?.(null));
    state.overlays = [];
  }
  async function doSearch() {
    if (!state.startPlace || !state.endPlace)
      return;
    loading.classList.add("show");
    resultsSection.classList.remove("show");
    routeDetail.classList.remove("show");
    clearOverlays();
    if (state.mode === "transit") {
      await doTransitSearch();
    } else {
      await doDrivingSearch();
    }
    loading.classList.remove("show");
  }
  async function doTransitSearch() {
    const result = await searchTransitRoute(
      state.startPlace.lng,
      state.startPlace.lat,
      state.endPlace.lng,
      state.endPlace.lat
    );
    if (!result || !result.path) {
      showNoResult();
      return;
    }
    state.transitRoutes = result.path;
    renderTransitList();
  }
  async function doDrivingSearch() {
    const result = await searchDrivingRoute(
      state.startPlace.lng,
      state.startPlace.lat,
      state.endPlace.lng,
      state.endPlace.lat
    );
    if (!result || result.code !== "Ok" || !result.routes?.length) {
      showNoResult();
      return;
    }
    state.drivingResult = result;
    renderDrivingList();
  }
  function showNoResult() {
    resultsSection.classList.add("show");
    routeList.innerHTML = isEnglish() ? `
      <div class="no-result">
        <div class="icon">\u{1F6AB}</div>
        <p>No route found.<br>Please check your start and destination.</p>
      </div>
    ` : `
      <div class="no-result">
        <div class="icon">\u{1F6AB}</div>
        <p>\uACBD\uB85C\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC2B5\uB2C8\uB2E4.<br>\uCD9C\uBC1C\uC9C0\uC640 \uB3C4\uCC29\uC9C0\uB97C \uB2E4\uC2DC \uD655\uC778\uD574\uC8FC\uC138\uC694.</p>
      </div>
    `;
  }
  function renderTransitList() {
    resultsSection.classList.add("show");
    routeList.innerHTML = "";
    state.transitRoutes.forEach((route, idx) => {
      const { info } = route;
      const subPaths = route.subPath;
      const card = document.createElement("div");
      card.className = "route-card";
      const totalTime = info.totalTime;
      let barHTML = "";
      let stepsHTML = "";
      subPaths.forEach((sp, i) => {
        const ratio = sp.sectionTime / totalTime * 100;
        if (sp.trafficType === 3) {
          barHTML += `<div class="route-bar-segment walk" style="width:${Math.max(ratio, 2)}%"></div>`;
          if (sp.sectionTime > 0)
            stepsHTML += `<span class="step-badge walk">\u{1F6B6} ${sp.sectionTime}\uBD84</span>`;
        } else if (sp.trafficType === 2) {
          const busName = sp.lane?.[0]?.busNo ?? "\uBC84\uC2A4";
          barHTML += `<div class="route-bar-segment bus" style="width:${Math.max(ratio, 2)}%"></div>`;
          stepsHTML += `<span class="step-badge bus">\u{1F68C} ${busName}</span>`;
        } else if (sp.trafficType === 1) {
          const lineName = sp.lane?.[0]?.name ?? "\uC9C0\uD558\uCCA0";
          const lineNum = sp.lane?.[0]?.subwayCode ?? 0;
          const lineClass = lineNum >= 1 && lineNum <= 9 ? `subway-line-${lineNum}` : "subway";
          barHTML += `<div class="route-bar-segment ${lineClass}" style="width:${Math.max(ratio, 2)}%"></div>`;
          stepsHTML += `<span class="step-badge subway">\u{1F687} ${lineName}</span>`;
        }
        if (i < subPaths.length - 1 && sp.sectionTime > 0)
          stepsHTML += '<span class="step-arrow">\u2192</span>';
      });
      const transferCount = info.busTransitCount + info.subwayTransitCount - 1;
      card.innerHTML = `
      <div class="route-card-header">
        <div class="route-time">${totalTime}<span>\uBD84</span></div>
        <div class="route-meta">
          <div class="cost">${info.payment.toLocaleString()}\uC6D0</div>
          <div>\uB3C4\uBCF4 ${info.totalWalk}m${transferCount > 0 ? ` \xB7 \uD658\uC2B9 ${transferCount}\uD68C` : ""}</div>
        </div>
      </div>
      <div class="route-bar">${barHTML}</div>
      <div class="route-steps">${stepsHTML}</div>
    `;
      card.addEventListener("click", () => showTransitDetail(idx));
      routeList.appendChild(card);
    });
  }
  function renderDrivingList() {
    resultsSection.classList.add("show");
    routeList.innerHTML = "";
    const routes = state.drivingResult.routes;
    routes.forEach((route, idx) => {
      const card = document.createElement("div");
      card.className = "route-card";
      const durationMin = Math.round(route.duration / 60);
      const distanceKm = (route.distance / 1e3).toFixed(1);
      card.innerHTML = `
      <div class="route-card-header">
        <div class="route-time">${durationMin}<span>\uBD84</span></div>
        <div class="route-meta">
          <div class="cost">${distanceKm}km</div>
          <div>\u{1F697} \uC790\uB3D9\uCC28 \uACBD\uB85C</div>
        </div>
      </div>
      <div class="driving-info">
        <span class="driving-tag">\u{1F4CF} ${distanceKm}km</span>
        <span class="driving-tag">\u23F1\uFE0F \uC57D ${durationMin}\uBD84</span>
      </div>
    `;
      card.addEventListener("click", () => showDrivingDetail(idx));
      routeList.appendChild(card);
    });
  }
  var SUBWAY_COLORS = {
    1: "#0052A4",
    2: "#009B3E",
    3: "#EF7C1C",
    4: "#00A2D1",
    5: "#996CAC",
    6: "#CD7C2F",
    7: "#747F00",
    8: "#E6186C",
    9: "#BDB092"
  };
  async function showTransitDetail(idx) {
    const route = state.transitRoutes[idx];
    resultsSection.classList.remove("show");
    routeDetail.classList.add("show");
    const { info } = route;
    const subPaths = route.subPath;
    let html = `
    <div class="detail-summary">
      <div class="total-time">${info.totalTime}<span>\uBD84</span></div>
      <div class="summary-meta">
        <span>\u{1F4B0} ${isEnglish() ? `\u20A9${info.payment.toLocaleString()}` : `${info.payment.toLocaleString()}\uC6D0`}</span>
        <span>\u{1F6B6} ${isEnglish() ? "Walk" : "\uB3C4\uBCF4"} ${info.totalWalk}m</span>
        <span>\u{1F504} ${isEnglish() ? "Transfers" : "\uD658\uC2B9"} ${Math.max(0, info.busTransitCount + info.subwayTransitCount - 1)}${isEnglish() ? "" : "\uD68C"}</span>
      </div>
    </div>
    <div class="timeline">
      <div class="timeline-item">
        <div class="timeline-dot start"></div>
        <div class="timeline-content"><h4>${isEnglish() ? "Start" : "\uCD9C\uBC1C"}</h4><p>${state.startPlace.name}</p></div>
      </div>
  `;
    subPaths.forEach((sp) => {
      if (sp.trafficType === 3 && sp.sectionTime > 0) {
        html += isEnglish() ? `<div class="timeline-item"><div class="timeline-dot walk"></div><div class="timeline-content"><h4>\u{1F6B6} Walk</h4><p>${sp.distance}m \xB7 ~${sp.sectionTime} min</p></div></div>` : `<div class="timeline-item"><div class="timeline-dot walk"></div><div class="timeline-content"><h4>\u{1F6B6} \uB3C4\uBCF4 \uC774\uB3D9</h4><p>${sp.distance}m \xB7 \uC57D ${sp.sectionTime}\uBD84</p></div></div>`;
      } else if (sp.trafficType === 2) {
        const busName = sp.lane?.[0]?.busNo ?? "";
        html += isEnglish() ? `<div class="timeline-item"><div class="timeline-dot bus"></div><div class="timeline-content"><h4>Board at ${sp.startName ?? ""}</h4><div class="transport-info bus">\u{1F68C} ${busName}</div><p>${sp.stationCount ?? 0} stops \xB7 ${sp.sectionTime} min</p><h4 style="margin-top:8px;">Get off at ${sp.endName ?? ""}</h4></div></div>` : `<div class="timeline-item"><div class="timeline-dot bus"></div><div class="timeline-content"><h4>${sp.startName ?? ""} \uC2B9\uCC28</h4><div class="transport-info bus">\u{1F68C} ${busName}</div><p>${sp.stationCount ?? 0}\uAC1C \uC815\uB958\uC7A5 \xB7 ${sp.sectionTime}\uBD84</p><h4 style="margin-top:8px;">${sp.endName ?? ""} \uD558\uCC28</h4></div></div>`;
      } else if (sp.trafficType === 1) {
        const lineName = sp.lane?.[0]?.name ?? "";
        html += isEnglish() ? `<div class="timeline-item"><div class="timeline-dot subway"></div><div class="timeline-content"><h4>Board at ${sp.startName ?? ""}</h4><div class="transport-info subway">\u{1F687} ${lineName}</div><p>${sp.stationCount ?? 0} stations \xB7 ${sp.sectionTime} min</p><h4 style="margin-top:8px;">Get off at ${sp.endName ?? ""}</h4></div></div>` : `<div class="timeline-item"><div class="timeline-dot subway"></div><div class="timeline-content"><h4>${sp.startName ?? ""} \uC2B9\uCC28</h4><div class="transport-info subway">\u{1F687} ${lineName}</div><p>${sp.stationCount ?? 0}\uAC1C \uC5ED \xB7 ${sp.sectionTime}\uBD84</p><h4 style="margin-top:8px;">${sp.endName ?? ""} \uD558\uCC28</h4></div></div>`;
      }
    });
    html += `
      <div class="timeline-item">
        <div class="timeline-dot end"></div>
        <div class="timeline-content"><h4>${isEnglish() ? "Arrive" : "\uB3C4\uCC29"}</h4><p>${state.endPlace.name}</p></div>
      </div>
    </div>
  `;
    detailContent.innerHTML = html;
    await drawTransitRoute(route);
  }
  async function drawTransitRoute(route) {
    clearOverlays();
    const detail = await loadTransitDetail(route.info.mapObj);
    if (detail?.result?.lane) {
      detail.result.lane.forEach((lane) => {
        if (lane.section) {
          lane.section.forEach((section) => {
            if (section.graphPos) {
              const path = section.graphPos.map((p) => new naver.maps.LatLng(p.y, p.x));
              let color = "#999";
              if (lane.class === 1)
                color = SUBWAY_COLORS[lane.type] ?? "#1e88e5";
              else if (lane.class === 2)
                color = "#43a047";
              const polyline = new naver.maps.Polyline({
                map,
                path,
                strokeColor: color,
                strokeOpacity: 0.8,
                strokeWeight: 5,
                strokeLineCap: "round",
                strokeLineJoin: "round"
              });
              state.overlays.push(polyline);
            }
          });
        }
      });
    }
    route.subPath.forEach((sp) => {
      if (sp.trafficType !== 3) {
        const dotColor = sp.trafficType === 1 ? "#1e88e5" : "#43a047";
        if (sp.startX && sp.startY) {
          const m = new naver.maps.Marker({
            map,
            position: new naver.maps.LatLng(sp.startY, sp.startX),
            icon: {
              content: `<div style="width:12px;height:12px;border-radius:50%;background:#fff;border:3px solid ${dotColor};box-shadow:0 1px 6px rgba(0,0,0,0.25);"></div>`,
              anchor: new naver.maps.Point(6, 6)
            }
          });
          state.overlays.push(m);
        }
        if (sp.endX && sp.endY) {
          const m = new naver.maps.Marker({
            map,
            position: new naver.maps.LatLng(sp.endY, sp.endX),
            icon: {
              content: `<div style="width:12px;height:12px;border-radius:50%;background:#fff;border:3px solid ${dotColor};box-shadow:0 1px 6px rgba(0,0,0,0.25);"></div>`,
              anchor: new naver.maps.Point(6, 6)
            }
          });
          state.overlays.push(m);
        }
      }
    });
    fitAllOverlays();
  }
  function showDrivingDetail(idx) {
    const route = state.drivingResult.routes[idx];
    resultsSection.classList.remove("show");
    routeDetail.classList.add("show");
    const durationMin = Math.round(route.duration / 60);
    const distanceKm = (route.distance / 1e3).toFixed(1);
    const steps = route.legs?.[0]?.steps ?? [];
    let stepsHTML = "";
    steps.forEach((step, i) => {
      const stepDist = step.distance >= 1e3 ? `${(step.distance / 1e3).toFixed(1)}km` : `${Math.round(step.distance)}m`;
      const stepTime = Math.round(step.duration / 60);
      const instruction = translateManeuver(step.maneuver?.type ?? "", step.name || "");
      stepsHTML += `
      <div class="timeline-item">
        <div class="timeline-dot ${i === 0 ? "start" : i === steps.length - 1 ? "end" : "walk"}"></div>
        <div class="timeline-content">
          <h4>${instruction}</h4>
          <p>${stepDist} \xB7 \uC57D ${stepTime > 0 ? stepTime + "\uBD84" : "1\uBD84 \uBBF8\uB9CC"}</p>
        </div>
      </div>
    `;
    });
    detailContent.innerHTML = `
    <div class="detail-summary driving">
      <div class="total-time">${durationMin}<span>\uBD84</span></div>
      <div class="summary-meta">
        <span>\u{1F4CF} ${distanceKm}km</span>
        <span>\u{1F697} ${isEnglish() ? "Driving route" : "\uC790\uB3D9\uCC28 \uACBD\uB85C"}</span>
      </div>
    </div>
    <div class="driving-detail-info">
      <h3>\u{1F697} ${isEnglish() ? "Turn-by-turn" : "\uD134\uBC14\uC774\uD134 \uC548\uB0B4"}</h3>
      <p>${isEnglish() ? "From" : "\uCD9C\uBC1C"}: ${state.startPlace.name}</p>
      <p>${isEnglish() ? "To" : "\uB3C4\uCC29"}: ${state.endPlace.name}</p>
    </div>
    <div class="timeline" style="margin-top:16px;">
      ${stepsHTML}
    </div>
  `;
    drawDrivingRoute(route);
  }
  function translateManeuver(type, road) {
    const roadLabel = road ? ` (${road})` : "";
    const maneuvers = {
      "depart": "\u{1F697} \uCD9C\uBC1C",
      "arrive": "\u{1F3C1} \uB3C4\uCC29",
      "turn": "\u21AA\uFE0F \uD68C\uC804",
      "new name": "\u27A1\uFE0F \uC9C1\uC9C4",
      "merge": "\u{1F500} \uD569\uB958",
      "on ramp": "\u2B06\uFE0F \uC9C4\uC785\uB85C",
      "off ramp": "\u2B07\uFE0F \uCD9C\uAD6C",
      "fork": "\u{1F500} \uBD84\uAE30",
      "end of road": "\u{1F51A} \uB3C4\uB85C \uB05D",
      "continue": "\u27A1\uFE0F \uC9C1\uC9C4",
      "roundabout": "\u{1F504} \uD68C\uC804\uAD50\uCC28\uB85C",
      "rotary": "\u{1F504} \uB85C\uD130\uB9AC",
      "roundabout turn": "\u{1F504} \uD68C\uC804\uAD50\uCC28\uB85C \uD68C\uC804",
      "notification": "\u2139\uFE0F \uC54C\uB9BC",
      "exit roundabout": "\u2197\uFE0F \uD68C\uC804\uAD50\uCC28\uB85C \uB098\uAC00\uAE30"
    };
    return (maneuvers[type] ?? `\u27A1\uFE0F ${type}`) + roadLabel;
  }
  function drawDrivingRoute(route) {
    clearOverlays();
    const geojson = route.geometry;
    if (!geojson?.coordinates?.length)
      return;
    const path = geojson.coordinates.map((c) => new naver.maps.LatLng(c[1], c[0]));
    const polyline = new naver.maps.Polyline({
      map,
      path,
      strokeColor: "#4285f4",
      strokeOpacity: 0.9,
      strokeWeight: 6,
      strokeLineCap: "round",
      strokeLineJoin: "round"
    });
    state.overlays.push(polyline);
    fitAllOverlays();
  }
  function fitAllOverlays() {
    const bounds = new naver.maps.LatLngBounds();
    let count = 0;
    const extend = (lat, lng) => {
      bounds.extend(new naver.maps.LatLng(lat, lng));
      count += 1;
    };
    if (state.startPlace)
      extend(state.startPlace.lat, state.startPlace.lng);
    if (state.endPlace)
      extend(state.endPlace.lat, state.endPlace.lng);
    state.overlays.forEach((o) => {
      if (o && typeof o.getPath === "function") {
        const path = o.getPath();
        if (path && typeof path.getLength === "function") {
          for (let i = 0; i < path.getLength(); i++) {
            const p = path.getAt(i);
            extend(p.lat(), p.lng());
          }
        }
        return;
      }
      if (o && typeof o.getPosition === "function") {
        const p = o.getPosition();
        if (p)
          extend(p.lat(), p.lng());
      }
    });
    if (count > 1) {
      map.fitBounds(bounds, { top: 50, right: 50, bottom: 50, left: 50 });
    }
  }
  function detectAutoLang() {
    const lang = (navigator.language || "").toLowerCase();
    if (lang.startsWith("ko"))
      return 0;
    if (lang.startsWith("ja"))
      return 2;
    if (lang.startsWith("zh")) {
      if (lang.includes("tw") || lang.includes("hant") || lang.includes("hk"))
        return 4;
      return 3;
    }
    if (lang.startsWith("vi"))
      return 5;
    return 1;
  }
  function setLang(next) {
    state.lang = next;
    state.lastSearchKeyword = { start: "", end: "" };
    startList.classList.remove("show");
    endList.classList.remove("show");
    resultsSection.classList.remove("show");
    routeDetail.classList.remove("show");
    clearOverlays();
  }
  if (langSelect) {
    setLang(1);
    langSelect.value = "1";
    langSelect.addEventListener("change", () => {
      const v = langSelect.value;
      if (v === "auto")
        setLang(detectAutoLang());
      else
        setLang(Number(v));
    });
  } else {
    setLang(1);
  }
  setupAutocomplete(startInput, startList, "start");
  setupAutocomplete(endInput, endList, "end");
  clearStart.addEventListener("click", () => {
    state.startPlace = null;
    startInput.value = "";
    startInfo.textContent = "";
    state.lastSearchKeyword.start = "";
    updateMarkers();
    checkSearchReady();
  });
  clearEnd.addEventListener("click", () => {
    state.endPlace = null;
    endInput.value = "";
    endInfo.textContent = "";
    state.lastSearchKeyword.end = "";
    updateMarkers();
    checkSearchReady();
  });
  swapBtn.addEventListener("click", () => {
    const temp = state.startPlace;
    state.startPlace = state.endPlace;
    state.endPlace = temp;
    startInput.value = state.startPlace?.name ?? "";
    endInput.value = state.endPlace?.name ?? "";
    startInfo.textContent = state.startPlace ? `\u{1F4CD} ${state.startPlace.lat.toFixed(5)}, ${state.startPlace.lng.toFixed(5)}` : "";
    endInfo.textContent = state.endPlace ? `\u{1F4CD} ${state.endPlace.lat.toFixed(5)}, ${state.endPlace.lng.toFixed(5)}` : "";
    updateMarkers();
    checkSearchReady();
  });
  searchBtn.addEventListener("click", doSearch);
  [startInput, endInput].forEach((input) => {
    input.addEventListener("keydown", (e) => {
      if (e.key === "Enter" && state.startPlace && state.endPlace)
        doSearch();
    });
  });
  tabTransit.addEventListener("click", () => {
    state.mode = "transit";
    tabTransit.classList.add("active");
    tabDriving.classList.remove("active");
    resultsSection.classList.remove("show");
    routeDetail.classList.remove("show");
    clearOverlays();
  });
  tabDriving.addEventListener("click", () => {
    state.mode = "driving";
    tabDriving.classList.add("active");
    tabTransit.classList.remove("active");
    resultsSection.classList.remove("show");
    routeDetail.classList.remove("show");
    clearOverlays();
  });
  modeStart.addEventListener("click", () => {
    state.clickMode = "start";
    modeStart.classList.add("active");
    modeEnd.classList.remove("active");
  });
  modeEnd.addEventListener("click", () => {
    state.clickMode = "end";
    modeEnd.classList.add("active");
    modeStart.classList.remove("active");
  });
  naver.maps.Event.addListener(map, "click", (e) => {
    const lat = e.coord.lat();
    const lng = e.coord.lng();
    selectPlaceByCoord(state.clickMode, lat, lng);
    if (state.clickMode === "start" && !state.endPlace) {
      state.clickMode = "end";
      modeEnd.classList.add("active");
      modeStart.classList.remove("active");
    }
  });
  backBtn.addEventListener("click", () => {
    routeDetail.classList.remove("show");
    resultsSection.classList.add("show");
    clearOverlays();
  });
  panelToggle.addEventListener("click", () => {
    sidePanel.classList.toggle("open");
  });
  searchBtn.disabled = true;
  console.log("\u{1F5FA}\uFE0F App loaded! (Naver Maps + ODsay + OSRM)");
})();
