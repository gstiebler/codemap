
class ClassA
{
    int _m1;
};

class ParentClass
{
    ClassA *_classA;
};

int main() 
{
    ClassA classA;
    classA._m1 = 8;
	ParentClass parentClass;
    parentClass._classA = &classA;
    int b = parentClass._classA->_m1;
}
