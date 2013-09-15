
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
	ClassA(int dummyA) : BaseClass(10, 55)
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
	ClassB(int dummyB) : BaseClass(20, 55), _bMember(30)
	{}

	int process(int baseParam)
	{
		return _baseMember * _bMember;
	}
};

int main() 
{
	BaseClass *bcPointer;
	ClassA classA(1);
	ClassB classB(2);
	
	if(true)
	{
		bcPointer = &classA;
	}
	else
	{
		bcPointer = &classB;
	}
	
	int a = bcPointer->process(40);
	bcPointer->_baseMember = a;
	int b = bcPointer->_baseMember;
}
