
struct sInterno
{
	float soEsse;
};

struct sPri
{
	sInterno sIntA;
	sInterno sIntB;
	
	void func1()
	{
		sIntA.soEsse = 20.0;
		sIntB.soEsse = 30.0;
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
	sPri str1;
	str1.func1();
	
	float x = str1.sIntA.soEsse;
	float y = str1.sIntB.soEsse;
    
    receiveStructRef(str1.sIntA);
    int a = str1.sIntA.soEsse;
    
    receiveStructPointer(&(str1.sIntA));
    int b = str1.sIntA.soEsse;
    
    receiveStructCopy(str1.sIntA);
    int c = str1.sIntA.soEsse;
}
