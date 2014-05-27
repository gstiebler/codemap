import os

path = os.getcwd()
strings = path.split("\\")

dir = strings.pop()

dotFilename = dir + ".dot"
svgFilename = dir + ".svg"

os.system("dot -Tsvg %s -o %s" % (dotFilename, svgFilename))
os.system("dot -Tsvg generated.dot -o generated.svg")