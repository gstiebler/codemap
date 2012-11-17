require 'fileutils'

path = Dir.pwd
strings = path.split("/")

dotFilename = "#{strings.last}.dot"
svgFilename = "#{strings.last}.svg"

system("dot -Tsvg #{dotFilename} -o #{svgFilename}")
system("dot -Tsvg generated.dot -o generated.svg")
