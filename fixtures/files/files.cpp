
#include <soma2.h>
#include "sub_folder/soma3.h"
#include "static_func.h"

int main() 
{
	int x;
	int a = 3;
	int b = 5;
	int c = soma(a, b);
	
	int d = soma(a, b, 0.3);
	
	int e = ClassTest::staticFunc(8);
	
	int g = sub(15, 20);
	
	int f = ClassTest::staticFuncSub(10);
	
	ClassTest ct;
	ct._member = f;
}
