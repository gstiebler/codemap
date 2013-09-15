
int main() {
	int l = 8;
	
	int q = 3;
	int s = 53;
	int e = 9;
	int d = 11;
	for (int i = 0; i < l; i++)
	{
		s = q;
		q += s - e;
        if( q > 9 )
        {
            d = q + i;
        }
	}
}
