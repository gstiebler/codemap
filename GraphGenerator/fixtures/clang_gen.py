import os

path = os.getcwd()
strings = path.split("\\")

dir = strings.pop()

clang_path = "C:\\Projetos\\clang\\build\\Debug\\bin\\clang-check.exe"
line = "{} {}.cpp -ast-dump -- > {}.ast".format(clang_path, dir, dir)
print line
os.system(line)