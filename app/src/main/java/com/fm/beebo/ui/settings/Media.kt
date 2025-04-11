package com.fm.beebo.ui.settings

import com.fm.beebo.ui.settings.Media.Bluray
import com.fm.beebo.ui.settings.Media.Bücher
import com.fm.beebo.ui.settings.Media.CDs
import com.fm.beebo.ui.settings.Media.EAudio
import com.fm.beebo.ui.settings.Media.EBook
import com.fm.beebo.ui.settings.Media.Einzelband
import com.fm.beebo.ui.settings.Media.Filme
import com.fm.beebo.ui.settings.Media.Kinderbücher
import com.fm.beebo.ui.settings.Media.Noten
import com.fm.beebo.ui.settings.Media.Other
import com.fm.beebo.ui.settings.Media.Streaming
import com.fm.beebo.ui.settings.Media.Schallplatte
import com.fm.beebo.ui.settings.Media.Spiel
import com.fm.beebo.ui.settings.Media.Karte
import com.fm.beebo.ui.settings.Media.CDRom
import com.fm.beebo.ui.settings.Media.Zeitung
import com.fm.beebo.ui.settings.Media.Musikgegenstand


enum class Media {
    Alles {
        override fun asGetParameter(): String {
            return ""
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("")
        }

        override fun getChipString(): String {
            return ""
        }

        override fun getIcon(): String {
            return ""
        }
    },
    Bücher {
        override fun asGetParameter(): String {
            return "297"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("Buch", "eBook")
        }

        override fun getChipString(): String {
            return "Bücher"
        }

        override fun getIcon(): String {
            return "📖"
        }
    },
//    KeinKinderbuch {
//        override fun asGetParameter(): String {
//            return "298"
//        }
//
//        override fun getKindOfMedium(): List<String> {
//            return listOf(
//                "Buch",
//                "Übergeordneter Titel, siehe auch Einzelbände",
//                "Einzelband einer Serie, siehe auch übergeordnete Titel",
//                "Artikel"
//            )
//        }
//
//        override fun getChipString(): String {
//            return "Buch für Erwachsene"
//        }
//
//        override fun getIcon(): String {
//            return "📖"
//        }
//
//    },
    Kinderbücher {
        override fun asGetParameter(): String {
            return "299"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("Kinderbuch")
        }

        override fun getChipString(): String {
            return "Kinderbücher"
        }

        override fun getIcon(): String {
            return "\uD83E\uDDF8"
        }
    },
    Filme {
        override fun asGetParameter(): String {
            return "305"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("DVD")
        }

        override fun getChipString(): String {
            return "DVDs"
        }

        override fun getIcon(): String {
            return "📀"
        }
    },
    CDs {
        override fun asGetParameter(): String {
            return "303"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("CD")
        }

        override fun getChipString(): String {
            return "CDs"
        }

        override fun getIcon(): String {
            return "💿"
        }
    },
    CDRom {
        override fun asGetParameter(): String {
            return "304"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("DVD-ROM", "CD-ROM")
        }

        override fun getChipString(): String {
            return "CD-Roms"
        }

        override fun getIcon(): String {
            return "💾"
        }
    },
    Karte {
        override fun asGetParameter(): String {
            return "306"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("Karte")
        }

        override fun getChipString(): String {
            return "Karten"
        }

        override fun getIcon(): String {
            return "💳"
        }
    },
    Schallplatte {
        override fun asGetParameter(): String {
            return "307"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("Schallplatte")
        }

        override fun getChipString(): String {
            return "Schallplaten"
        }

        override fun getIcon(): String {
            return "⚫"
        }
    },
    Noten {
        override fun asGetParameter(): String {
            return "309"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("Noten")
        }

        override fun getChipString(): String {
            return "Noten"
        }

        override fun getIcon(): String {
            return "♬"
        }
    },
    Spiel {
        override fun asGetParameter(): String {
            return "310"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("Spiel")
        }

        override fun getChipString(): String {
            return "Spiele"
        }

        override fun getIcon(): String {
            return "\uD83C\uDFB2"
        }
    },
    Zeitung {
        override fun asGetParameter(): String {
            return "315"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("Zeitung / Zeitschrift", "eMagazine")
        }

        override fun getChipString(): String {
            return "Zeitung"
        }

        override fun getIcon(): String {
            return "📰"
        }
    },
//    Item {
//        override fun asGetParameter(): String {
//            return "369"
//        }
//
//        override fun getKindOfMedium(): List<String> {
//            return listOf("Gegenstand (Bibliothek der Dinge)")
//        }
//
//        override fun getChipString(): String {
//            return "Gegenstand"
//        }
//
//        override fun getIcon(): String {
//            return "\uD83D\uDD2D"
//        }
//    },
    Musikgegenstand {
        override fun asGetParameter(): String {
            return "370"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("Musikinstrument (Bibliothek der Dinge)")
        }

        override fun getChipString(): String {
            return "Instrument"
        }

        override fun getIcon(): String {
            return "\uD83E\uDD41"
        }
    },
    Streaming {
        override fun asGetParameter(): String {
            return "379"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("Videostreaming über Filmfriend")
        }

        override fun getChipString(): String {
            return "Streaming"
        }

        override fun getIcon(): String {
            return "\uD83D\uDCFA"
        }
    },
//    Nintendo{
//        override fun asGetParameter(): String {
//            return "316"
//        }
//        override fun getKindOfMedium(): List<String> {
//            return listOf("Spiel für Nintendo DS")
//        }
//        override fun getChipString(): String {
//            return "NDS"
//        }
//        override fun getIcon(): String {
//            return "\uD83C\uDFAE"
//        }
//    },
    Bluray {
        override fun asGetParameter(): String {
            return "301"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("Blu-ray Disc")
        }

        override fun getChipString(): String {
            return "Blu-ray"
        }

        override fun getIcon(): String {
            return "🔵"
        }
    },
//    BluRay3D {
//        override fun asGetParameter(): String {
//            return "302"
//        }
//
//        override fun getKindOfMedium(): List<String> {
//            return listOf("Blu-ray Disc 3D")
//        }
//
//        override fun getChipString(): String {
//            return "Blu-ray 3D"
//        }
//
//        override fun getIcon(): String {
//            return "\uD83D\uDD37"
//        }
//    },
    Einzelband {
        override fun asGetParameter(): String {
            return ""
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("Einzelband einer Serie, siehe auch übergeordnete Titel")
        }

        override fun getChipString(): String {
            return "Einzelband"
        }

        override fun getIcon(): String {
            return "📕"
        }
    },
//    ÜbergeordnetesBuch {
//        override fun asGetParameter(): String {
//            return ""
//        }
//
//        override fun getKindOfMedium(): List<String> {
//            return listOf("Übergeordneter Titel, siehe auch Einzelbände")
//        }
//
//        override fun getChipString(): String {
//            return "Buchserie"
//        }
//
//        override fun getIcon(): String {
//            return "\uD83D\uDCDA"
//        }
//    },
    EAudio {
        override fun asGetParameter(): String {
            return "331"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("eAudio")
        }

        override fun getChipString(): String {
            return "eAudio"
        }

        override fun getIcon(): String {
            return "\uD83D\uDD09"
        }
    },
    EBook {
        override fun asGetParameter(): String {
            return "332"
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("eBook")
        }

        override fun getChipString(): String {
            return "eBook"
        }

        override fun getIcon(): String {
            return "\uD83D\uDCD7"
        }
    },
//    Tonie {
//        override fun asGetParameter(): String {
//            return "392"
//        }
//
//        override fun getKindOfMedium(): List<String> {
//            return listOf("Tonie-Figur")
//        }
//
//        override fun getChipString(): String {
//            return "Tonie"
//        }
//
//        override fun getIcon(): String {
//            return "\uD83D\uDC40"
//        }
//
//    },
    Other {
        override fun asGetParameter(): String {
            return ""
        }

        override fun getKindOfMedium(): List<String> {
            return listOf("")
        }

        override fun getChipString(): String {
            return "Sonstiges"
        }

        override fun getIcon(): String {
            return "❓"
        }
    };

