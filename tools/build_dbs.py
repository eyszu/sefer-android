#!/usr/bin/env python3
"""Rebuild app/src/main/assets/{asv,tagalog}.db from the source databases.

usage: python3 tools/build_dbs.py ASV.db TagAngBiblia.db

Source schema:  <PREFIX>_verses(id, book_id, chapter, verse, text)
Output schema:  verses(book, chapter, verse, text)  PRIMARY KEY (book, chapter, verse)

 * Duplicate (book, chapter, verse) rows: the row with the highest id wins (in the Tagalog
   source the later rows are the corrected ones).
 * The divine name is stored in the sources as Phoenician letters (U+10909 U+10904 U+10905
   U+10904), which Android system fonts can't draw. They are converted to Hebrew letters.
Bump BibleRepository.DATA_VERSION after changing the output so installed apps refresh.
"""
import os, sqlite3, sys

OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "assets")
PHOENICIAN_TO_HEBREW = {0x10909: "י", 0x10904: "ה", 0x10905: "ו"}

def build(src_path, prefix, out_name):
    src = sqlite3.connect(src_path)
    rows = {}
    for b, c, v, t in src.execute(f"SELECT book_id, chapter, verse, text FROM {prefix}_verses ORDER BY id"):
        rows[(b, c, v)] = t.strip()
    out_path = os.path.join(OUT, out_name)
    if os.path.exists(out_path):
        os.remove(out_path)
    out = sqlite3.connect(out_path)
    out.execute("CREATE TABLE verses(book INTEGER NOT NULL, chapter INTEGER NOT NULL, verse INTEGER NOT NULL, "
                "text TEXT NOT NULL, PRIMARY KEY(book, chapter, verse)) WITHOUT ROWID")
    for (b, c, v), t in sorted(rows.items()):
        out.execute("INSERT INTO verses VALUES (?,?,?,?)", (b, c, v, t.translate(PHOENICIAN_TO_HEBREW)))
    out.commit(); out.execute("VACUUM"); out.close()
    print(out_name, len(rows), "verses")

if __name__ == "__main__":
    build(sys.argv[1], "ASV", "asv.db")
    build(sys.argv[2], "TagAngBiblia", "tagalog.db")
