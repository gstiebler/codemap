
int* func()
{
	int *x = new int;
	*x = 5;
	return x;
}

class Class
{
public:
	int _a;
	static Class* classInst;
};

Class* func2()
{
	Class *classTemp = new Class;
	classTemp->_a = 10;
	return classTemp;
}

Class* Class::classInst = NULL;

Class* instancia()
{
    Class classInst3;
    Class classInst4;
    classInst3->_a = 33;
    classInst4->_a = 44;
    
    Class::classInst = NULL;
    if( Class::classInst )
        Class::classInst = &classInst4;
    else
        Class::classInst = &classInst3;
        
    return Class::classInst;
}

int main() 
{
	int *a = func();
	int b = *a;
	int c = func2()->_a;
	
	Class *inst = instancia();
	int d = inst->_a;
}
