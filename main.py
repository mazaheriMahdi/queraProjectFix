import glob
import os
import re

my_files = glob.glob(os.path.abspath(os.getcwd()) + r'/**/**/**/*.java', recursive=True)
my_files = list(dict.fromkeys(my_files))
main = ''

import_list = [
    'import java.util.ArrayList;',
    'import java.util.Arrays;',
    'import java.util.Objects;',
    'import java.util.Scanner;',
    "import java.util.*;",
    "import java.util.regex.Matcher;",
    "import java.util.regex.Pattern;", ]

for item in my_files:
    with open(os.path.relpath(item)) as file:
        aa = re.split(r'public', file.read(), 1)
        temp = ""
        i = 0
        for a in aa:
            if i != 0:
                temp += a
            i += 1
        main += "\n\n" + temp
with open("Main.java", 'w') as file:
    import_list  = "\n".join(import_list)
    main = import_list + main
    file.write(main)
