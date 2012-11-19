
class BaseClassTest
{
	virtual int func(int param) = 0;
	
	int calledFunc(int paramCF)
	{
		int result = func(paramCF);
		return 3 * result;
	}
};

class ClassTest : public BaseClassTest
{
public:
	int func(int param)
	{
		return 2 * param;
	}
};

int main() 
{
	ClassTest classTest;
	int a = classTest(8);
}
