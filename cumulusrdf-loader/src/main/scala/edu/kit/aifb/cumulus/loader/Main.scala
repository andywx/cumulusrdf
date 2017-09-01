package edu.kit.aifb.cumulus.loader

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.cassandra._

import com.datastax.spark.connector._
import org.apache.hadoop.io.LongWritable
import org.apache.jena.hadoop.rdf.types.TripleWritable
import org.apache.hadoop.conf.Configuration
import org.apache.jena.hadoop.rdf.io.input.TriplesInputFormat

object Main {
  
  def main(args: Array[String]) {
    
    val conf = new SparkConf(true)
      .setAppName("TestSparkCassandra")
      .setMaster("spark://node01:7077")
      .set("spark.cassandra.connection.host", "node01")
      .registerKryoClasses(
          Array(
              classOf[LongWritable],
              classOf[TripleWritable]))
    
      
    val sc = new SparkContext(conf)
    
    val hadoopConf = new Configuration()
    
    val triplesInput = sc.newAPIHadoopFile(
        //args(0),
        "hdfs://node01:9000/user/xinwang/mappingbased_objects_en.ttl",
        classOf[TriplesInputFormat],
        classOf[LongWritable],
        classOf[TripleWritable],
        hadoopConf)
        
    // RDD of triples
    val triples = triplesInput.map(pair => (pair._2.get.getSubject.getURI, 
        pair._2.get.getPredicate.getURI, 
        pair._2.get.getObject.getURI))
    
    triples.take(10).foreach(println)
    
//    val rdd = sc.cassandraTable("test", "kv")
//    
//    rdd.collect().foreach(println)
    
    triples.saveToCassandra("test", "spo", SomeColumns("s", "p", "o"))
    
  }
}