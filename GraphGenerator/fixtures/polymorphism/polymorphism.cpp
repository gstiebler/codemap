
class BaseClass
{
public:
	int _baseMember;
	
	BaseClass(int init, int dummy)
	{
		_baseMember = init;
	}
	
	virtual int process(int baseParam) = 0;
};

class ClassA : public BaseClass
{
	int _aMember;
	
public:
	ClassA() : BaseClass(10, 55)
	{}

	int process(int baseParam)
	{
		return _baseMember + baseParam;
	}
};

class ClassB : public BaseClass
{
	int _bMember;
	
public:
	ClassB() : BaseClass(20, 55), _bMember(30)
	{}

	int process(int baseParam)
	{
		return _baseMember * _bMember;
	}
};

int main() 
{
	BaseClass *bcPointer;
	
	if(true)
	{
		bcPointer = new ClassA();
	}
	else
	{
		bcPointer = new ClassB();
	}
	
	int e = bcPointer->_baseMember;
	int a = bcPointer->process(40);
	bcPointer->_baseMember = a;
	int b = bcPointer->_baseMember;
}
