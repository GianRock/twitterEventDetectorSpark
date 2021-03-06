package com.rock.twitterEventDetector.mongoSpark

import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.{BasicDBObject, DBObject, MongoException}
import com.rock.twitterEventDetector.model.Model.{Tweet, DbpediaAnnotation}
import com.rock.twitterEventDetector.configuration.Constant._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.JavaConverters._

/**
  * Created by rocco on 03/02/2016.
  */
object DbpediaAnnotationCollection {
  def inserDbpediaAnnotationsBulk(  annotations:Iterator[(Long, Option[List[DbpediaAnnotation]])]  )={


    val collection=MongoCLientSingleton.clientMongo(MONGO_DB_NAME).getCollection("dbpediaAnnotations")
    val bulkWrites=collection.initializeUnorderedBulkOperation()
     annotations.foreach(annotation=>{
       bulkWrites.insert(MongoDBObject("_id"->annotation._1,"annotations"->annotation._2.get.map(ann=>ann.toMaps)))
    })

    println(" ADDING "+annotations.size+ " annotations to db")
    bulkWrites.execute()


  }

  def inserDbpediaAnnotationsBulk2(  annotations:Iterator[(Long, List[DbpediaAnnotation])])={
  println(" sto aggiungendo ")

    val collection=MongoCLientSingleton.clientMongo(MONGO_DB_NAME).getCollection("dbpediaAnnotations")
    val bulkWrites=collection.initializeUnorderedBulkOperation()
    annotations.foreach(annotation=>{
      bulkWrites.insert(MongoDBObject("_id"->annotation._1,"annotations"->annotation._2.map(ann=>ann.toMaps)))
    })

    println(" ADDING "+annotations.size+ " annotations to db")
    bulkWrites.execute()


  }

  /**
    *
    * @param annotations
    */
  def inserDbpediaAnnotations(  annotations:Iterator[(Long, Option[List[DbpediaAnnotation]])])={


    val collection=MongoCLientSingleton.clientMongo(MONGO_DB_NAME).getCollection("dbpediaAnnotations")
     annotations.foreach(annotation=>{
       try{
       collection.insert(MongoDBObject("_id"->annotation._1,"annotations"->annotation._2.get.map(ann=>ann.toMaps)))

       }catch {
         case foo: MongoException => println(foo)
       }
    })



  }

  def insertDbpediaAnnotationsOfTweet(idTweet:Long,annotations:List[DbpediaAnnotation])={

    val collection=MongoCLientSingleton.clientMongo(MONGO_DB_NAME).getCollection("dbpediaAnnotations")
    try{
      collection.insert(MongoDBObject("_id"->idTweet,"annotations"->annotations.map(annotation=>annotation.toMaps)))

    }catch {
      case foo: MongoException => println(foo)
    }


  }

  /**
    * retrive the annotations of tweet
    * it will reurn Some(of the list made of Dbpedia Annotations object]
    * None if the tweet isn't altready annotated through dbpedia Spootlight
    *
    * @param idTweet
    * @return
    */
  def getAnnotationsOfTweet(idTweet:Long):Option[List[DbpediaAnnotation]]={


    val collection=MongoCLientSingleton.clientMongo(MONGO_DB_NAME).getCollection("dbpediaAnnotations")
    val result: DBObject =collection.find(MongoDBObject("_id"->idTweet)).one()
    if(result==null){
      None
    }else{
      val annotations=result.get("annotations").asInstanceOf[java.util.List[BasicDBObject]].asScala.map(bson=>new DbpediaAnnotation(bson)).toList
      annotations.foreach(println)
      Some(annotations)
    }
  }




  def main(args: Array[String]) {
    val annotations=DbpediaAnnotationCollection.getAnnotationsOfTweet(33l)
    println(annotations)

  val ids=List(256121411834368000L,256121411851128832L,256230354388660225L,256121411859525632L)
    val sparkConf = new SparkConf()
      .setAppName("LSH")
      .setMaster("local[*]")
    val sc = new SparkContext("local", "SparkExample", sparkConf)

   val tweetsRDD: RDD[Tweet] = sc.parallelize(ids).flatMap(id=>TweetCollection.findTweetById(id))
    tweetsRDD.collect().foreach(println)


  }


}
