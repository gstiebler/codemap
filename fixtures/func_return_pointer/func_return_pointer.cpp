
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
};

Class* func2()
{
	Class *classTemp = new Class;
	classTemp->_a = 10;
	return classTemp;
}

int main() 
{
	int *a = func();
	int b = *a;
	int c = func2()->_a;
}
