package gvpl.cdt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;

public class Visitor extends ASTVisitor {

	public class ASTItem {
		public IASTNode _ast_item;
		public List<ASTItem> _AST;

		public ASTItem(IASTNode ast_item) {
			_AST = new ArrayList<ASTItem>();
			_ast_item = ast_item;
		}
	}

	private Map<IASTNode, ASTItem> _items;

	public ASTItem _root;

	public Visitor(IASTNode root) {
		_items = new HashMap<IASTNode, ASTItem>();

		_root = new ASTItem(root);
		_items.put(root, _root);
	}

	private void insert(IASTNode node) {
		IASTNode current = node;
		ASTItem ast_item;
		while (true) {
			current = (IASTNode) current.getParent();
			if (current == null) {
				System.out.println("Erro!! não achou pai.");
				return;
			}
			ast_item = (ASTItem) _items.get(current);
			if (ast_item == null)
				continue;

			ASTItem new_item = new ASTItem(node);
			ast_item._AST.add(new_item);
			_items.put(node, new_item);
			return;
		}
	}

	public int visit(IASTName node) {
		insert(node);
		//IBinding a = node.resolveBinding();
		return ASTVisitor.PROCESS_CONTINUE;
	}

	public int visit(IASTDeclarator node) {
		insert(node);
		return ASTVisitor.PROCESS_CONTINUE;
	}

	public int visit(IASTExpression node) {
		insert((IASTNode) node);
		return ASTVisitor.PROCESS_CONTINUE;
	}

	public int visit(IASTParameterDeclaration node) {
		insert((IASTNode) node);
		return ASTVisitor.PROCESS_CONTINUE;
	}

	public int visit(IASTStatement node) {
		insert(node);
		return ASTVisitor.PROCESS_CONTINUE;
	}

	public int visit(IASTProblem node) {
		insert(node);
		return ASTVisitor.PROCESS_CONTINUE;
	}

	/*
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		insert(node);
		return true;
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
	public boolean visit(BooleanLiteral node) {
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
	}*/

}
