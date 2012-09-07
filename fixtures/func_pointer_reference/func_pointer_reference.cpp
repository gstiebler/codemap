
class UnicaClass
{
	int _memberInt;
	
public:
	UnicaClass(int init)
	{
		_memberInt = init;
	}
};

int funcao(UnicaClass &paramRef, UnicaClass *paramPointer)
{
	return paramRef._memberInt + paramPointer->_memberInt;
}

int main() 
{
	UnicaClass ucRef1(1);
	UnicaClass ucRef2(2); 
	UnicaClass *ucP1, *ucP2;
	ucP1 = new UnicaClass(10);
	ucP2 = new UnicaClass(20);
	
	int natural11 = funcao(ucRef1, ucP1);
	int refRef3 = funcao(ucRef1, &ucRef2);
}
