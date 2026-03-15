from pyspark.sql import SparkSession

spark = SparkSession.builder \
    .appName("HelloE103") \
    .getOrCreate()

print("=" * 40)
print("hello 103 word!!")
print("=" * 40)

spark.stop()
