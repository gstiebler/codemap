
int main() 
{
	int a;
	int b = 3;
    int c = 200;
	
	switch(b) {
		case 1: a = 10; break;
		case 2: 
            a = 20; 
            c = 300;
		case 3: a = 30; break;
		default: 
            a = 100;
            c = 500;
	}
}
