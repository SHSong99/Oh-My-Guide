from pyspark.sql import SparkSession

spark = SparkSession.builder \
    .appName("HelloE103") \
    .getOrCreate()

print("=" * 40)
print("hello e103!!")
print("=" * 40)

spark.stop()
