#!/usr/bin/env python3

#------------------------------------------------------------------------------
# Colorize MultimediaLib
# Copyright 2009-2026 Colorize
# Apache license (http://www.apache.org/licenses/LICENSE-2.0)
#------------------------------------------------------------------------------

import os
import sys

sys.path.append(os.environ["COLORIZE_PYTHON_DEVELOPMENT_SCRIPTS_PATH"])
from clrzprojectlib import *


REPLACEMENTS = {
    "Float.parseFloat" : "Double.parseDouble",
    "<Float>" : "<Double>",
    "float " : "double ",
    "float[]" : "double[]",
    "RNG.getFloat" : "RNG.getDouble",
    "float..." : "double...",
    "Float.MAX_VALUE" : "Double.MAX_VALUE",
    "(float) " : "(double) ",
    "Float.compare" : "Double.compare",
    ", Float>" : ", Double>",
    "Float::parseFloat" : "Double::parseDouble",
    "getFloat(" : "getDouble("
}


if __name__ == "__main__":
    if "--force" not in sys.argv and "--dry" not in sys.argv:
        print("Usage: migrate_math.py <--force | --dry>")
        sys.exit(1)

    dry = sys.argv[1] != "--force"

    for file in inspectProject("."):
        if file.type in ("Java", "Java (Test)"):
            print(file.relativePath)
            replacements = rewriteFile(file.path, REPLACEMENTS, dry)
            if dry:
                for oldLine, newLine in replacements:
                    print(f"    <<<  {oldLine.strip()}")
                    print(f"    >>>  {newLine.strip()}")
                    print("")
