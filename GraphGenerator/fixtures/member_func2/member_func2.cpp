
class ClassDummy;

struct sInterno
{
	float soEsse;
};

struct sPri
{
	sInterno sIntA;
	sInterno sIntB;
    int dummyInt;
	
	void func1()
	{
		sIntA.soEsse = 20.0;
		sIntB.soEsse = 30.0;
	}
    
    sPri(int di)
    {
        dummyInt = di;
    }
    
    sPri(ClassDummy *cdParam)
    {
    }
};

void receiveStructRef(sInterno &param1)
{
    param1.soEsse = 55.0;
}

void receiveStructPointer(sInterno* param1)
{
    param1->soEsse = 155.0;
}

void receiveStructCopy(sInterno param1)
{
    param1.soEsse = 255.0;
}

int main() {
	sPri str1(42);
	str1.func1();
    int d = str1.dummyInt;
	
	float x = str1.sIntA.soEsse;
	float y = str1.sIntB.soEsse;
    
    receiveStructRef(str1.sIntA);
    int a = str1.sIntA.soEsse;
    
    receiveStructPointer(&(str1.sIntA));
    int b = str1.sIntA.soEsse;
    
 //   receiveStructCopy(str1.sIntA);
 //   int c = str1.sIntA.soEsse;
    
    ClassDummy *cd;
    sPri dummyClassConstructor(cd);
    int e = dummyClassConstructor.dummyInt;
}
