
class ClassA
{
    int _m1;
};

int main() 
{
	ClassA* inst1 = new ClassA;
    inst1->_m1 = 8;
    ClassA* point1;
    point1 = inst1;
    int b = point1->_m1;
}
