require 'fileutils'

path = Dir.pwd
strings = path.split("/")
filename = "#{strings.last}.dot"
FileUtils.cp "generated.dot", filename
