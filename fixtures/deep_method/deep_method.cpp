
class cClass
{
public:
	float a;
	float b;
    float c;
	
    void calledFuncA()
    {
        a = 18;
    }
   
    void calledFuncB()
    {
        b = 28;
    }
	
    void calledFuncC()
    {
        c = 38;
    }
    
	void func1()
    {
        int K = 3;
        bool cond = K > 4;
        
        if( cond )
            calledFuncA();
               
        {
            if( cond )
                calledFuncB();
        }
            
        for( int i(0); i < K; ++i)
        {
            if( i > 2 )
                calledFuncC();
        }
    }
};

int main() {
    cClass instance;
    instance.a = 17;
    instance.b = 27;
    instance.c = 37;
    instance.func1();
    float x = instance.a;
    float y = instance.b;
    float z = instance.c;
}