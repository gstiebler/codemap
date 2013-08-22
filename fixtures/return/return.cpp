
int func1( int param1 )
{
    return param1;
}

int func2( int param2 )
{
    if( param2 > 3)
        return 20;
        
    if( param2 > 5)
        return 30;
    
    int a1 = 100;
    int b1 = 200;
    if( param2 > 15)
        return b1 / a1;
}

int main() 
{
	int a = func1( 8 );
    int b = func2( 10 );
}
