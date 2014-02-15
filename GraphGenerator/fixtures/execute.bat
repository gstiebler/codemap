set path=%path%;"C:\Program Files (x86)\Graphviz 2.28\bin"

dot -Tsvg parent_class_method.dot -o parent_class_method.svg
dot -Tsvg generated.dot -o generated.svg