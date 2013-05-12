
enum eMyEnum
{
    E_FIRST = 0,
    E_SECOND,
    E_THIRD
};

class Class1
{
public:

    enum eInsideClass
    {
        E_IC_1,
        E_IC_2
    };
};

int main() 
{
	int a = E_FIRST;
    eMyEnum b = E_SECOND;
    a = E_THIRD;
    eMyEnum c;
    c = a;
    int d = E_SECOND;
    
    int e = Class1::E_IC_2;
}
