
int main() 
{
		int *a;
		{
			a = new int;
		}
		*a = 5;
		int b = *a;
		
		int *c = new int;
		*c = 19;
		int d = c;
}
