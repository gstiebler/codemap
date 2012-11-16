
int main() 
{
	int a = 3;
	
	bool cond1 = true;
	bool cond2 = false;
	
	int b = 10;
	if(cond1)
	{
		if (cond2)
			a = 7;
		else
		{
			a = 9;
			b = a;
		}
	}
	int c = a;
}
