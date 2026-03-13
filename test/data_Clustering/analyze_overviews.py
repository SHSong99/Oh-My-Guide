"""
Oh! My Guide - overview 텍스트 형태소 분석 & 무드 차원 도출

[사용법]
1. collect_overviews.py로 수집한 tourapi_overviews.csv 준비
2. pip install pandas konlpy scikit-learn matplotlib wordcloud
3. python analyze_overviews.py

[분석 내용]
1. 형태소 분석 (KoNLPy Okt) → 명사+형용사 추출
2. 관광 분위기 관련 키워드 빈도 분석
3. TF-IDF 기반 키워드 클러스터링 → 자연스러운 차원 도출
4. 결과를 CSV + 시각화로 저장
"""

import pandas as pd
import numpy as np
from collections import Counter
import re
import os

# ── KoNLPy 임포트 (없으면 안내) ──
try:
    from konlpy.tag import Okt
    okt = Okt()
except ImportError:
    print("❌ KoNLPy가 필요합니다: pip install konlpy")
    print("   (Java JDK 8+ 도 필요합니다)")
    exit(1)

try:
    from sklearn.feature_extraction.text import TfidfVectorizer
    from sklearn.cluster import KMeans
except ImportError:
    print("❌ scikit-learn이 필요합니다: pip install scikit-learn")
    exit(1)


INPUT_FILE = "tourapi_overviews.csv"
OUTPUT_DIR = "analysis_results"


def load_data() -> pd.DataFrame:
    if not os.path.exists(INPUT_FILE):
        print(f"❌ {INPUT_FILE} 파일이 없습니다.")
        print("   collect_overviews.py를 먼저 실행하세요.")
        exit(1)

    df = pd.read_csv(INPUT_FILE)
    df = df.dropna(subset=["overview"])
    df = df[df["overview"].str.len() >= 20]
    print(f"✅ 데이터 로드: {len(df)}건 (overview 있는 것만)")
    return df


def extract_keywords(text: str) -> list[str]:
    """형태소 분석 → 명사 + 형용사 추출"""
    # HTML 태그 제거
    text = re.sub(r"<[^>]+>", " ", str(text))
    text = re.sub(r"[^\w\sㄱ-ㅎㅏ-ㅣ가-힣]", " ", text)

    try:
        # Okt: 명사(Noun) + 형용사(Adjective) 추출
        morphs = okt.pos(text, stem=True)
        keywords = [
            word for word, pos in morphs
            if pos in ("Noun", "Adjective") and len(word) >= 2
        ]
        return keywords
    except Exception:
        return []


def analyze_mood_keywords(df: pd.DataFrame) -> pd.DataFrame:
    """
    분위기 관련 키워드 빈도 분석

    사전 정의한 분위기 시드 키워드를 기준으로
    실제 overview에서 얼마나 등장하는지 빈도를 측정
    """

    # 분위기 시드 키워드 (이걸로 실제 빈도를 측정)
    mood_seeds = {
        "힐링/여유": ["힐링", "여유", "조용", "평화", "휴식", "산책", "고요", "느긋", "명상", "치유", "쉼", "안식", "편안", "느림"],
        "활동적/에너지": ["활동", "체험", "놀이", "레저", "스포츠", "즐기", "신나", "에너지", "다이나믹", "흥미진진"],
        "감성/포토": ["감성", "사진", "포토", "예쁘", "아름답", "인스타", "풍경", "뷰", "전망", "멋지", "그림같", "경치", "절경", "비경"],
        "로맨틱/데이트": ["로맨틱", "데이트", "연인", "커플", "분위기", "낭만", "둘이", "사랑"],
        "미식/맛집": ["맛집", "맛있", "음식", "먹거리", "특산물", "향토", "식도락", "미식", "별미", "먹방", "맛", "요리"],
        "역사/학습": ["역사", "유적", "문화재", "전통", "교육", "학습", "박물관", "전시", "유산", "문화", "고궁", "사찰", "서원"],
        "모험/스릴": ["모험", "스릴", "짜릿", "익스트림", "도전", "절벽", "래프팅", "번지", "짚라인", "서핑", "다이빙"],
        "가족/어린이": ["가족", "어린이", "아이", "아이들", "놀이터", "키즈", "체험학습", "나들이", "소풍", "유아", "어린"],
        "야경/야간": ["야경", "야간", "밤", "조명", "불빛", "야시장", "일몰", "석양", "노을", "달빛", "별", "야행"],
        "현지/로컬": ["로컬", "현지", "시장", "전통시장", "재래시장", "골목", "마을", "동네", "토속", "향토", "주민"],
    }

    print("\n" + "=" * 60)
    print("분위기 키워드 빈도 분석")
    print("=" * 60)

    results = []
    total_docs = len(df)

    for mood, seeds in mood_seeds.items():
        # 해당 키워드가 overview에 포함된 문서 수
        doc_count = 0
        total_mentions = 0
        keyword_counts = Counter()

        for _, row in df.iterrows():
            text = str(row["overview"])
            found_any = False
            for seed in seeds:
                count = text.count(seed)
                if count > 0:
                    keyword_counts[seed] += count
                    total_mentions += count
                    found_any = True
            if found_any:
                doc_count += 1

        pct = (doc_count / total_docs) * 100
        top_keywords = keyword_counts.most_common(5)

        results.append({
            "mood": mood,
            "doc_count": doc_count,
            "doc_pct": round(pct, 1),
            "total_mentions": total_mentions,
            "top_keywords": ", ".join(f"{k}({v})" for k, v in top_keywords),
        })

        print(f"\n  [{mood}]")
        print(f"    문서 등장률: {doc_count}/{total_docs} ({pct:.1f}%)")
        print(f"    총 언급 횟수: {total_mentions}")
        print(f"    상위 키워드: {top_keywords}")

    results_df = pd.DataFrame(results)
    results_df = results_df.sort_values("doc_count", ascending=False)
    return results_df


