import os

path = os.getcwd()

print path
for x in os.walk(path):
    curr_path = x[0]
    strings = curr_path.split("\\")
    curr_dir = strings.pop()
    
    dotFilename = "{}/{}.dot".format(curr_dir, curr_dir)
    svgFilename = "{}/{}.svg".format(curr_dir, curr_dir)
    
    originalLine = "dot -Tsvg %s -o %s" % (dotFilename, svgFilename)
    generatedLine = "dot -Tsvg {}/generated.dot -o {}/generated.svg".format(curr_dir, curr_dir)
    print originalLine
    print generatedLine
    os.system(originalLine)
    os.system(generatedLine)