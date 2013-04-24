
class cClass
{
public:
	float a;
	float b;
	
    void calledFunc()
    {
        a = 8;
    }
    
	void func1()
    {
        int c = 3;
        
        if( c > 4 )
            calledFunc();
        //for( int i(0); i < c; ++i)
        //{
        //    if( i > 2 )
        //        calledFunc();
        //}
    }
};

int main() {
    cClass instance;
    instance.a = 7;
    instance.func1();
    float x = instance.a;
}