set path=%path%;"C:\Program Files (x86)\Graphviz 2.28\bin"

dot -Tsvg generated.dot -o generated.svg
dot -Tsvg basic.dot -o basic.svg