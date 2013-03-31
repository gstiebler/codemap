
class cClass
{
	float a;
	float b;
	
    void calledFunc()
    {
        a = 8;
    }
    
	void func1()
    {
        int c = 3;
        for( int i(0); i < c; ++i)
        {
            if( i > 2 )
                calledFunc();
        }
    }
};

int main() {
    cClass instance;
    instance.func1();
    float x = instance.a;
}