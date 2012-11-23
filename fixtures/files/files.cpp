
#include <soma2.h>
#include "sub_folder/soma3.cpp"
#include "static_func.cpp"

int main() 
{
	int x;
	int a = 3;
	int b = 5;
	int c = soma(a, b);
	
	int d = soma(a, b, 0.3);
	
	int e = ClassTest::staticFunc(8);
}
