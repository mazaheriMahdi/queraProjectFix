import glob
import os
import re

my_files = glob.glob(os.path.abspath(os.getcwd()) + r'/**/**/**/*.java', recursive=True)
my_files = list(dict.fromkeys(my_files))
main = ''

import_list = []

for item in my_files:
    with open(os.path.relpath(item)) as file:
        split_class = re.split(r'public ', file.read(), 1)
        import_block = split_class[0].split("\n")
        import_list += [item for item in import_block if item.startswith("import")]
        temp = ""
        i = 0
        for a in split_class:
            if i != 0:
                temp += a
            i += 1
        main += "\n\n" + temp
with open("Main.java", 'w') as file:
    file.write("")
    import_list  = "\n".join(import_list)
    main = import_list + main
    file.write(main)
