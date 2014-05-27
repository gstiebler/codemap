import os
import shutil

path = os.getcwd()
strings = path.split("\\")

dir = strings.pop()
filename = dir + ".dot"
shutil.copy("generated.dot", filename)