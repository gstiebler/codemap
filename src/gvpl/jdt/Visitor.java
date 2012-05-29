package gvpl.jdt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;


public class Visitor extends ASTVisitor {
	
	public class ASTItem {
		public ASTNode _ast_item;
		public List<ASTItem> _AST;
		
		public ASTItem(ASTNode ast_item){
			_AST = new ArrayList<ASTItem>();
			_ast_item = ast_item;
			
			System.out.println(ast_item.toString());
		}
	}
	
	private Map<ASTNode, ASTItem> _items;
	
	public ASTItem _root;
	
	public Visitor(ASTNode root) {
		_items = new HashMap<ASTNode, ASTItem>();
	
		_root = new ASTItem(root);
		_items.put(root, _root);
	}
	
	private void insert(ASTNode node) {
		ASTNode current = node;
		ASTItem ast_item;
		while(true)
		{
			current = current.getParent();
			if(current == null)
			{
				System.out.println("Erro!! não achou pai.");
				return;
			}
			ast_item = (ASTItem)_items.get(current);
			if(ast_item == null)
				continue;
			
			ASTItem new_item = new ASTItem(node);
			ast_item._AST.add(new_item);
			_items.put(node, new_item);
			return;
		}
	}

	
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		insert(node);
		return false;
	}
	
	public boolean visit(Assignment node) {
		insert(node);
		return true;
	}
	
	@Override
	public boolean visit(InfixExpression node) {
		insert(node);
		return true;
	}
	
	public boolean visit(ForStatement node) {
		insert(node);
		return true;
	}
	
	@Override
	public boolean visit(NumberLiteral node) {
		insert(node);
		return true;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		insert(node);
		return true;
	}
	
	@Override
	public boolean visit(Block node) {
		insert(node);
		return true;
	}
	
	@Override
	public boolean visit(SimpleName node) {
		insert(node);
		return true;
	}
	
}
