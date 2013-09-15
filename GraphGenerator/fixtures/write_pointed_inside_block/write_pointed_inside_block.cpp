
int main() 
{
    int x = 7;
    int *a;
    a = &x;
    
    {
        *a = 4;
    }
        
    int d = x;
}
