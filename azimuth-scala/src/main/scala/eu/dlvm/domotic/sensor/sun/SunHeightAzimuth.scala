package eu.dlvm.domotic.sensor.sun

/**
  * Zie http://wiki.bk.tudelft.nl/bk-wiki/Zonnestralingsrichting
  * <br/>Created by dirk on 24/06/2017.
  */
object SunHeightAzimuth {

  def toRadian(degrees: Double): Double =
    degrees * Math.PI / 180.0
  def toDegrees(radians: Double) : Double =
    radians *180/Math.PI

  val breedtegraad = 52
  val sinBreedtegraad = Math.sin(toRadian(breedtegraad))
  val cosBreedtegraad = Math.cos(toRadian(breedtegraad))

  // D = 23,44° sin {360°(284 + n)/365}
  def calcD(dag: Int): Double =
    toRadian(23.44) * Math.sin(toRadian(360 * (284 + dag) / 365))

  def calcU(uur: Double): Double =
    toRadian(15) * uur

  def calcH(d:Double, u:Double):Double = {
    Math.asin(sinBreedtegraad * Math.sin(d) - cosBreedtegraad * Math.cos(d) * Math.cos(u))
  }

  // hoogtehoek h = arcsin (sin ф sin d – cos ф cos d cos u)
  def hoogtehoek(dag:Int, uur:Double): Double = {
    val d = calcD(dag)
    val u = calcU(uur)
    calcH(d,u)
  }


  //azimuth t.o.v. zuiden a = arcsin { (cos d sin u) / cos h }
  def azimuth(dag:Int, uur:Double): Double = {
    val d = calcD(dag)
    val u = calcU(uur)
    val h = calcH(d,u)
    Math.asin((Math.cos(d)*Math.sin(u))/Math.cos(h))
  }

}
