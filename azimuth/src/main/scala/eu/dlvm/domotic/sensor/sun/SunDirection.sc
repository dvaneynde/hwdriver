
import eu.dlvm.domotic.sensor.sun.SunHeightAzimuth.{azimuth => azimuth, hoogtehoek => hoogtehoek}
import eu.dlvm.domotic.sensor.sun.SunHeightAzimuth.{toDegrees => toDegrees}

val dag:Int = 365 / 2
(0 to 23).map(uur => uur + ": a=" + Math.round(toDegrees(azimuth(dag,uur))) + "\th="+Math.round(toDegrees(hoogtehoek(0,uur)))).mkString("\n")
(0 to 23).map(uur => uur + ": a=" + Math.round(toDegrees(azimuth(dag,uur))) + "\th="+Math.round(toDegrees(hoogtehoek(180,uur)))).mkString("\n")

//(0 to 23).map(toDegrees(azimhelputh(dag,_))).toList