    companion object {
        fun iterator(): Iterator<Media> = entries.iterator()
    }

    abstract fun asGetParameter(): String
    abstract fun getKindOfMedium(): List<String>

    /**
     * short description for the filter chips
     */
    abstract fun getChipString(): String
    abstract fun getIcon(): String
}

fun mediaFromString(string: String): Media {
    return when (string) {
        "Buch" -> Bücher
        "Kinderbuch" -> Kinderbücher
        "DVD" -> Filme
        "CD" -> CDs
        "Noten" -> Noten
        "Videostreaming über Filmfriend" -> Streaming
        "Blu-ray Disc" -> Bluray
        "Einzelband einer Serie, siehe auch übergeordnete Titel" -> Einzelband
        "eAudio" -> EAudio
        "eBook" -> EBook
        "Tonie-Figur" -> Other
        "Karte" -> Karte
        "Artikel" -> Bücher
        "DVD-ROM" -> CDRom
        "CD-ROM" -> CDRom
        "Schallplatte" -> Schallplatte
        "Spiel" -> Spiel
        "Spiel für PlayStation 4" -> Spiel
        "Spiel für Nintendo Switch" -> Spiel
        "Spiel für Nintendo DS" -> Spiel
        "eMagazine" -> Zeitung
        "Zeitung / Zeitschrift" -> Zeitung
        "Gegenstand (Bibliothek der Dinge)" -> Other
        "Musikinstrument (Bibliothek der Dinge)" -> Musikgegenstand
        "Blu-ray Disc 3D" -> Bluray
        "Übergeordneter Titel, siehe auch Einzelbände" -> Bücher
        else -> Other
    }
}
