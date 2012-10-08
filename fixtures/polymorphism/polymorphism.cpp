
class BaseClass
{
	int _baseMember;
	
public:
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
	bcPointer = new ClassA();
	int a = bcPointer->process(40);
}
