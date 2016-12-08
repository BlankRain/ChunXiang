
/**
  * Created by Administrator on 2016/12/7.
  */

import java.io._

import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Element
import play.api.libs.json._

import scala.util.Try
import scalaj.http.Http

object Felicia extends App {
  println("Nice to meet you!~")
  dianPu()
  caiDan()

  def dianPu(): Unit = {
    val geoIdlist = scala.io.Source.fromFile("geo_id.txt", "utf8").getLines().toList
    for (geoIdLine <- geoIdlist) {
      val geoId = geoIdLine.split(",")(0)
      val geoName = geoIdLine.split(",")(1)
      val pass = Try(geoIdLine.split(",")(2)).toOption
      println(geoId)
      if (pass == None) {
        pollAllData(geoId, geoName)
      } else {
        println(s"$geoId passed")
      }
    }
  }

  def caiDan() = {

    val geoIdlist = scala.io.Source.fromFile("geo_id.txt", "utf8")
      .getLines().toList.filter(_.contains(",pass"))
    for (geoIdLine <- geoIdlist) {
      println(geoIdLine )
      val geoId = geoIdLine.split(",")(0)
      val l = scala.io.Source.fromFile(s"./data/$geoId", "utf8").getLines.toList
      //      (177653,"德克士（阳阳国际）",108934808,34218031,"西安雁塔西路158号祥瑞大厦")
      val Pattern =
        """\((\d+),(.*)""".r
      val caiDanList = l.map(x => x match {
        case Pattern(y, b) => y
      })
      for (id <- caiDanList) {
        val p = commentCount(id)
        Thread.sleep(1000)
        val resList = restaurant(id)
        val d = resList.map { (x) => {
          x match {
            case ((mon, caijson), index) => {
              val c = Try(Json.parse(caijson)).toOption
              c match {
                case Some(cai) => {

                  val a = cai \ "id" get
                  val b = cai \ "name" get
                  val v = (id, p, a, b, mon)
                  v
                }
                case None => ""
              }
            }

            case _ => println("oh,no")
          }
        }
        }
        appendUtf8(s"./data/caidan/$geoId")(d)

        Thread.sleep(2000)

      }
    }

  }


  def pollAllData(geoId: String, geoName: String) = {
    val data = scala.collection.mutable.ListBuffer[(JsValue, JsValue, JsValue, JsValue, JsValue)]()
    var startOff = 1
    val pageSize = 20
    val twoSeconds = 2000
    val (m, d) = pollData(geoId, geoName, startOff, pageSize)
    appendUtf8(s"./data/$geoId")(d)
    data.:+(d)
    var flag = m
    while (flag == true) {
      startOff = pageSize + startOff
      Thread.sleep(twoSeconds)
      val (m, d) = pollData(geoId, geoName, startOff, pageSize)
      flag = m
      appendUtf8(s"./data/$geoId")(d)
      data.:+(d)
    }
    data.toList
  }

  def appendUtf8(fileName: String)(d: List[Any]): Unit = {
    val out = new PrintStream(new FileOutputStream(new File(fileName), true), true, "utf8")
    out.append(d.mkString("\n"))
    out.append("\n")
    out.close();
  }

  def pollData(geoId: String, geoName: String, offset: Int, pagesize: Int) = {
    println(s"$offset ,$pagesize")
    val url = "http://waimai.meituan.com/ajax/poilist"
    val param = Seq("classify_type" -> "cate_all",
      "sort_type" -> "0",
      "price_type" -> "0",
      "support_online_pay" -> "0",
      "support_invoice" -> "0",
      "support_logistic" -> "0",
      "page_offset" -> s"${offset}",
      "page_size" -> s"${pagesize}")
    val u = java.util.UUID.randomUUID().toString
    val s = Http(url).headers(
      ("Accept", "application/json, text/javascript, */*; q=0.01"),
      ("Origin", "http://waimai.meituan.com"),
      ("X-Requested-With", "XMLHttpRequest"),
      //w_uuid=Yrm_hwJXKQYj1o-4Y8N5hu6m6KxSHko_VaNYhF_NxY1xccwc75lM0HAHPP9g9gpj;
      //Yrm_hwJXKQYj1o-4Y8N5hu6m6KxSHko_VaNYhF_NxY1xccwc75lM0HAHPP9
      //%e9%92%9f%e6%a5%bc   %E5%B0%8F%E5%AF%A8
      ("Cookie", "w_uuid=" + u + "; w_cid=610100; w_cpy_cn=\"%E8%A5%BF%E5%AE%89\"; w_cpy=xian; waddrname=\"" + java.net.URLEncoder.encode(geoName, "utf8") + "\"; w_geoid=" + geoId + "; w_ah=\"34.22905493527651,108.95319599658251,%E5%B0%8F%E5%AF%A8|34.265677984803915,108.95338173955679,%E9%92%9F%E6%A5%BC\"; JSESSIONID=s800462r8qgm1n8wy37hmdzfh; _ga=GA1.3.728080757.1481074893; w_visitid=d3012d7b-2eb8-4051-a9c3-c35d8736e935; __mta=46037659.1481074894066.1481076084230.1481076114667.6; w_utmz=\"utm_campaign=(direct)&utm_source=(direct)&utm_medium=(none)&utm_content=(none)&utm_term=(none)\""),
      ("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99"),
      ("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
    ).postForm(param).asString
    val jb = Json.parse(s.body)
    //    println(jb)
    val more = (jb \ "data" \ "hasMore" get).as[Boolean]
    val result = (jb \ "data" \ "poiList").as[List[JsObject]].map((x) => {
      val a = (x \ "wmPoi4Web" \ "wm_poi_id").get
      val b = (x \ "wmPoi4Web" \ "address").get
      val c = (x \ "wmPoi4Web" \ "name").get
      val d = (x \ "wmPoi4Web" \ "longitude").get
      val e = (x \ "wmPoi4Web" \ "latitude").get
      (a, c, d, e, b)
    })
    (more, result)
  }


  def commentCount(id: String) = {
    val url = s"http://waimai.meituan.com/comment/$id"
    val Pattern ="""\(共收到(\d+)份美食评价\)""".r
    val Pattern(count) = JsoupBrowser().get(url) >> text(".count")
    count
  }

  def restaurant(id: String) = {
    val url = s"http://waimai.meituan.com/restaurant/$id"
    val doc = JsoupBrowser().get(url)
    val sc: List[Element] = doc >> elementList(".sold-count")
    val ft: List[Element] = doc >> elementList("script")

    val scs = sc.map(x => text(x))

    val fts = ft.filter((x) => {
      try {
        (x.attr("type") == "text/template") && x.attr("id").toString.startsWith("foodcontext")
      } catch {
        case e: Exception => false
      }
    }).map(x => x.innerHtml)

    scs.zip(fts).zipWithIndex

  }
}
