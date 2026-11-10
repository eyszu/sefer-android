package app.sefer.data

/**
 * Book names are keyed by canonical book number (1 = Genesis ... 66 = Revelation), which is
 * also the `book` column in the bundled databases. The lists cover all 66 books so a database
 * that later includes the New Testament works without code changes.
 */
object BookNames {
    val english = listOf(
        "Genesis", "Exodus", "Leviticus", "Numbers", "Deuteronomy", "Joshua", "Judges", "Ruth",
        "1 Samuel", "2 Samuel", "1 Kings", "2 Kings", "1 Chronicles", "2 Chronicles", "Ezra",
        "Nehemiah", "Esther", "Job", "Psalms", "Proverbs", "Ecclesiastes", "Song of Solomon",
        "Isaiah", "Jeremiah", "Lamentations", "Ezekiel", "Daniel", "Hosea", "Joel", "Amos",
        "Obadiah", "Jonah", "Micah", "Nahum", "Habakkuk", "Zephaniah", "Haggai", "Zechariah",
        "Malachi",
        "Matthew", "Mark", "Luke", "John", "Acts", "Romans", "1 Corinthians", "2 Corinthians",
        "Galatians", "Ephesians", "Philippians", "Colossians", "1 Thessalonians",
        "2 Thessalonians", "1 Timothy", "2 Timothy", "Titus", "Philemon", "Hebrews", "James",
        "1 Peter", "2 Peter", "1 John", "2 John", "3 John", "Jude", "Revelation",
    )

    val tagalog = listOf(
        "Genesis", "Exodo", "Levitico", "Mga Bilang", "Deuteronomio", "Josue", "Mga Hukom", "Ruth",
        "1 Samuel", "2 Samuel", "1 Mga Hari", "2 Mga Hari", "1 Mga Cronica", "2 Mga Cronica",
        "Ezra", "Nehemias", "Esther", "Job", "Mga Awit", "Mga Kawikaan", "Eclesiastes",
        "Awit ng mga Awit", "Isaias", "Jeremias", "Mga Panaghoy", "Ezekiel", "Daniel", "Oseas",
        "Joel", "Amos", "Abdias", "Jonas", "Mikas", "Nahum", "Habacuc", "Sofonias", "Hagai",
        "Zacarias", "Malakias",
        "Mateo", "Marcos", "Lucas", "Juan", "Mga Gawa", "Roma", "1 Corinto", "2 Corinto",
        "Galacia", "Efeso", "Filipos", "Colosas", "1 Tesalonica", "2 Tesalonica", "1 Timoteo",
        "2 Timoteo", "Tito", "Filemon", "Hebreo", "Santiago", "1 Pedro", "2 Pedro", "1 Juan",
        "2 Juan", "3 Juan", "Judas", "Apocalipsis",
    )

    fun name(version: BibleVersion, book: Int): String {
        val list = if (version == BibleVersion.TAGALOG) tagalog else english
        return list.getOrNull(book - 1) ?: "Book $book"
    }

    fun isOldTestament(book: Int) = book <= 39
}
