

class GtkWidget
{
public:
	GtkWidget();
	int adiciona(void *param);
};

void ClickHandlerBotao(GtkWidget *w, void *data)
{
	int b = w->adiciona(data);
}

typedef void (*ButtonFunc)(GtkWidget *w, void *data);

void gtk_signal_connect(GtkWidget *widget, char *event, ButtonFunc func, void *userData);

int main() 
{
	GtkWidget *botao = new GtkWidget();
	int *a = new int;
	gtk_signal_connect(botao, "clicked", ClickHandlerBotao, a);
}