def analyze_global_keywords(df: pd.DataFrame, top_n: int = 200) -> pd.DataFrame:
    """전체 overview에서 가장 많이 등장하는 키워드 Top N"""
    print("\n" + "=" * 60)
    print(f"전체 키워드 빈도 Top {top_n}")
    print("=" * 60)

    all_keywords = Counter()
    sample = df.head(min(len(df), 10000))  # 10000건 샘플 (속도)

    for i, (_, row) in enumerate(sample.iterrows()):
        keywords = extract_keywords(row["overview"])
        all_keywords.update(keywords)
        if (i + 1) % 1000 == 0:
            print(f"  형태소 분석 진행: {i+1}/{len(sample)}")

    # 불용어 제거
    stopwords = {
        "것", "수", "등", "이", "때", "곳", "중", "내", "및", "더", "약",
        "년", "월", "일", "위", "또한", "가지", "대한", "통해", "현재",
        "있다", "하다", "되다", "이다", "않다", "없다", "같다",
        "지역", "주변", "근처", "부근", "인근", "도로", "방면",
    }
    filtered = {k: v for k, v in all_keywords.items() if k not in stopwords}

    top_keywords = Counter(filtered).most_common(top_n)
    top_df = pd.DataFrame(top_keywords, columns=["keyword", "count"])

    print(f"\n  분석 완료: 고유 키워드 {len(filtered)}개")
    print(f"\n  상위 30개:")
    for kw, cnt in top_keywords[:30]:
        print(f"    {kw}: {cnt}")

    return top_df


def cluster_keywords(df: pd.DataFrame, n_clusters: int = 10):
    """TF-IDF 기반 문서 클러스터링 → 자연스러운 분위기 그룹 도출"""
    print("\n" + "=" * 60)
    print(f"TF-IDF 클러스터링 (K={n_clusters})")
    print("=" * 60)

    sample = df.head(min(len(df), 10000))

    # 형태소 분석 후 공백 결합
    texts = []
    for _, row in sample.iterrows():
        keywords = extract_keywords(row["overview"])
        texts.append(" ".join(keywords))

    # TF-IDF
    vectorizer = TfidfVectorizer(max_features=1000, min_df=5, max_df=0.8)
    tfidf_matrix = vectorizer.fit_transform(texts)
    feature_names = vectorizer.get_feature_names_out()

    # KMeans 클러스터링
    kmeans = KMeans(n_clusters=n_clusters, random_state=42, n_init=10)
    clusters = kmeans.fit_predict(tfidf_matrix)

    # 클러스터별 상위 키워드
    cluster_results = []
    for i in range(n_clusters):
        center = kmeans.cluster_centers_[i]
        top_indices = center.argsort()[::-1][:15]
        top_words = [(feature_names[j], round(center[j], 3)) for j in top_indices]
        cluster_size = int(np.sum(clusters == i))

        cluster_results.append({
            "cluster": i,
            "size": cluster_size,
            "top_keywords": ", ".join(f"{w}({s})" for w, s in top_words),
        })

        print(f"\n  [클러스터 {i}] ({cluster_size}건)")
        print(f"    핵심 키워드: {', '.join(w for w, s in top_words[:10])}")

    return pd.DataFrame(cluster_results)


def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # 1. 데이터 로드
    df = load_data()

    # 2. 분위기 키워드 빈도 분석
    mood_df = analyze_mood_keywords(df)
    mood_df.to_csv(f"{OUTPUT_DIR}/mood_keyword_frequency.csv", index=False, encoding="utf-8-sig")
    print(f"\n  → {OUTPUT_DIR}/mood_keyword_frequency.csv 저장됨")

    # 3. 전체 키워드 빈도
    global_df = analyze_global_keywords(df)
    global_df.to_csv(f"{OUTPUT_DIR}/global_keyword_top200.csv", index=False, encoding="utf-8-sig")
    print(f"  → {OUTPUT_DIR}/global_keyword_top200.csv 저장됨")

    # 4. 클러스터링
    cluster_df = cluster_keywords(df)
    cluster_df.to_csv(f"{OUTPUT_DIR}/keyword_clusters.csv", index=False, encoding="utf-8-sig")
    print(f"  → {OUTPUT_DIR}/keyword_clusters.csv 저장됨")

    # 5. 요약
    print("\n" + "=" * 60)
    print("분석 완료!")
    print("=" * 60)
    print(f"\n생성된 파일:")
    print(f"  1. {OUTPUT_DIR}/mood_keyword_frequency.csv  ← 10개 무드별 빈도")
    print(f"  2. {OUTPUT_DIR}/global_keyword_top200.csv    ← 전체 키워드 Top200")
    print(f"  3. {OUTPUT_DIR}/keyword_clusters.csv         ← 10개 클러스터 결과")
    print(f"\n이 파일들을 Claude에 업로드하면 최종 차원 선정 근거를 정리해드립니다.")


if __name__ == "__main__":
    main()
