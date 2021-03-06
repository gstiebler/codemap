
class BaseClass
{
public:
	int _baseMember;
	
	virtual int process(int baseParam) = 0;
	
	void initialize()
	{
        _baseMember = 8;
		_baseMember = process(3);
	}
    
    int soma(int valor)
    {
        return _baseMember + valor;
    }
    
    int sub(int valor)
    {
        return _baseMember - valor;
    }
};

class ClassA : public BaseClass
{
	
public:
    
	int process(int baseParam)
	{
		return _baseMember + baseParam;
	}
    
    int calculoA()
    {
        return soma(4);
    }
};

class ClassB : public BaseClass
{
public:

	int process(int baseParam)
	{
		return _baseMember * baseParam;
	}
    
    int calculoB()
    {
        return BaseClass::sub(5);
    }
};

int main() 
{
	BaseClass *bcPointer;
	ClassA classA;
	ClassB classB;
    
    classA.initialize();
    classB.initialize();
    
    int A = classA.calculoA();
    int B = classB.calculoB();
	
	if(true)
	{
		bcPointer = &classB;
        int D = ((ClassB*)bcPointer)->calculoB();
	}
}
