
int main() 
{
    int x = 5;
    int *a = &x;
    
    if( 3 > 5 )
    {
        a = new int;
        *a = 4;
    }
    
    int c = *a;
}
