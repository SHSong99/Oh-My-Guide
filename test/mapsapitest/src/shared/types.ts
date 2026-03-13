// ═══════════════════════════════════════════
// 공유 타입 정의 (서버 & 클라이언트)
// ═══════════════════════════════════════════

/** 장소 정보 */
export interface Place {
  name: string;
  lat: number;
  lng: number;
  type: string;
  id?: number;
}

/** 검색 결과 응답 */
export interface SearchResponse {
  results: Place[];
}

/** 경로 검색 요청 파라미터 */
export interface RouteQuery {
  sx: string;
  sy: string;
  ex: string;
  ey: string;
}

/** ODsay 노선 정보 */
export interface Lane {
  busNo?: string;
  name?: string;
  subwayCode?: number;
  type?: number;
  class?: number;
  busID?: number;
  subwayID?: number;
}

/** ODsay 경로 하위 구간 */
export interface SubPath {
  trafficType: number; // 1: 지하철, 2: 버스, 3: 도보
  distance: number;
  sectionTime: number;
  stationCount?: number;
  lane?: Lane[];
  startName?: string;
  endName?: string;
  startX?: number;
  startY?: number;
  endX?: number;
  endY?: number;
  startID?: number;
  endID?: number;
  way?: string;
  wayCode?: number;
  door?: string;
  startExitNo?: string;
  endExitNo?: string;
}

/** ODsay 경로 정보 */
export interface RouteInfo {
  totalTime: number;
  payment: number;
  busTransitCount: number;
  subwayTransitCount: number;
  mapObj: string;
  firstStartStation: string;
  lastEndStation: string;
  totalStationCount: number;
  busStationCount: number;
  subwayStationCount: number;
  totalDistance: number;
  totalWalk: number;
  checkIntervalTime?: number;
  checkIntervalTimeOver498?: number;
  pathType: number;
}

/** ODsay 경로 */
export interface RoutePath {
  pathType: number;
  info: RouteInfo;
  subPath: SubPath[];
}

/** ODsay 경로 검색 결과 */
export interface RouteResult {
  searchType: number;
  outTrafficCheck: number;
  busCount: number;
  subwayCount: number;
  subwayBusCount: number;
  pointDistance: number;
  startRadius: number;
  endRadius: number;
  path: RoutePath[];
}

/** 경로 상세 좌표 */
export interface GraphPos {
  x: number;
  y: number;
}

export interface LaneSection {
  graphPos: GraphPos[];
}

export interface DetailLane {
  class: number;
  type: number;
  section: LaneSection[];
}

export interface LoadLaneResult {
  result: {
    lane: DetailLane[];
  };
}

/** 에러 응답 */
export interface ErrorResponse {
  error: string;
}
