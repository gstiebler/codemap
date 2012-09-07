
class UnicaClass
{
	int _memberInt;
	
public:
	UnicaClass(int init)
	{
		_memberInt = init;
	}
};

int funcao(UnicaClass &param_ref, UnicaClass *param_pointer)
{
	return param_ref._memberInt + param_pointer->_memberInt;
}

int main() 
{
	UnicaClass ucRef1(1), ucRef2(2), *ucP1, *ucP2;
	int a = ucRef1._memberInt;
	//ucP1 = new UnicaClass(10);
	//ucP2 = new UnicaClass(20);
	
	//int natural = funcao(ucRef1, ucP1);
	//int refRef = funcao(ucRef1, &ucRef2);
	//int pointerPointer = funcao(*ucP1, ucP2);
	//int reverse = funcao(*ucP1, &ucRef2);
}
