import org.apache.spark.sql.{SaveMode, SparkSession}

/**
  * Data: http://www.nyc.gov/html/tlc/html/about/trip_record_data.shtml
  */
object NYCTaxi {

  val spark: SparkSession = SparkSession.builder
    .appName(this.getClass.getName)
    .master("local[1]") // force single thread usage for fair comparison to DataFusion
    .getOrCreate()

  def main(arg: Array[String]): Unit = {

//    load_csv()

    load_parquet()

    //    test(spark, "SELECT COUNT(1) FROM tripdata")

    //    test(spark, "SELECT passenger_count, COUNT(1) " +
    //      "FROM tripdata " +
    //      "GROUP BY passenger_count")

        test(spark, "SELECT passenger_count, COUNT(1), MIN(CAST(fare_amount AS FLOAT)), MAX(CAST(fare_amount AS FLOAT)) " +
          "FROM tripdata " +
          "GROUP BY passenger_count")

    /*
[7,44,-70.0,77.75]
[3,425351,-169.0,550.0]
[8,36,0.8,100.0]
[0,58353,0.0,655.35]
[5,443643,-59.7,850.0]
[6,268944,-52.0,297.5]
[9,30,-9.0,99.99]
[1,6647126,-340.0,391911.78]
[4,217875,-60.0,590.0]
[2,1446874,-340.0,3029.0]
     */

    // 12 seconds CSV
    // 2 seconds Parquet
  }

  def load_csv() {

    val df = spark.read
      .option("header", "true")
      .option("inferSchema", "true")
      .csv("/mnt/ssd/nyc_taxis/yellow_tripdata_2017-12.csv")
    df.printSchema()

    df.createOrReplaceTempView("tripdata")
  }

  def load_parquet() {
    val df = spark.read
      .parquet("/mnt/ssd/nyc_taxis/parquet/yellow_tripdata_2017-12")
    df.printSchema()

    df.createOrReplaceTempView("tripdata")
  }

  def test(spark: SparkSession, sql: String): Unit = {
    println(sql)
    val start = System.currentTimeMillis()
    val df2 = spark.sql(sql)
    df2.collect().foreach(println)
    val end = System.currentTimeMillis()
    println(s"Took ${end-start} ms")
    println("--")
  }

}
