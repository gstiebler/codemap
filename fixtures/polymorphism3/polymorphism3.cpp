
class BaseClass
{
public:
	int _baseMember;
	
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
	
	virtual int process(int baseParam) = 0;
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
		return _baseMember * _bMember;
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
    
    int A = classA.calculoA();
    int B = classB.calculoB();
    
    bcPointer = &classB;
    ClassB *temp = (ClassB*)bcPointer;
    int D = temp->calculoB();
	
	if(true)
	{
		//bcPointer = &classB;
        //ClassB *temp = (ClassB*)bcPointer;
        //int D = temp->calculoB();
        //int D = ((ClassB*)bcPointer)->calculoB();
	}
}
