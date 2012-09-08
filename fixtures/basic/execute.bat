set path=%path%;"C:\Program Files (x86)\Graphviz 2.28\bin"

dot -Tpng generated.dot -o generated.png
dot -Tpng basic.dot -o basic.png