
enum eMyEnum
{
    E_FIRST = 0,
    E_SECOND,
    E_THIRD
};

int main() 
{
	int a = E_FIRST;
    eMyEnum b = E_SECOND;
    a = E_THIRD;
    eMyEnum c;
    c = a;
    int d = E_SECOND;
}
