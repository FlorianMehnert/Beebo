package com.fm.beebo.ui.search

enum class BranchOffice(val id: Int, val displayName: String) {
    Zentralbibliothek(0, "Zentralbibliothek"),
    Neustadt(1, "Neustadt"),
    Pieschen(2, "Pieschen"),
    Cotta(3, "Cotta"),
    Plauen(4, "Plauen"),
    Blasewitz(5, "Blasewitz"),
    Klotzsche(6, "Klotzsche"),
    Langebrueck(7, "Langebrück"),
    MobileBibliothek(8, "Mobile Bibliothek"),
    Weissig(9, "Weißig"),
    Buehlau(10, "Bühlau"),
    Laubegast(11, "Laubegast"),
    Suedvorstadt(14, "Südvorstadt"),
    LeubnitzNeuostra(15, "Leubnitz-Neuostra"),
    Prohlis(21, "Prohlis"),
    Gorbitz(24, "Gorbitz"),
    Gruna(26, "Gruna"),
    Strehlen(27, "Strehlen"),
    Johannstadt(28, "Johannstadt"),
    Weixdorf(29, "Weixdorf"),
    Cossebaude(33, "Cossebaude"),
    EBibo(60, "eBibo - virtuelle Medien"),
    Schulbibliothek(70, "Schulbibliothek");
    companion object {
        private val idMap = values().associateBy(BranchOffice::id)

        fun getById(id: Int): BranchOffice? = idMap[id]
    }
}
