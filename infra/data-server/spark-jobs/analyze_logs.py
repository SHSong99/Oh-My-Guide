from pyspark.sql import SparkSession
from pyspark.sql.functions import col, count, dense_rank, when, sum as spark_sum, monotonically_increasing_id
from pyspark.sql.window import Window
import os
import sys

# ===== 설정 =====
# Livy args로 전달된 경우 sys.argv 사용, 없으면 환경변수, 없으면 기본값
if len(sys.argv) >= 6:
    DB_HOST     = sys.argv[1]
    DB_PORT     = sys.argv[2]
    DB_NAME     = sys.argv[3]
    DB_USER     = sys.argv[4]
    DB_PASSWORD = sys.argv[5]
else:
    DB_HOST     = os.environ.get('DB_HOST', 'j14e103.p.ssafy.io')
    DB_PORT     = os.environ.get('DB_PORT', '5432')
    DB_NAME     = os.environ.get('DB_NAME', 'ohmyguide')
    DB_USER     = os.environ.get('DB_USER', 'admin')
    DB_PASSWORD = os.environ.get('DB_PASSWORD', 'admin1234')

# 1. Spark 세션 생성 (Spark 프로그램의 시작점)
spark = SparkSession.builder \
    .appName("TravelLogAnalysis") \
    .config("spark.jars", "/opt/spark-jobs/postgresql-42.7.4.jar") \
    .config("spark.hadoop.dfs.client.use.datanode.hostname", "false") \
    .getOrCreate()

# 2. HDFS에서 모든 날짜의 CSV 로그 읽기
#    /*/* = 모든 날짜 폴더 / 모든 파일
df = spark.read.csv("hdfs://namenode:9000/user-logs/*/*", header=False)

# 3. 컬럼 이름 지정 (CSV는 헤더가 없으므로 직접 지정)
df = df.toDF(
    "user_id", "nationality", "age", "gender",
    "travel_purpose", "lifestyle", "action", "place_id", "timestamp"
)

# 4. 타입 변환 (CSV에서 읽으면 전부 문자열이라 숫자로 변환)
df = df.withColumn("age", col("age").cast("int"))
df = df.withColumn("place_id", col("place_id").cast("long"))

# 5. 연령대 생성 (25살 → "20s", 32살 → "30s", 70세 이상 → "70s+")
df = df.withColumn("age_group",
    when(col("age") < 20, "10s")
    .when(col("age") < 30, "20s")
    .when(col("age") < 40, "30s")
    .when(col("age") < 50, "40s")
    .when(col("age") < 60, "50s")
    .when(col("age") < 70, "60s")
    .otherwise("70s+")
)

# 6. 행동별 가중치 부여 (VIEW=1, RE_RECOMMEND=2, GO=3)
df = df.withColumn("score",
    when(col("action") == "GO", 3)
    .when(col("action") == "RE_RECOMMEND", 2)
    .otherwise(1)
)

# 7. 군집별 장소 점수 합산
#    같은 군집(국적+연령대+성별+여행목적)의 같은 장소끼리 묶어서
#    방문 횟수와 총 점수를 계산
grouped = df.groupBy(
    "nationality", "age_group", "gender",
    "travel_purpose", "place_id"
).agg(
    count("*").alias("visit_count"),           # 몇 번 봤/갔는지
    spark_sum("score").alias("total_score")    # 가중치 합산 점수
)

# 8. 군집 내에서 점수 높은 순으로 순위 매기기
#    dense_rank: 공동 1위가 있으면 다음은 2위 (1,1,2,3 순)
window = Window.partitionBy(
    "nationality", "age_group", "gender",
    "travel_purpose"
).orderBy(col("total_score").desc())

ranked = grouped.withColumn("place_rank", dense_rank().over(window))

# 9. 상위 5개 장소만 선택
top5 = ranked.filter(col("place_rank") <= 5)

# 9-1. JPA 엔티티용 id 컬럼 추가
#      Spark가 overwrite할 때 id가 없으면 Spring Boot API에서 500 에러
top5 = top5.withColumn("id", monotonically_increasing_id())

# 10. 결과를 PostgreSQL에 저장
jdbc_url = f"jdbc:postgresql://{DB_HOST}:{DB_PORT}/{DB_NAME}"

top5.write \
    .format("jdbc") \
    .option("url", jdbc_url) \
    .option("dbtable", "popular_places") \
    .option("user", DB_USER) \
    .option("password", DB_PASSWORD) \
    .option("driver", "org.postgresql.Driver") \
    .mode("overwrite") \
    .save()

print("=== 분석 완료! popular_places 테이블에 저장됨 ===")

spark.stop()