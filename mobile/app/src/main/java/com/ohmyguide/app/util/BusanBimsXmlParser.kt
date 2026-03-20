package com.ohmyguide.app.util

import com.ohmyguide.app.data.model.BusArrivalInfo
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object BusanBimsXmlParser {

    fun parse(xml: String): List<BusArrivalInfo> {
        val items = mutableListOf<BusArrivalInfo>()
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(StringReader(xml))

        var inItem = false
        var tag = ""
        var arsno = ""
        var bstopid = ""
        var lineno = ""
        var lineid = ""
        var nodenm = ""
        var min1 = 0
        var station1 = 0
        var lowplate1 = 0
        var carno1 = ""
        var min2 = 0
        var station2 = 0
        var lowplate2 = 0
        var carno2 = ""
        var bustype = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    tag = parser.name
                    if (tag == "item") {
                        inItem = true
                        arsno = ""; bstopid = ""; lineno = ""; lineid = ""
                        nodenm = ""; carno1 = ""; carno2 = ""; bustype = ""
                        min1 = 0; station1 = 0; lowplate1 = 0
                        min2 = 0; station2 = 0; lowplate2 = 0
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inItem) {
                        val text = parser.text?.trim() ?: ""
                        when (tag) {
                            "arsno" -> arsno = text
                            "bstopid" -> bstopid = text
                            "lineno" -> lineno = text
                            "lineid" -> lineid = text
                            "nodenm" -> nodenm = text
                            "min1" -> min1 = text.toIntOrNull() ?: 0
                            "station1" -> station1 = text.toIntOrNull() ?: 0
                            "lowplate1" -> lowplate1 = text.toIntOrNull() ?: 0
                            "carno1" -> carno1 = text
                            "min2" -> min2 = text.toIntOrNull() ?: 0
                            "station2" -> station2 = text.toIntOrNull() ?: 0
                            "lowplate2" -> lowplate2 = text.toIntOrNull() ?: 0
                            "carno2" -> carno2 = text
                            "bustype" -> bustype = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "item" && inItem) {
                        items += BusArrivalInfo(
                            arsno = arsno,
                            bstopid = bstopid,
                            lineno = lineno,
                            lineid = lineid,
                            nodenm = nodenm,
                            min1 = min1,
                            station1 = station1,
                            lowplate1 = lowplate1,
                            carno1 = carno1,
                            min2 = min2,
                            station2 = station2,
                            lowplate2 = lowplate2,
                            carno2 = carno2,
                            bustype = bustype,
                        )
                        inItem = false
                    }
                    tag = ""
                }
            }
            eventType = parser.next()
        }
        return items
    }
}