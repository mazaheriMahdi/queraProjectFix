import os


def merge_java_files(input_directory, output_directory):
    # Check if the input directory exists
    if not os.path.exists(input_directory) or not os.path.isdir(input_directory):
        return f"Input directory '{input_directory}' does not exist or is not a directory."

    os.chdir(input_directory)

    # Get a list of Java files in the input directory and its subdirectories
    java_files = []

    for root, dirs, files in os.walk(input_directory):
        for file in files:
            if file.endswith(".java"):
                java_files.append(os.path.join(root, file))

    # Check if there are Java files in the input directory
    if not java_files:
        return f"No Java files found in '{input_directory}'."

    class_contents = []
    imports = set()

    # Iterate through each Java file
    for java_file in java_files:
        with open(java_file, 'r') as infile:
            content = infile.read().split("public", 1)

            for imp in content[0].splitlines():
                if "import" in imp and "java" in imp:
                    imports.add(imp)

            # Remove package declarations and make Main class public
            if os.path.basename(java_file) == "Main.java":
                class_contents.append("public ")
            class_contents.append(content[1].strip())
            class_contents.append("\n\n")

    # Check if the output directory exists, if not, create it
    if not os.path.exists(output_directory):
        os.makedirs(output_directory)

    # Write the merged content to the output file
    output_file = os.path.join(output_directory, "Main.java")
    with open(output_file, 'w') as output:
        output.write("\n".join(imports) + '\n\n' + "".join(class_contents))

    return f"Merged {len(java_files)} Java files into 'Main.java' in '{output_directory}'"


def main():
    pass


if __name__ == "__main__":
    main()
