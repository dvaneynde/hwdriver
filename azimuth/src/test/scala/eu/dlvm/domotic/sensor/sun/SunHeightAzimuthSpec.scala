package eu.dlvm.domotic.sensor.sun

import org.scalatest.FlatSpec

/**
  * Created by dirk on 24/06/2017.
  */
class SunHeightAzimuthSpec extends FlatSpec {

  "Op 21 juni" should " hoogtes moeten overeenkomen met tabel" in {
    import java.util.Calendar
    val calendar = Calendar.getInstance()
    calendar.set(2017, 6, 21)
    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

  }
}
/*
 	22 december	21 maart / 23 september	21 juni
 	a	h	a	h	a	h
4.00	 	 	 	 	127° 22'	1° 47'
5.00	 	 	 	 	116° 00'	9° 39'
6.00	 	 	 	 	104° 58'	18° 17'
7.00	 	 	78° 05'	9° 10'	93° 45'	27° 23'
8.00	 	 	65° 32'	17° 56'	81° 40'	36° 35'
9.00	40° 37'	4° 55'	51° 46'	25° 48'	67° 42'	45° 29'
10.00	27° 46'	10° 06'	36° 14'	32° 13'	50° 17'	53° 24'
11.00	14° 08'	13° 24'	18° 47'	36° 29'	27° 39'	59° 14'
12.00	0	14° 32'	0	38° 00'	0	61° 28'
13.00	-14° 08'	13° 24'	-18° 47'	36° 29'	-27° 39'	59° 14'
14.00	-27° 46'	10° 06'	-36° 14'	32° 13'	-50° 17'	53° 24'
15.00	-40° 37'	4° 55'	-51° 46'	25° 48'	-67° 42'	45° 29'
16.00	 	 	-65° 32'	17° 56'	-81° 40'	36° 35'
17.00	 	 	-78° 05'	9° 10'	-93° 45'	27° 23'
18.00	 	 	 	 	-104° 58'	18° 17'
19.00	 	 	 	 	-116° 00'	9° 39'
20.00	 	 	 	 	-127° 22'	1° 47'
 */