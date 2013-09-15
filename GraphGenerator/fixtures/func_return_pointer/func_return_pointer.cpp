
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
    if( !Class::classInst )
    {
        Class::classInst = new Class();
    }
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
