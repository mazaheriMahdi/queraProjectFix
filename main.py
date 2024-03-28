import os
import tkinter as tk
from tkinter import filedialog, messagebox


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


def browse_input_directory(entry):
    directory_path = filedialog.askdirectory()
    if directory_path:
        entry.delete(0, tk.END)
        entry.insert(0, directory_path)


def browse_output_directory(entry):
    directory_path = filedialog.askdirectory()
    if directory_path:
        entry.delete(0, tk.END)
        entry.insert(0, directory_path)


def merge(input_entry, output_entry):
    input_directory = input_entry.get()
    output_directory = output_entry.get()
    if not input_directory:
        messagebox.showerror("Error", "Please select input directory.")
        return
    if not output_directory:
        messagebox.showerror("Error", "Please select output directory.")
        return
    merge_result = merge_java_files(input_directory, output_directory)
    messagebox.showinfo("Merge Result", merge_result)


def main():
    root = tk.Tk()
    root.title("Java File Merger")

    # Get screen width and height
    screen_width = root.winfo_screenwidth()
    screen_height = root.winfo_screenheight()

    # Set window size and position it in the center
    window_width = int(screen_width * 0.5)
    window_height = int(screen_height * 0.5)
    x_position = (screen_width // 2) - (window_width // 2)
    y_position = (screen_height // 2) - (window_height // 2)
    root.geometry(f"{window_width}x{window_height}+{x_position}+{y_position}")

    label_input = tk.Label(root, text="Input Directory:", font=("Arial", 16))
    input_frame = tk.Frame(root)
    input_entry = tk.Entry(input_frame, font=("Arial", 14), width=40)
    input_button = tk.Button(input_frame, text="Browse", font=("Arial", 14), command=lambda: browse_input_directory(input_entry))
    label_output = tk.Label(root, text="Output Directory:", font=("Arial", 16))
    output_frame = tk.Frame(root)
    output_entry = tk.Entry(output_frame, font=("Arial", 14), width=40)
    output_button = tk.Button(output_frame, text="Browse", font=("Arial", 14), command=lambda: browse_output_directory(output_entry))
    merge_button = tk.Button(root, text="Merge", font=("Arial", 16), command=lambda: merge(input_entry, output_entry))

    label_input.pack(pady=10)
    input_frame.pack(pady=5)
    input_entry.pack(side=tk.LEFT)
    input_button.pack(side=tk.LEFT, padx=5)
    label_output.pack(pady=10)
    output_frame.pack(pady=5)
    output_entry.pack(side=tk.LEFT)
    output_button.pack(side=tk.LEFT, padx=5)
    merge_button.pack(pady=20)

    root.mainloop()


if __name__ == "__main__":
    main()
