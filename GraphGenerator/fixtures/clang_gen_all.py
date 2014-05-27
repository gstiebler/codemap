import os

path = os.getcwd()

print path
clang_path = "C:\\Projetos\\clang\\build\\Release\\bin\\clang-check.exe"
for x in os.walk(path):
    curr_path = x[0]
    strings = curr_path.split("\\")
    curr_dir = strings.pop()
    line = "{} {}\\{}.cpp -ast-dump -- > {}\\{}.ast".format(clang_path, curr_dir, curr_dir, curr_dir, curr_dir)
    print line
    os.system(line)