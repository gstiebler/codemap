
int main() 
{
	int a, b, g;
	int *pointer = &g;
	
	if(5 > 2)
	{
		pointer = &a;
	}
	else
	{
		pointer = &b;
	}
	
	a = 10;
	b = 20;
	
	int c = *pointer;
	int *pointerRec = pointer;
	int f = *pointerRec
	
	int d = 30;
	pointer = &d;
	int e = *pointer;
}
