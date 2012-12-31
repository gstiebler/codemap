
int* func()
{
	int *x = new int;
	*x = 5;
	return x;
}

int main() 
{
	int *a = func();
	int b = *a;
}
