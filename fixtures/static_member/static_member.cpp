
class Class
{
	static int _globalVar, _global2;
	
	void static func();
};

int Class::_globalVar = 0;
int Class::_global2 = 18;

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
}
