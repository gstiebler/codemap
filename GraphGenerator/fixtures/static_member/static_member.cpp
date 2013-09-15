
class Class
{
	static int _globalVar;
	static char *_strChar;
	static int _global2;
	static int *_intP;
	
	void static func();
};

int Class::_globalVar = 0;
int Class::_global2 = 18;

char* Class::_strChar = "teste string";

void Class::func()
{
	int a = _global2;
	_globalVar = 5;
}

int main() 
{
	int b = Class::_globalVar;
	Class::_global2 = 10;
	Class::func();
	int c = Class::_globalVar;
	
	char *temp;
	temp = "temp str";
	Class::_intP = new int;
	*(Class::_intP) = 5;
	Class::_strChar = "teste string";
}
