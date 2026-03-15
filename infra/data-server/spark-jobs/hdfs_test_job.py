from pyspark.sql import SparkSession

spark = SparkSession.builder \
    .appName("HDFSTest") \
    .config("spark.hadoop.fs.defaultFS", "hdfs://namenode:9000") \
    .getOrCreate()

sc = spark.sparkContext

# HDFS에 테스트 데이터 쓰기
test_data = ["hello from spark", "hdfs connection test", "e103 team"]
rdd = sc.parallelize(test_data)
rdd.saveAsTextFile("hdfs://namenode:9000/test/spark-hdfs-test")

print("=" * 40)
print("HDFS write success!")
print("=" * 40)

# HDFS에서 데이터 읽기
read_rdd = sc.textFile("hdfs://namenode:9000/test/spark-hdfs-test")
lines = read_rdd.collect()

print("=" * 40)
print("HDFS read success!")
for line in lines:
    print(line)
print("=" * 40)

spark.stop()
